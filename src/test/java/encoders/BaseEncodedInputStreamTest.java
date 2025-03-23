package encoders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseEncodedInputStreamTest {

    protected final Function<InputStream, EncodedInputStream> supplier;

    public BaseEncodedInputStreamTest(Function<InputStream, EncodedInputStream> supplier) {
        this.supplier = supplier;
    }

    @ParameterizedTest
    @MethodSource
    public void testReadInt(byte[] input, int expected) {
        EncodedInputStream eis = supplier.apply(new ByteArrayInputStream(input));
        assertEquals(expected, eis.readInt());
    }

    @ParameterizedTest
    @MethodSource
    public void testReadLong(byte[] input, long expected) {
        EncodedInputStream eis = supplier.apply(new ByteArrayInputStream(input));
        assertEquals(expected, eis.readLong());
    }

    @ParameterizedTest
    @MethodSource
    public void testReadString(byte[] input, String expected) {
        EncodedInputStream eis = supplier.apply(new ByteArrayInputStream(input));
        assertEquals(expected, eis.readString());
    }
}
