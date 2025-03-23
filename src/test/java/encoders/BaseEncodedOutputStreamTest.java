package encoders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseEncodedOutputStreamTest {

    protected final ByteArrayOutputStream os;
    protected final EncodedOutputStream eos;

    public BaseEncodedOutputStreamTest(Function<OutputStream, EncodedOutputStream> supplier) {
        this.os = new ByteArrayOutputStream();
        this.eos = supplier.apply(os);
    }

    @BeforeEach
    public void resetOutputStream() {
        os.reset();
    }

    @ParameterizedTest
    @MethodSource
    public void testWriteInt(int value, byte[] expected) {
        assertEquals(expected.length, eos.write(value));
        assertArrayEquals(expected, os.toByteArray());
    }

    @ParameterizedTest
    @MethodSource
    public void testWriteLong(long value, byte[] expected) {
        assertEquals(expected.length, eos.write(value));
        assertArrayEquals(expected, os.toByteArray());
    }

    @ParameterizedTest
    @MethodSource
    public void testWriteString(String value, byte[] expected) {
        assertEquals(expected.length, eos.write(value));
        assertArrayEquals(expected, os.toByteArray());
    }
}
