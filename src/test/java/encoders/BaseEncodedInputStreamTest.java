package encoders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseEncodedInputStreamTest {

    protected final Function<InputStream, EncodedInputStream> supplier;

    public BaseEncodedInputStreamTest(Function<InputStream, EncodedInputStream> supplier) {
        this.supplier = supplier;
    }

    @ParameterizedTest
    @MethodSource
    public void testRead(byte[] input) {
        EncodedInputStream eis = supplier.apply(new ByteArrayInputStream(input));
        byte[] buffer = new byte[input.length];
        eis.read(buffer, 0, buffer.length);
        assertArrayEquals(input, buffer);
    }

    private static Stream<byte[]> testRead() {
        return Stream.of(
                new byte[] { 0, 0, 0, 0 },
                new byte[] { 0, 0, 0, 1},
                new byte[] { 0, 0, 0, 23 },
                new byte[] { 0, 0, 13, 125 },
                new byte[] { 0, 35, (byte)233, 14 }
        );
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
