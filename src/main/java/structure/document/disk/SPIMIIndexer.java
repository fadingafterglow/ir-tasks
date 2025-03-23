package structure.document.disk;

import document.Document;
import encoders.EncodedInputStream;
import encoders.EncodedOutputStream;
import lombok.SneakyThrows;
import tokenizer.Tokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static structure.document.disk.Utils.*;

public class SPIMIIndexer implements Indexer {

    private static final int MAX_NUMBER_OF_THREADS = 10;
    private static final String BLOCK_FILE_PREFIX = "block-";
    private final Path path;
    private final Function<OutputStream, EncodedOutputStream> encodedOutputStreamFactory;
    private final Function<InputStream, EncodedInputStream> encodedInputStreamFactory;
    private Iterator<DocumentInfo> documentsIterator;
    private int blockId;

    @SneakyThrows
    public SPIMIIndexer(String path, Function<OutputStream, EncodedOutputStream> encodedOutputStreamFactory, Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        this.path = Path.of(path);
        Files.createDirectories(this.path);
        this.encodedOutputStreamFactory = encodedOutputStreamFactory;
        this.encodedInputStreamFactory = encodedInputStreamFactory;
    }

    @Override
    public void index(Collection<Document> documents, Tokenizer tokenizer) {
        if (documents.isEmpty()) return;
        buildDocumentsMap(documents);
        buildBlocks(documents, tokenizer);
        mergeBlocks();
        deleteBlocks();
        reset();
    }

    @SneakyThrows
    private void buildDocumentsMap(Collection<Document> documents) {
        List<DocumentInfo> list = new ArrayList<>(documents.size());
        int id = 0;
        try (PrintWriter os = new PrintWriter(new BufferedOutputStream(new FileOutputStream(path(DOCUMENTS_MAP_FILE_NAME), false)), false, StandardCharsets.UTF_8)) {
            for (Document document : documents) {
                list.add(new DocumentInfo(document, id++));
                os.println(document.getName());
            }
        }
        documentsIterator = list.iterator();
    }

