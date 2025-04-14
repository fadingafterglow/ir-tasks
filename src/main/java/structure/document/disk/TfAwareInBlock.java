package structure.document.disk;

import encoders.EncodedInputStream;
import encoders.EncodedOutputStream;
import encoders.VocabularyEncoder;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class TfAwareInBlock implements InBlock {

    private final EncodedInputStream is;
    private String currentTerm;
    private int currentFrequency;
    private List<DocumentInfo> currentInfos;

    public TfAwareInBlock(EncodedInputStream is) {
        this.is = is;
    }

    @SneakyThrows
    public boolean advance() {
        currentTerm = is.readString();

        currentFrequency = is.readInt();

        int infosCount = is.readInt();
        currentInfos = new ArrayList<>(infosCount);
        for (int i = 0; i < infosCount; i++) {
            int id = is.readInt();
            int frequency = is.readInt();
            currentInfos.add(new DocumentInfo(id, frequency));
        }

        return !is.eofReached();
    }

    @Override
    public MergeResult toMergeResult() {
        return new TfAwareBlockMergeResult(this);
    }

    @SneakyThrows
    public void close() {
        is.close();
    }

    @Override
    public int compareTo(InBlock other) {
        if (other instanceof TfAwareInBlock o)
            return currentTerm.compareTo(o.currentTerm);
        return -1;
    }

    private static class TfAwareBlockMergeResult implements MergeResult {

        private final String term;
        private int frequency;
        private final LinkedList<DocumentInfo> infos;

        public TfAwareBlockMergeResult(TfAwareInBlock block) {
            this.term = block.currentTerm;
            this.frequency = block.currentFrequency;
            this.infos = new LinkedList<>(block.currentInfos);
        }

        @Override
        public boolean shouldMerge(InBlock block) {
            return (block instanceof TfAwareInBlock b) && term.equals(b.currentTerm);
        }

        @Override
        public void merge(InBlock block) {
            if (!(block instanceof TfAwareInBlock b)) return;
            frequency += b.currentFrequency;
            merge(infos, b.currentInfos);
        }

        private void merge(LinkedList<DocumentInfo> left, List<DocumentInfo> right) {
            ListIterator<DocumentInfo> leftIterator = left.listIterator();
            for (DocumentInfo r : right) {
                while (leftIterator.hasNext()) {
                    DocumentInfo l = leftIterator.next();
                    // ids in different blocks are never equal
                    if (l.id > r.id) {
                        leftIterator.previous();
                        break;
                    }
                }
                leftIterator.add(r);
            }
        }

        @Override
        public long save(long position, EncodedOutputStream osPostings, VocabularyEncoder osVocabulary) {
            osVocabulary.write(term, frequency, position);
            int previousDocumentId = 0;
            for (DocumentInfo info : infos) {
                position += osPostings.write(info.id - previousDocumentId);
                position += osPostings.write(info.frequency);
                previousDocumentId = info.id;
            }
            return position;
        }
    }

    private record DocumentInfo(int id, int frequency) {}
}
