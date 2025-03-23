package encoders;

import java.io.Closeable;

public interface EncodedInputStream extends Closeable {
    int available();
    int readInt();
    long readLong();
    String readString();
}
