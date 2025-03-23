package encoders;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class NotEncodedInputStream implements EncodedInputStream {

    private final InputStream is;
    private byte[] intBuffer;
    private byte[] longBuffer;

    public NotEncodedInputStream(InputStream is) {
        this.is = is;
        intBuffer = new byte[4];
        longBuffer = new byte[8];
    }

    @Override
    @SneakyThrows
    public int available() {
        return is.available();
    }

    @Override
    @SneakyThrows
    public int readInt() {
        is.read(intBuffer);
        return bytesToInt();
    }

    @Override
    @SneakyThrows
    public long readLong() {
        is.read(longBuffer);
        return bytesToLong();
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
