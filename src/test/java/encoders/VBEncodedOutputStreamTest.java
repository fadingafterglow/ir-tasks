package encoders;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class VBEncodedOutputStreamTest extends BaseEncodedOutputStreamTest{

    public VBEncodedOutputStreamTest() {
        super(VBEncodedOutputStream::new);
    }

    private static Stream<Arguments> testWriteInt() {
        return Stream.of(
                Arguments.of(0, new byte[] { (byte)0b10000000 }),
                Arguments.of(1, new byte[] { (byte)0b10000001 }),
                Arguments.of(23, new byte[] { (byte)0b10010111 }),
                Arguments.of(3453, new byte[] { 0b00011010, (byte)0b11111101 }),
                Arguments.of(2353422, new byte[] { 0b00000001, 0b00001111, 0b01010010, (byte)0b10001110 })
        );
    }

    private static Stream<Arguments> testWriteLong() {
        return Stream.of(
                Arguments.of(0, new byte[] { (byte)0b10000000 }),
                Arguments.of(1, new byte[] { (byte)0b10000001 }),
                Arguments.of(23, new byte[] { (byte)0b10010111 }),
                Arguments.of(3453, new byte[] { 0b00011010, (byte)0b11111101 }),
                Arguments.of(2353422, new byte[] { 0b00000001, 0b00001111, 0b01010010, (byte)0b10001110 }),
                Arguments.of(123132123131321L, new byte[] { 0b00011011, 0b01111111, 0b01001110, 0b01110011, 0b01010110, 0b01011011, (byte)0b10111001 })
        );
    }

    private static Stream<Arguments> testWriteString() {
        return Stream.of(
                Arguments.of("qwe", new byte[] { (byte)0b10000011, 0x71, 0x77, 0x65 }),
                Arguments.of("абвг", new byte[] { (byte)0b10001000, (byte)0xd0, (byte)0xb0, (byte)0xd0, (byte)0xb1, (byte)0xd0, (byte)0xb2, (byte)0xd0, (byte)0xb3 })
        );
    }
}
