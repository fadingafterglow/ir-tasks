package structure.document.disk;

import document.Document;
import encoders.*;
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
    private final int zonesCount;
    private final Function<OutputStream, EncodedOutputStream> encodedOutputStreamFactory;
    private final Function<InputStream, EncodedInputStream> encodedInputStreamFactory;
    private final Function<Integer, OutBlock> outBlockFactory;
    private final Function<EncodedInputStream, InBlock> inBlockFactory;

    private Iterator<DocumentInfo> documentsIterator;
    private int blockId;

    @SneakyThrows
    private SPIMIIndexer(Path path, int zonesCount,
                        Function<OutputStream, EncodedOutputStream> encodedOutputStreamFactory,
                        Function<InputStream, EncodedInputStream> encodedInputStreamFactory,
                        Function<Integer, OutBlock> outBlockFactory,
                        Function<EncodedInputStream, InBlock> inBlockFactory)
    {
        Files.createDirectories(path);
        this.path = path;
        this.zonesCount = zonesCount;
        this.encodedOutputStreamFactory = encodedOutputStreamFactory;
        this.encodedInputStreamFactory = encodedInputStreamFactory;
        this.outBlockFactory = outBlockFactory;
        this.inBlockFactory = inBlockFactory;
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
        PriorityQueue<InBlock> queue = initBlockQueue();
        long position = 0;
        try (EncodedOutputStream osPostings = os(POSTINGS_FILE_NAME);
             VocabularyEncoder osVocabulary = new VocabularyFrontEncoder(os(VOCABULARY_STRING_FILE_NAME), os(VOCABULARY_TABLE_FILE_NAME))) {
            while (!queue.isEmpty()) {
                InBlock block = queue.poll();
                InBlock.MergeResult mergeResult = block.toMergeResult();
                while (!queue.isEmpty() && mergeResult.shouldMerge(queue.peek())) {
                    InBlock nextBlock = queue.poll();
                    mergeResult.merge(nextBlock);
                    if (nextBlock.advance()) queue.add(nextBlock);
                    else nextBlock.close();
                }
                if (block.advance()) queue.add(block);
                else block.close();
                position = mergeResult.save(position, osPostings, osVocabulary);
            }
        }
    }

    @SneakyThrows
    private PriorityQueue<InBlock> initBlockQueue() {
        PriorityQueue<InBlock> queue = new PriorityQueue<>();
        for (int i = 0; i < blockId; i++) {
            InBlock block = inBlockFactory.apply(is(BLOCK_FILE_PREFIX + i));
            if (block.advance()) queue.add(block);
            else block.close();
        }
        return queue;
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

        private static final int MIN_BLOCK_SIZE = 10_000_000;
        private final Tokenizer tokenizer;
        private final long minMemoryThreshold;

        public InverterThread(Tokenizer tokenizer, long minMemoryThreshold) {
            this.tokenizer = tokenizer;
            this.minMemoryThreshold = minMemoryThreshold - MIN_BLOCK_SIZE;
        }

        @Override
        public void run() {
            OutBlock block = outBlockFactory.apply(zonesCount);
            while (true) {
                DocumentInfo documentInfo = nextDocument();
                if (documentInfo == null) break;
                if (block.size() >= MIN_BLOCK_SIZE && freeMemory() - documentInfo.document().getSize() < minMemoryThreshold) {
                    flushBlock(block);
                    block = outBlockFactory.apply(zonesCount);
                }
                int documentId = documentInfo.id() * zonesCount;
                int zoneId = 0;
                for (String zone : documentInfo.document().getZones()) {
                    final OutBlock b = block;
                    final int id = documentId + zoneId;
                    tokenizer.tokenizeAsStream(zone).forEach(t -> b.add(t, id));
                    if (++zoneId >= zonesCount) break;
                }
            }
            if (!block.isEmpty()) flushBlock(block);
        }

        @SneakyThrows
        private void flushBlock(OutBlock block) {
            try (EncodedOutputStream os = os(BLOCK_FILE_PREFIX + nextBlockId())) {
                block.flush(os);
            }
        }
    }

    public static Builder builder(Path path) {
        return new Builder(path);
    }

    public static Builder builder(String path) {
        return new Builder(path);
    }

    public static class Builder {
        private final Path path;
        private int zonesCount = 1;
        private Function<OutputStream, EncodedOutputStream> encodedOutputStreamFactory = NotEncodedOutputStream::new;
        private Function<InputStream, EncodedInputStream> encodedInputStreamFactory = NotEncodedInputStream::new;
        private Function<Integer, OutBlock> outBlockFactory = DefaultOutBlock::new;
        private Function<EncodedInputStream, InBlock> inBlockFactory = DefaultInBlock::new;

        private Builder(Path path) {
            this.path = path;
        }

        private Builder(String path) {
            this.path = Path.of(path);
        }

        public Builder zonesCount(int zonesCount) {
            this.zonesCount = zonesCount;
            return this;
        }

        public Builder encodedOutputStreamFactory(Function<OutputStream, EncodedOutputStream> encodedOutputStreamFactory) {
            this.encodedOutputStreamFactory = encodedOutputStreamFactory;
            return this;
        }

        public Builder encodedInputStreamFactory(Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
            this.encodedInputStreamFactory = encodedInputStreamFactory;
            return this;
        }

        public Builder outBlockFactory(Function<Integer, OutBlock> outBlockFactory) {
            this.outBlockFactory = outBlockFactory;
            return this;
        }

        public Builder inBlockFactory(Function<EncodedInputStream, InBlock> inBlockFactory) {
            this.inBlockFactory = inBlockFactory;
            return this;
        }

        public SPIMIIndexer build() {
            return new SPIMIIndexer(path, zonesCount, encodedOutputStreamFactory, encodedInputStreamFactory, outBlockFactory, inBlockFactory);
        }
    }
}
