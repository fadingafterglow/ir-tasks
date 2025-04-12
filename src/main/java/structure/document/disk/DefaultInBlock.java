package structure.document.disk;

import encoders.EncodedInputStream;
import encoders.EncodedOutputStream;
import encoders.VocabularyEncoder;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class DefaultInBlock implements InBlock {

    private final EncodedInputStream is;
    private String currentTerm;
    private int currentFrequency;
    private List<Integer> currentDocumentIds;

    public DefaultInBlock(EncodedInputStream is) {
        this.is = is;
    }

    @SneakyThrows
    public boolean advance() {
        currentTerm = is.readString();

        currentFrequency = is.readInt();

        int documentIdsCount = is.readInt();
        currentDocumentIds = new ArrayList<>(documentIdsCount);
        for (int i = 0; i < documentIdsCount; i++)
            currentDocumentIds.add(is.readInt());

        return !is.eofReached();
    }

    @Override
    public MergeResult toMergeResult() {
        return new DefaultInBlockMergeResult(this);
    }

    @SneakyThrows
    public void close() {
        is.close();
    }

    @Override
    public int compareTo(InBlock other) {
        if (other instanceof DefaultInBlock o)
            return currentTerm.compareTo(o.currentTerm);
        return -1;
    }

    private static class DefaultInBlockMergeResult implements MergeResult {

        private final String term;
        private int frequency;
        private final LinkedList<Integer> documentIds;

        public DefaultInBlockMergeResult(DefaultInBlock block) {
            this.term = block.currentTerm;
            this.frequency = block.currentFrequency;
            this.documentIds = new LinkedList<>(block.currentDocumentIds);
        }

        @Override
        public boolean shouldMerge(InBlock block) {
            return (block instanceof DefaultInBlock b) && term.equals(b.currentTerm);
        }

        @Override
        public void merge(InBlock block) {
            if (!(block instanceof DefaultInBlock b)) return;
            frequency += b.currentFrequency;
            merge(documentIds, b.currentDocumentIds);
        }

        private void merge(LinkedList<Integer> left, List<Integer> right) {
            ListIterator<Integer> leftIterator = left.listIterator();
            for (int rightId : right) {
                while (leftIterator.hasNext()) {
                    int leftId = leftIterator.next();
                    // ids in different blocks are never equal
                    if (leftId > rightId) {
                        leftIterator.previous();
                        break;
                    }
                }
                leftIterator.add(rightId);
            }
        }

        @Override
        public long save(long position, EncodedOutputStream osPostings, VocabularyEncoder osVocabulary) {
            osVocabulary.write(term, frequency, position);
            int previousDocumentId = 0;
            for (int documentId : documentIds) {
                position += osPostings.write(documentId - previousDocumentId);
                previousDocumentId = documentId;
            }
            return position;
        }
    }
}
