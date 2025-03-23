package encoders;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NotEncodedInputStream implements EncodedInputStream {

    private final InputStream is;
    private boolean eofReached;
    private byte[] intBuffer;
    private byte[] longBuffer;

    public NotEncodedInputStream(InputStream is) {
        this.is = is;
        intBuffer = new byte[4];
        longBuffer = new byte[8];
    }

    @Override
    public boolean eofReached() {
        return eofReached;
    }

    @Override
    @SneakyThrows
    public void read(byte[] buffer, int offset, int length) {
        if (is.read(buffer, offset, length) == -1)
            eofReached = true;
    }

    @Override
    @SneakyThrows
    public int readInt() {
        if (is.read(intBuffer) == -1) {
            eofReached = true;
            return 0;
        }
        return bytesToInt();
    }

    @Override
    @SneakyThrows
    public long readLong() {
        if (is.read(longBuffer) == -1) {
            eofReached = true;
            return 0;
        }
        return bytesToLong();
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
        intBuffer = null;
        longBuffer = null;
    }

    private int bytesToInt() {
        return ((intBuffer[0] & 0xFF) << 24) |
                ((intBuffer[1] & 0xFF) << 16) |
                ((intBuffer[2] & 0xFF) << 8) |
                (intBuffer[3] & 0xFF);
    }

    private long bytesToLong() {
        return ((longBuffer[0] & 0xFFL) << 56) |
                ((longBuffer[1] & 0xFFL) << 48) |
                ((longBuffer[2] & 0xFFL) << 40) |
                ((longBuffer[3] & 0xFFL) << 32) |
                ((longBuffer[4] & 0xFFL) << 24) |
                ((longBuffer[5] & 0xFFL) << 16) |
                ((longBuffer[6] & 0xFFL) << 8) |
                (longBuffer[7] & 0xFFL);
    }
}
