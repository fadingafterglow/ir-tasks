package structure.document.disk;

import encoders.EncodedInputStream;
import lombok.SneakyThrows;
import structure.document.Index;
import tokenizer.Tokenizer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;


public class OnDiskInvertedIndex implements Index, Closeable {

    private final Tokenizer tokenizer;
    private final Function<InputStream, EncodedInputStream> encodedInputStreamFactory;
    private final Map<String, PostingListInfo> index;
    private final List<String> documentsMap;
    private final FileChannel postings;

    public OnDiskInvertedIndex(Path indexDirectory, Tokenizer tokenizer, Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        this.tokenizer = tokenizer;
        this.encodedInputStreamFactory = encodedInputStreamFactory;
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
        int previousFrequency = 0;
        long previousPosition = 0;
        try (EncodedInputStream is = encodedInputStreamFactory.apply(new BufferedInputStream(Files.newInputStream(indexDirectory.resolve(Indexer.VOCABULARY_FILE_NAME))))) {
            while (is.available() > 12) {
                String term = is.readString();
                int frequency = is.readInt();
                long position = is.readLong();
                if (previousTerm != null)
                    index.put(previousTerm, new PostingListInfo(previousFrequency, previousPosition, (int)(position - previousPosition)));
                previousTerm = term;
                previousFrequency = frequency;
                previousPosition = position;
            }
        }
        if (previousTerm != null) {
            long postingsSize = Files.size(indexDirectory.resolve(Indexer.POSTINGS_FILE_NAME));
            index.put(previousTerm, new PostingListInfo(previousFrequency, previousPosition, (int)(postingsSize - previousPosition)));
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
        byte[] ids = new byte[info.size()];
        ByteBuffer buffer = ByteBuffer.wrap(ids);
        postings.read(buffer, info.position());
        try (EncodedInputStream is = encodedInputStreamFactory.apply(new ByteArrayInputStream(ids))) {
            List<Integer> result = new ArrayList<>(info.frequency());
            int previousId = 0;
            for (int i = 0; i < info.frequency(); i++) {
                previousId += is.readInt();
                result.add(previousId);
            }
            return result;
        }
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

    private record PostingListInfo(int frequency, long position, int size) {}
}
