package encoders;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class VBEncodedOutputStream implements EncodedOutputStream {

    private final OutputStream os;
    private byte[] buffer;

    public VBEncodedOutputStream(OutputStream os) {
        this.os = os;
        buffer = new byte[9];
    }

    @Override
    @SneakyThrows
    public int write(int value) {
        return write((long) value);
    }

    @Override
    @SneakyThrows
    public int write(long value) {
        int bytes = 0;
        buffer[bytes++] = (byte) ((value & 0x7F) | 0x80);
        while ((value >>>= 7) != 0) {
            buffer[bytes++] = (byte) (value & 0x7F);
        }
        for (int i = bytes - 1; i >= 0; i--)
            os.write(buffer, i, 1);
        return bytes;
    }

    @Override
    @SneakyThrows
    public int write(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int written = write(bytes.length);
        os.write(bytes);
        return bytes.length + written;
    }

    @Override
    public void close() throws IOException {
        os.close();
        buffer = null;
    }
}
