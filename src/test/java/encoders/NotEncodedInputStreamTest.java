package encoders;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;


public class NotEncodedInputStreamTest extends BaseEncodedInputStreamTest{

    public NotEncodedInputStreamTest() {
        super(NotEncodedInputStream::new);
    }

    private static Stream<Arguments> testReadInt() {
        return Stream.of(
                Arguments.of(new byte[] { 0, 0, 0, 0 }, 0),
                Arguments.of(new byte[] { 0, 0, 0, 1}, 1),
                Arguments.of(new byte[] { 0, 0, 0, 23 }, 23),
                Arguments.of(new byte[] { 0, 0, 13, 125 }, 3453),
                Arguments.of(new byte[] { 0, 35, (byte)233, 14 }, 2353422)
        );
    }

    private static Stream<Arguments> testReadLong() {
        return Stream.of(
                Arguments.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0),
                Arguments.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 1}, 1),
                Arguments.of(new byte[] { 0, 0, 0, 0, 0, 0, 0, 23 }, 23),
                Arguments.of(new byte[] { 0, 0, 0, 0, 0, 0, 13, 125 }, 3453),
                Arguments.of(new byte[] { 0, 0, 0, 0, 0, 35, (byte)233, 14 }, 2353422),
                Arguments.of(new byte[] { 0, 0, 111, (byte)252, (byte)238, 117, (byte)173, (byte)185 }, 123132123131321L)
        );
    }

    private static Stream<Arguments> testReadString() {
        return Stream.of(
                Arguments.of(new byte[] { 0, 0, 0, 3, 0x71, 0x77, 0x65 }, "qwe"),
                Arguments.of(new byte[] { 0, 0, 0, 8, (byte)0xd0, (byte)0xb0, (byte)0xd0, (byte)0xb1, (byte)0xd0, (byte)0xb2, (byte)0xd0, (byte)0xb3 }, "абвг")
        );
    }
}
