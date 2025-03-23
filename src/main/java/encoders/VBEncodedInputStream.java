package encoders;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class VBEncodedInputStream implements EncodedInputStream {

    private final InputStream is;
    private byte[] buffer;

    public VBEncodedInputStream(InputStream is) {
        this.is = is;
        buffer = new byte[1];
    }

    @Override
    @SneakyThrows
    public int available() {
        return is.available();
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
            is.read(buffer);
            value <<= 7;
            value |= buffer[0] & 0x7F;
        } while ((buffer[0] & 0x80) == 0);
        return value;
    }

    @Override
    @SneakyThrows
    public String readString() {
        byte[] bytes = is.readNBytes(readInt());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        is.close();
        buffer = null;
    }
}
