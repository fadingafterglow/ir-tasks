package encoders;

import java.io.Closeable;

public interface VocabularyEncoder extends Closeable {

    void write(String term, int frequency, long position);
}
