package structure.document.disk;

import document.Document;
import lombok.SneakyThrows;
import tokenizer.Tokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static structure.document.disk.Utils.*;

public class SPIMIIndexer implements Indexer {

    private static final int MAX_NUMBER_OF_THREADS = 10;
    private static final String BLOCK_FILE_PREFIX = "block-";
    private final Path path;
    private Iterator<DocumentInfo> documentsIterator;
    private int blockId;

    @SneakyThrows
    public SPIMIIndexer(String path) {
        this.path = Path.of(path);
        Files.createDirectories(this.path);
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
        try (PrintWriter os = new PrintWriter(os(DOCUMENTS_MAP_FILE_NAME), false, StandardCharsets.UTF_8)) {
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
        try (BufferedOutputStream osPostings = os(POSTINGS_FILE_NAME);
             BufferedOutputStream osVocabulary = os(VOCABULARY_FILE_NAME)) {
            byte[] intBuffer = new byte[4];
            byte[] longBuffer = new byte[8];
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
                for (int documentId : documentIds)
                    osPostings.write(intToBytes(documentId, intBuffer));
                byte[] termBytes = stringToBytes(term);
                osVocabulary.write(intToBytes(termBytes.length, intBuffer));
                osVocabulary.write(termBytes);
                osVocabulary.write(longToBytes(position, longBuffer));
                position += 4L * documentIds.size();
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
    private BufferedOutputStream os(String fileName) {
        return new BufferedOutputStream(new FileOutputStream(path(fileName), false));
    }

    @SneakyThrows
    private BufferedInputStream is(String fileName) {
        return new BufferedInputStream(new FileInputStream(path(fileName)));
    }

    private record DocumentInfo(Document document, int id) {}

    private class InverterThread extends Thread {

        private static final int MIN_TERMS_PER_BLOCK = 100000;
        private static final int AVERAGE_TERM_SIZE = 10;
        private final Tokenizer tokenizer;
        private final long minMemoryThreshold;

        public InverterThread(Tokenizer tokenizer, long minMemoryThreshold) {
            this.tokenizer = tokenizer;
            this.minMemoryThreshold = minMemoryThreshold - MIN_TERMS_PER_BLOCK * AVERAGE_TERM_SIZE;
        }

        @Override
        public void run() {
            Map<String, List<Integer>> block = new HashMap<>();
            while (true) {
                DocumentInfo documentInfo = nextDocument();
                if (documentInfo == null) break;
                if (freeMemory() - documentInfo.document().getSize() < minMemoryThreshold && block.size() >= MIN_TERMS_PER_BLOCK) {
                    flushBlock(block);
                    block = new HashMap<>();
                }
                int documentId = documentInfo.id();
                Iterator<String> terms = tokenizer.tokenizeAsStream(documentInfo.document()).iterator();
                while (terms.hasNext()) {
                    String term = terms.next();
                    List<Integer> postingList = block.computeIfAbsent(term, _ -> new ArrayList<>());
                    if (postingList.isEmpty() || !postingList.getLast().equals(documentId))
                        postingList.add(documentId);
                }
            }
            if (!block.isEmpty()) flushBlock(block);
        }

        @SneakyThrows
        private void flushBlock(Map<String, List<Integer>> block) {
            List<String> terms = block.keySet().stream().sorted().toList();
            try (BufferedOutputStream os = os(BLOCK_FILE_PREFIX + nextBlockId())) {
                byte[] intBuffer = new byte[4];
                for (String term : terms) {
                    byte[] termBytes = stringToBytes(term);
                    List<Integer> documentIds = block.get(term);
                    os.write(intToBytes(termBytes.length, intBuffer));
                    os.write(termBytes);
                    os.write(intToBytes(documentIds.size(), intBuffer));
                    for (int documentId : documentIds)
                        os.write(intToBytes(documentId, intBuffer));
                }
            }
        }
    }

    private class Block implements Comparable<Block>, Closeable {

        private final BufferedInputStream is;
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

            int termLength = bytesToInt(is.readNBytes(4));
            currentTerm = bytesToString(is.readNBytes(termLength));

            int documentIdsCount = bytesToInt(is.readNBytes(4));
            currentDocumentIds = new ArrayList<>(documentIdsCount);
            byte[] buffer = is.readNBytes(4 * documentIdsCount);
            for (int i = 0; i < documentIdsCount; i++)
                currentDocumentIds.add(bytesToInt(buffer, 4 * i));

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