    @SneakyThrows
    private void buildBlocks(Collection<Document> documents, Tokenizer tokenizer) {
        int numberOfThreads = Math.min(MAX_NUMBER_OF_THREADS, documents.size());
        long minMemoryThreshold = freeMemory() / (numberOfThreads + 1);
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new InverterThread(tokenizer, minMemoryThreshold);
            threads[i].start();
        }
        for (Thread thread : threads)
            thread.join();
    }

    @SneakyThrows
    private void mergeBlocks() {
        PriorityQueue<Block> queue = initBlockQueue();
        long position = 0;
        try (EncodedOutputStream osPostings = os(POSTINGS_FILE_NAME);
             EncodedOutputStream osVocabulary = os(VOCABULARY_FILE_NAME)) {
            while (!queue.isEmpty()) {
                Block block = queue.poll();
                String term = block.currentTerm();
                LinkedList<Integer> documentIds = new LinkedList<>(block.currentDocumentIds());
                while (!queue.isEmpty() && queue.peek().currentTerm().equals(term)) {
                    Block nextBlock = queue.poll();
                    merge(documentIds, nextBlock.currentDocumentIds());
                    if (nextBlock.advance()) queue.add(nextBlock);
                    else nextBlock.close();
                }
                if (block.advance()) queue.add(block);
                else block.close();
                osVocabulary.write(term);
                osVocabulary.write(documentIds.size());
                osVocabulary.write(position);
                int previousDocumentId = 0;
                for (int documentId : documentIds) {
                    position += osPostings.write(documentId - previousDocumentId);
                    previousDocumentId = documentId;
                }
            }
        }
    }

    private PriorityQueue<Block> initBlockQueue() {
        PriorityQueue<Block> queue = new PriorityQueue<>();
        for (int i = 0; i < blockId; i++) {
            Block block = new Block(BLOCK_FILE_PREFIX + i);
            if (block.advance()) queue.add(block);
            else block.close();
        }
        return queue;
    }

    private void merge(LinkedList<Integer> left, List<Integer> right) {
        ListIterator<Integer> leftIterator = left.listIterator();
        outer:
        for (int rightId : right) {
            while (leftIterator.hasNext()) {
                int leftId = leftIterator.next();
                if (leftId == rightId)
                    continue outer;
                else if (leftId > rightId) {
                    leftIterator.previous();
                    break;
                }
            }
            leftIterator.add(rightId);
        }
    }

    @SneakyThrows
    private void deleteBlocks() {
        for (int i = 0; i < blockId; i++)
            Files.delete(Path.of(path(BLOCK_FILE_PREFIX + i)));
    }

    private void reset() {
        documentsIterator = null;
        blockId = 0;
    }

    private synchronized int nextBlockId() {
        return blockId++;
    }

    private synchronized DocumentInfo nextDocument() {
        if (documentsIterator.hasNext())
            return documentsIterator.next();
        return null;
    }

    private String path(String fileName) {
        return path.resolve(fileName).toString();
    }

    @SneakyThrows
    private EncodedOutputStream os(String fileName) {
        return encodedOutputStreamFactory.apply(new BufferedOutputStream(new FileOutputStream(path(fileName), false)));
    }

    @SneakyThrows
    private EncodedInputStream is(String fileName) {
        return encodedInputStreamFactory.apply(new BufferedInputStream(new FileInputStream(path(fileName))));
    }

    private record DocumentInfo(Document document, int id) {}

    private class InverterThread extends Thread {

        private static final int MIN_BLOCK_SIZE = 5_000_000;
        private static final int POSTING_SIZE = 16;
        private final Tokenizer tokenizer;
        private final long minMemoryThreshold;

        public InverterThread(Tokenizer tokenizer, long minMemoryThreshold) {
            this.tokenizer = tokenizer;
            this.minMemoryThreshold = minMemoryThreshold - MIN_BLOCK_SIZE;
        }

        @Override
        public void run() {
            Map<String, List<Integer>> block = new HashMap<>();
            int blockSize = 0;
            while (true) {
                DocumentInfo documentInfo = nextDocument();
                if (documentInfo == null) break;
                if (blockSize >= MIN_BLOCK_SIZE && freeMemory() - documentInfo.document().getSize() < minMemoryThreshold) {
                    flushBlock(block);
                    block = new HashMap<>();
                    blockSize = 0;
                }
                int documentId = documentInfo.id();
                Iterator<String> terms = tokenizer.tokenizeAsStream(documentInfo.document()).iterator();
                while (terms.hasNext()) {
                    String term = terms.next();
                    List<Integer> postingList = block.computeIfAbsent(term, _ -> new ArrayList<>());
                    if (postingList.isEmpty() || !postingList.getLast().equals(documentId)) {
                        postingList.add(documentId);
                        blockSize += POSTING_SIZE;
                    }
                }
            }
            if (!block.isEmpty()) flushBlock(block);
        }

        @SneakyThrows
        private void flushBlock(Map<String, List<Integer>> block) {
            List<String> terms = block.keySet().stream().sorted().toList();
            try (EncodedOutputStream os = os(BLOCK_FILE_PREFIX + nextBlockId())) {
                for (String term : terms) {
                    List<Integer> documentIds = block.get(term);
                    os.write(term);
                    os.write(documentIds.size());
                    for (int documentId : documentIds)
                        os.write(documentId);
                }
            }
        }
    }

    private class Block implements Comparable<Block>, Closeable {

        private final EncodedInputStream is;
        private String currentTerm;
        private List<Integer> currentDocumentIds;

        @SneakyThrows
        public Block(String fileName) {
            is = is(fileName);
        }

        public String currentTerm() {
            return currentTerm;
        }

        public List<Integer> currentDocumentIds() {
            return currentDocumentIds;
        }

        @SneakyThrows
        public boolean advance() {
            if (is.available() < 4) return false;

            currentTerm = is.readString();

            int documentIdsCount = is.readInt();
            currentDocumentIds = new ArrayList<>(documentIdsCount);
            for (int i = 0; i < documentIdsCount; i++)
                currentDocumentIds.add(is.readInt());

            return true;
        }

        @Override
        @SneakyThrows
        public void close() {
            is.close();
        }

        @Override
        public int compareTo(Block o) {
            return currentTerm().compareTo(o.currentTerm());
        }
    }
}
