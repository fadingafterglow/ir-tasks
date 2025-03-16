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
    private Map<String, Integer> documentsMap;
    private int blockId = 0;

    @SneakyThrows
    public SPIMIIndexer(String path) {
        this.path = Path.of(path);
        Files.createDirectories(this.path);
    }

    @Override
    public void index(List<Document> documents, Tokenizer tokenizer) {
        if (documents.isEmpty()) return;
        documentsMap = buildDocumentsMap(documents);
        buildBlocks(documents, tokenizer);
        mergeBlocks();
        deleteBlocks();
    }

    @SneakyThrows
    private Map<String, Integer> buildDocumentsMap(List<Document> documents) {
        HashMap<String, Integer> map = new HashMap<>();
        int id = 0;
        try (PrintWriter os = new PrintWriter(os(DOCUMENTS_MAP_FILE_NAME), false, StandardCharsets.UTF_8)) {
            for (Document document : documents) {
                map.put(document.getName(), id);
                os.println(document.getName());
                id++;
            }
        }
        return map;
    }

    @SneakyThrows
    private void buildBlocks(List<Document> documents, Tokenizer tokenizer) {
        int numberOfThreads = Math.min(MAX_NUMBER_OF_THREADS, documents.size());
        long minMemoryThreshold = freeMemory() / (numberOfThreads + 1);
        int splitSize = documents.size() / numberOfThreads;
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            int from = i * splitSize;
            int to = i == numberOfThreads - 1 ? documents.size() : (i + 1) * splitSize;
            threads[i] = new InverterThread(documents.subList(from, to), tokenizer, minMemoryThreshold);
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @SneakyThrows
    private void mergeBlocks() {
        PriorityQueue<Block> queue = new PriorityQueue<>();
        for (int i = 0; i < blockId; i++) {
            Block block = new Block(BLOCK_FILE_PREFIX + i);
            if (block.advance()) queue.add(block);
            else block.close();
        }
        long position = 0;
        try (BufferedOutputStream osPostings = os(POSTINGS_FILE_NAME);
             BufferedOutputStream osVocabulary = os(VOCABULARY_FILE_NAME)) {
            byte[] intBuffer = new byte[4];
            byte[] longBuffer = new byte[8];
            while (!queue.isEmpty()) {
                Block block = queue.poll();
                String term = block.currentTerm();
                List<Integer> documentIds = block.currentDocumentIds();
                while (!queue.isEmpty() && queue.peek().currentTerm().equals(term)) {
                    Block nextBlock = queue.poll();
                    documentIds = merge(documentIds, nextBlock.currentDocumentIds());
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

    @SneakyThrows
    private void deleteBlocks() {
        for (int i = 0; i < blockId; i++)
            Files.delete(Path.of(path(BLOCK_FILE_PREFIX + i)));
    }

    private List<Integer> merge(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).equals(right.get(r))) {
                result.add(left.get(l));
                l++;
                r++;
            } else if (left.get(l) < right.get(r)) {
                result.add(left.get(l));
                l++;
            } else {
                result.add(right.get(r));
                r++;
            }
        }
        while (l < left.size())
            result.add(left.get(l++));
        while (r < right.size())
            result.add(right.get(r++));
        return result;
    }

    private synchronized int nextBlockId() {
        return blockId++;
    }

    private int getDocumentId(String documentName) {
        return documentsMap.get(documentName);
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

    private class InverterThread extends Thread {

        private final Collection<Document> documents;
        private final Tokenizer tokenizer;
        private final long minMemoryThreshold;

        public InverterThread(Collection<Document> documents, Tokenizer tokenizer, long minMemoryThreshold) {
            this.documents = documents;
            this.tokenizer = tokenizer;
            this.minMemoryThreshold = minMemoryThreshold;
        }

        @Override
        public void run() {
            Map<String, List<Integer>> block = new HashMap<>();
            for (Document document : documents) {
                if (freeMemory() - document.getSize() < minMemoryThreshold) {
                    flushBlock(block);
                    block = new HashMap<>();
                }
                int documentId = getDocumentId(document.getName());
                Iterator<String> terms = tokenizer.tokenizeAsStream(document).iterator();
                while (terms.hasNext()) {
                    if (freeMemory() < minMemoryThreshold) {
                        flushBlock(block);
                        block = new HashMap<>();
                    }
                    String term = terms.next();
                    List<Integer> postingList = block.computeIfAbsent(term, _ -> new ArrayList<>());
                    if (postingList.isEmpty() || !postingList.getLast().equals(documentId))
                        postingList.add(documentId);
                }
            }
            flushBlock(block);
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
