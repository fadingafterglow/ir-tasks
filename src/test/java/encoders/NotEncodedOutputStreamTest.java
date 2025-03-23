package encoders;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class NotEncodedOutputStreamTest extends BaseEncodedOutputStreamTest{

    public NotEncodedOutputStreamTest() {
        super(NotEncodedOutputStream::new);
    }

    private static Stream<Arguments> testWriteInt() {
        return Stream.of(
                Arguments.of(0, new byte[] { 0, 0, 0, 0 }),
                Arguments.of(1, new byte[] { 0, 0, 0, 1}),
                Arguments.of(23, new byte[] { 0, 0, 0, 23 }),
                Arguments.of(3453, new byte[] { 0, 0, 13, 125 }),
                Arguments.of(2353422, new byte[] { 0, 35, (byte)233, 14 })
        );
    }

    private static Stream<Arguments> testWriteLong() {
        return Stream.of(
                Arguments.of(0, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }),
                Arguments.of(1, new byte[] { 0, 0, 0, 0, 0, 0, 0, 1}),
                Arguments.of(23, new byte[] { 0, 0, 0, 0, 0, 0, 0, 23 }),
                Arguments.of(3453, new byte[] { 0, 0, 0, 0, 0, 0, 13, 125 }),
                Arguments.of(2353422, new byte[] { 0, 0, 0, 0, 0, 35, (byte)233, 14 }),
                Arguments.of(123132123131321L, new byte[] { 0, 0, 111, (byte)252, (byte)238, 117, (byte)173, (byte)185 })
        );
    }

    private static Stream<Arguments> testWriteString() {
        return Stream.of(
                Arguments.of("qwe", new byte[] { 0, 0, 0, 3, 0x71, 0x77, 0x65 }),
                Arguments.of("абвг", new byte[] { 0, 0, 0, 8, (byte)0xd0, (byte)0xb0, (byte)0xd0, (byte)0xb1, (byte)0xd0, (byte)0xb2, (byte)0xd0, (byte)0xb3 })
        );
    }
}
