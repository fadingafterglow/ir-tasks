package structure.document.disk;

import encoders.EncodedInputStream;
import encoders.NotEncodedInputStream;
import lombok.SneakyThrows;
import structure.document.TfAwareIndex;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class TfAwareOnDiskInvertedIndex extends OnDiskInvertedIndex implements TfAwareIndex {

    private TfAwareOnDiskInvertedIndex(Path indexDirectory, Tokenizer tokenizer, int zonesCount,
                                       Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        super(indexDirectory, tokenizer, zonesCount, encodedInputStreamFactory);
    }

    @Override
    protected List<Integer> extractIds(EncodedInputStream is, PostingListInfo info) {
        List<Integer> result = new ArrayList<>(info.frequency());
        int previousId = 0;
        for (int i = 0; i < info.frequency(); i++) {
            previousId += is.readInt();
            is.readInt(); // skip frequency
            result.add(previousId);
        }
        return result;
    }

    @Override
    @SneakyThrows
    public List<TfAwareIndex.Entry> getEntries(String term) {
        int index = getPostingListInfoIndex(term);
        if (index == -1) return List.of();
        return getEntries(index);
    }

    @SneakyThrows
    public List<TfAwareIndex.Entry> getEntries(int termId) {
        PostingListInfo info = postingListInfos.get(termId);
        byte[] list = readPostingList(termId, info);
        try (EncodedInputStream is = encodedInputStreamFactory.apply(new ByteArrayInputStream(list))) {
            List<TfAwareIndex.Entry> result = new ArrayList<>(info.frequency());
            int previousId = 0;
            for (int i = 0; i < info.frequency(); i++) {
                previousId += is.readInt();
                int frequency = is.readInt();
                result.add(new Entry(previousId, frequency));
            }
            return result;
        }
    }

    @Override
    public double getIdf(String term) {
        return calculateIdf(getDocumentFrequency(term));
    }

    @Override
    public double getIdf(int termId) {
        return calculateIdf(postingListInfos.get(termId).frequency());
    }

    private double calculateIdf(int documentFrequency) {
        return Math.log((double)documentsCount() / documentFrequency);
    }

    private static final class Entry implements TfAwareIndex.Entry {
        private final int id;
        private final int frequency;

        private Entry(int id, int frequency) {
            this.id = id;
            this.frequency = frequency;
        }

        @Override
        public int getDocumentId() {
            return id;
        }

        @Override
        public int getTermFrequency() {
            return frequency;
        }
    }

    public static Builder builder(Path indexDirectory) {
        return new Builder(indexDirectory);
    }

    public static Builder builder(String indexDirectory) {
        return new Builder(indexDirectory);
    }

    public static class Builder {
        private final Path indexDirectory;
        private Tokenizer tokenizer = new DefaultTokenizer();
        private int zonesCount = 1;
        private Function<InputStream, EncodedInputStream> encodedInputStreamFactory = NotEncodedInputStream::new;

        private Builder(Path indexDirectory) {
            this.indexDirectory = indexDirectory;
        }

        private Builder(String indexDirectory) {
            this.indexDirectory = Path.of(indexDirectory);
        }

        public Builder tokenizer(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
            return this;
        }

        public Builder zonesCount(int zonesCount) {
            this.zonesCount = zonesCount;
            return this;
        }

        public Builder encodedInputStreamFactory(Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
            this.encodedInputStreamFactory = encodedInputStreamFactory;
            return this;
        }

        public TfAwareOnDiskInvertedIndex build() {
            return new TfAwareOnDiskInvertedIndex(indexDirectory, tokenizer, zonesCount, encodedInputStreamFactory);
        }
    }
}
