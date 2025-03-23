package encoders;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class NotEncodedOutputStream implements EncodedOutputStream {

    private final OutputStream os;
    private byte[] intBuffer;
    private byte[] longBuffer;

    public NotEncodedOutputStream(OutputStream os) {
        this.os = os;
        intBuffer = new byte[4];
        longBuffer = new byte[8];
    }

    @Override
    @SneakyThrows
    public int write(int value) {
        intToBytes(value);
        os.write(intBuffer);
        return 4;
    }

    @Override
    @SneakyThrows
    public int write(long value) {
        longToBytes(value);
        os.write(longBuffer);
        return 8;
    }

    @Override
    @SneakyThrows
    public int write(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        write(bytes.length);
        os.write(bytes);
        return bytes.length + 4;
    }

    @Override
    public void close() throws IOException {
        os.close();
        intBuffer = null;
        longBuffer = null;
    }

    private void intToBytes(int value) {
        intBuffer[0] = (byte) (value >>> 24);
        intBuffer[1] = (byte) (value >>> 16);
        intBuffer[2] = (byte) (value >>> 8);
        intBuffer[3] = (byte) value;
    }

    private void longToBytes(long value) {
        longBuffer[0] = (byte) (value >>> 56);
        longBuffer[1] = (byte) (value >>> 48);
        longBuffer[2] = (byte) (value >>> 40);
        longBuffer[3] = (byte) (value >>> 32);
        longBuffer[4] = (byte) (value >>> 24);
        longBuffer[5] = (byte) (value >>> 16);
        longBuffer[6] = (byte) (value >>> 8);
        longBuffer[7] = (byte) value;
    }
}
