package encoders;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;


public class VBEncodedInputStreamTest extends BaseEncodedInputStreamTest{

    public VBEncodedInputStreamTest() {
        super(VBEncodedInputStream::new);
    }

    private static Stream<Arguments> testReadInt() {
        return Stream.of(
                Arguments.of(new byte[] { (byte)0b10000000 }, 0),
                Arguments.of(new byte[] { (byte)0b10000001 }, 1),
                Arguments.of(new byte[] { (byte)0b10010111 }, 23),
                Arguments.of(new byte[] { 0b00011010, (byte)0b11111101 }, 3453),
                Arguments.of(new byte[] { 0b00000001, 0b00001111, 0b01010010, (byte)0b10001110 }, 2353422)
        );
    }

    private static Stream<Arguments> testReadLong() {
        return Stream.of(
                Arguments.of(new byte[] { (byte)0b10000000 }, 0),
                Arguments.of(new byte[] { (byte)0b10000001 }, 1),
                Arguments.of(new byte[] { (byte)0b10010111 }, 23),
                Arguments.of(new byte[] { 0b00011010, (byte)0b11111101 }, 3453),
                Arguments.of(new byte[] { 0b00000001, 0b00001111, 0b01010010, (byte)0b10001110 }, 2353422),
                Arguments.of(new byte[] { 0b00011011, 0b01111111, 0b01001110, 0b01110011, 0b01010110, 0b01011011, (byte)0b10111001 }, 123132123131321L)
        );
    }

    private static Stream<Arguments> testReadString() {
        return Stream.of(
                Arguments.of(new byte[] { (byte)0b10000011, 0x71, 0x77, 0x65 }, "qwe"),
                Arguments.of(new byte[] { (byte)0b10001000, (byte)0xd0, (byte)0xb0, (byte)0xd0, (byte)0xb1, (byte)0xd0, (byte)0xb2, (byte)0xd0, (byte)0xb3 }, "абвг")
        );
    }
}
