package encoders;

import java.io.Closeable;

public interface EncodedOutputStream extends Closeable {
    int write(int value);
    int write(long value);
    int write(String value);
}
