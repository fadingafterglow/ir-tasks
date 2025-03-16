package structure.document.disk;

import lombok.SneakyThrows;
import structure.document.Index;
import tokenizer.Tokenizer;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static structure.document.disk.Utils.*;

public class UncompressedInvertedIndex implements Index, Closeable {

    private final Tokenizer tokenizer;
    private final Map<String, PostingListInfo> index;
    private final List<String> documentsMap;
    private final FileChannel postings;

    public UncompressedInvertedIndex(Path indexDirectory, Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        documentsMap = loadDocumentsMap(indexDirectory);
        index = loadIndex(indexDirectory);
        postings = initPostings(indexDirectory);
    }

    @SneakyThrows
    private List<String> loadDocumentsMap(Path indexDirectory) {
        return new ArrayList<>(Files.readAllLines(indexDirectory.resolve(Indexer.DOCUMENTS_MAP_FILE_NAME)));
    }

    @SneakyThrows
    private Map<String, PostingListInfo> loadIndex(Path indexDirectory) {
        Map<String, PostingListInfo> index = new HashMap<>();
        String previousTerm = null;
        long previousPosition = 0;
        byte[] intBuffer = new byte[4];
        byte[] longBuffer = new byte[8];
        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(indexDirectory.resolve(Indexer.VOCABULARY_FILE_NAME)))) {
            while (is.available() > 12) {
                is.read(intBuffer);
                int termLength = bytesToInt(intBuffer);
                String term = bytesToString(is.readNBytes(termLength));
                is.read(longBuffer);
                long position = bytesToLong(longBuffer);
                if (previousTerm != null)
                    index.put(previousTerm, new PostingListInfo((int) ((position - previousPosition) / 4), previousPosition));
                previousTerm = term;
                previousPosition = position;
            }
        }
        if (previousTerm != null) {
            long postingsSize = Files.size(indexDirectory.resolve(Indexer.POSTINGS_FILE_NAME));
            index.put(previousTerm, new PostingListInfo((int) ((postingsSize - previousPosition) / 4), previousPosition));
        }
        return index;
    }

    @SneakyThrows
    private FileChannel initPostings(Path indexDirectory) {
        return FileChannel.open(indexDirectory.resolve(Indexer.POSTINGS_FILE_NAME));
    }

    @Override
    public int documentsCount() {
        return documentsMap.size();
    }

    @Override
    public int termsCount() {
        return index.size();
    }

    @Override
    public String getDocumentName(int id) {
        if (id < 0 || id >= documentsMap.size())
            return null;
        return documentsMap.get(id);
    }

    @Override
    @SneakyThrows
    public List<Integer> getDocumentIds(String term) {
        PostingListInfo info = index.get(term);
        if (info == null) return List.of();
        byte[] ids = new byte[info.frequency() * 4];
        ByteBuffer buffer = ByteBuffer.wrap(ids).order(ByteOrder.BIG_ENDIAN);
        postings.read(buffer, info.position());
        buffer.flip();
        List<Integer> result = new ArrayList<>(info.frequency());
        for (int i = 0; i < info.frequency(); i++)
            result.add(buffer.getInt());
        return result;
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public List<Integer> getAllDocumentIds() {
        return Stream.iterate(0, x -> x < documentsMap.size(), x -> x + 1).toList();
    }

    @Override
    public int getDocumentFrequency(String term) {
        PostingListInfo info = index.get(term);
        return info == null ? 0 : info.frequency();
    }

    @Override
    @SneakyThrows
    public void close() {
        postings.close();
    }

    private record PostingListInfo(int frequency, long position) {}
}
