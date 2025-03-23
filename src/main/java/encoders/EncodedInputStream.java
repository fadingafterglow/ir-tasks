package encoders;

import java.io.Closeable;

public interface EncodedInputStream extends Closeable {
    boolean eofReached();
    void read(byte[] buffer, int offset, int length);
    int readInt();
    long readLong();
    String readString();
}
