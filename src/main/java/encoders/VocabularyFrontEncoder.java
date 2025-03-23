package encoders;

import lombok.SneakyThrows;

public class VocabularyFrontEncoder implements VocabularyEncoder {

    public static final int TERMS_PER_BLOCK = 4;

    private final EncodedOutputStream osString;
    private final EncodedOutputStream osTable;

    private String[] currentBlock;
    private int currentBlockSize;
    private int currentPosition;

    public VocabularyFrontEncoder(EncodedOutputStream osString, EncodedOutputStream osTable) {
        this.osString = osString;
        this.osTable = osTable;
        currentBlock = new String[TERMS_PER_BLOCK];
    }

    public void write(String term, int frequency, long position) {
        if (currentBlockSize == 0)
            osTable.write(currentPosition);
        osTable.write(frequency);
        osTable.write(position);
        currentBlock[currentBlockSize++] = term;
        if (currentBlockSize == TERMS_PER_BLOCK)
            flushBlock();
    }

    private void flushBlock() {
        int prefixLength = commonPrefixLength();
        currentPosition += osString.write(currentBlock[0].substring(0, prefixLength));
        for (int i = 0; i < currentBlockSize; i++)
            currentPosition += osString.write(currentBlock[i].substring(prefixLength));
        currentBlockSize = 0;
    }

    private int commonPrefixLength() {
        int prefixLength = currentBlock[0].length();
        for (int i = 1; i < currentBlockSize; i++) {
            int possibleLength = Math.min(prefixLength, currentBlock[i].length());
            for (prefixLength = 0; prefixLength < possibleLength; prefixLength++) {
                if (currentBlock[0].charAt(prefixLength) != currentBlock[i].charAt(prefixLength))
                    break;
            }
        }
        return prefixLength;
    }

    @Override
    @SneakyThrows
    public void close() {
        if (currentBlockSize > 0)
            flushBlock();
        currentBlock = null;
        osString.close();
        osTable.close();
    }
}