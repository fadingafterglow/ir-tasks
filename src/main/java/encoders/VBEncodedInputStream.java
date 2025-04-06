package encoders;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class VBEncodedInputStream implements EncodedInputStream {

    private final InputStream is;
    private boolean eofReached;
    private byte[] buffer;

    public VBEncodedInputStream(InputStream is) {
        this.is = is;
        buffer = new byte[1];
    }

    @Override
    public boolean eofReached() {
        return eofReached;
    }

    @Override
    public void resetEof() {
        eofReached = false;
    }

    @Override
    @SneakyThrows
    public void read(byte[] buffer, int offset, int length) {
        if (is.read(buffer, offset, length) == -1)
            eofReached = true;
    }

    @Override
    public int readInt() {
        return (int) readLong();
    }

    @Override
    @SneakyThrows
    public long readLong() {
        long value = 0;
        do {
            if (is.read(buffer) == -1) {
                eofReached = true;
                return 0;
            }
            value <<= 7;
            value |= buffer[0] & 0x7F;
        } while ((buffer[0] & 0x80) == 0);
        return value;
    }

    @Override
    @SneakyThrows
    public String readString() {
        int length = readInt();
        byte[] bytes = is.readNBytes(length);
        if (bytes.length != length) {
            eofReached = true;
            return "";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        is.close();
        buffer = null;
    }
}
