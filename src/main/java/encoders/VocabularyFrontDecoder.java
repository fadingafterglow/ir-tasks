package encoders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class VocabularyFrontDecoder implements VocabularyDecoder {

    private final SeekableByteArrayInputStream vocabularyString;
    private final EncodedInputStream is;

    public VocabularyFrontDecoder(byte[] vocabularyString, Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        this.vocabularyString = new SeekableByteArrayInputStream(vocabularyString);
        this.is = encodedInputStreamFactory.apply(this.vocabularyString);
    }

    @Override
    public void seek(int position) {
        vocabularyString.seek(position);
        is.resetEof();
    }

    @Override
    public byte[] readPrefix() {
        int prefixSize = is.readInt();
        if (is.eofReached()) return new byte[0];
        byte[] prefix = new byte[prefixSize];
        is.read(prefix, 0, prefixSize);
        return prefix;
    }

    @Override
    public String readTerm(byte[] prefix) {
        int termSize = is.readInt();
        if (is.eofReached()) return "";
        byte[] term = new byte[prefix.length + termSize];
        System.arraycopy(prefix, 0, term, 0, prefix.length);
        is.read(term, prefix.length, termSize);
        return new String(term, StandardCharsets.UTF_8);
    }

    private static class SeekableByteArrayInputStream extends ByteArrayInputStream {

        public SeekableByteArrayInputStream(byte[] buf) {
            super(buf);
        }

        public void seek(int position) {
            pos = position;
        }
    }
}
