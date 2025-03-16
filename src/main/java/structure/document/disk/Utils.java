package structure.document.disk;

import java.nio.charset.StandardCharsets;

public abstract class Utils {

    public static long freeMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
    }

    public static byte[] intToBytes(int value) {
        return intToBytes(value, new byte[4]);
    }

    public static byte[] intToBytes(int value, byte[] buffer) {
        buffer[0] = (byte) (value >>> 24);
        buffer[1] = (byte) (value >>> 16);
        buffer[2] = (byte) (value >>> 8);
        buffer[3] = (byte) value;
        return buffer;
    }

    public static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, 0);
    }

    public static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
                ((bytes[offset + 1] & 0xFF) << 16) |
                ((bytes[offset + 2] & 0xFF) << 8) |
                (bytes[offset + 3] & 0xFF);
    }

    public static byte[] longToBytes(long value) {
        return longToBytes(value, new byte[8]);
    }

    public static byte[] longToBytes(long value, byte[] buffer) {
        buffer[0] = (byte) (value >>> 56);
        buffer[1] = (byte) (value >>> 48);
        buffer[2] = (byte) (value >>> 40);
        buffer[3] = (byte) (value >>> 32);
        buffer[4] = (byte) (value >>> 24);
        buffer[5] = (byte) (value >>> 16);
        buffer[6] = (byte) (value >>> 8);
        buffer[7] = (byte) value;
        return buffer;
    }

    public static long bytesToLong(byte[] bytes) {
        return bytesToLong(bytes, 0);
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFFL) << 56) |
                ((bytes[offset + 1] & 0xFFL) << 48) |
                ((bytes[offset + 2] & 0xFFL) << 40) |
                ((bytes[offset + 3] & 0xFFL) << 32) |
                ((bytes[offset + 4] & 0xFFL) << 24) |
                ((bytes[offset + 5] & 0xFFL) << 16) |
                ((bytes[offset + 6] & 0xFFL) << 8) |
                (bytes[offset + 7] & 0xFFL);
    }

    public static byte[] stringToBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
