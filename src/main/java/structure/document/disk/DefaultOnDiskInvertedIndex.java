package structure.document.disk;

import encoders.EncodedInputStream;
import encoders.NotEncodedInputStream;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;


public class DefaultOnDiskInvertedIndex extends OnDiskInvertedIndex {

    private DefaultOnDiskInvertedIndex(Path indexDirectory, Tokenizer tokenizer, int zonesCount,
                                      Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        super(indexDirectory, tokenizer, zonesCount, encodedInputStreamFactory);
    }

    @Override
    protected List<Integer> extractIds(EncodedInputStream is, OnDiskInvertedIndex.PostingListInfo info) {
        List<Integer> result = new ArrayList<>(info.frequency());
        int previousId = 0;
        for (int i = 0; i < info.frequency(); i++) {
            previousId += is.readInt();
            result.add(previousId);
        }
        return result;
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

        public DefaultOnDiskInvertedIndex build() {
            return new DefaultOnDiskInvertedIndex(indexDirectory, tokenizer, zonesCount, encodedInputStreamFactory);
        }
    }
}
