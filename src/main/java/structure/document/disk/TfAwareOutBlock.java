package structure.document.disk;

import encoders.EncodedOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TfAwareOutBlock implements OutBlock {

    private static final int POSTING_SIZE = 24;
    private static final int TERM_SIZE = 80;
    private final int zonesCount;
    private final Map<String, TermInfo> block;
    private long postingsSize;

    public TfAwareOutBlock(int zonesCount) {
        this.zonesCount = zonesCount;
        this.block = new HashMap<>();
    }

    @Override
    public long size() {
        return (long)block.size() * TERM_SIZE + postingsSize;
    }

    @Override
    public boolean isEmpty() {
        return block.isEmpty();
    }

    @Override
    public void add(String term, int id) {
        TermInfo t = block.computeIfAbsent(term, _ -> new TermInfo(id));
        DocumentInfo last = t.postingList.getLast();
        if (last.id == id) {
            ++last.frequency;
        }
        else {
            t.postingList.add(new DocumentInfo(id, 1));
            if (documentId(id) != documentId(last.id))
                ++t.frequency;
            postingsSize += POSTING_SIZE;
        }
    }

    private int documentId(int id) {
        return id - id % zonesCount;
    }

    @Override
    public void flush(EncodedOutputStream os) {
        block.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(
                e -> {
                    TermInfo termInfo = e.getValue();
                    os.write(e.getKey());
                    os.write(termInfo.frequency);
                    os.write(termInfo.postingList.size());
                    for (DocumentInfo info : termInfo.postingList) {
                        os.write(info.id);
                        os.write(info.frequency);
                    }
                }
        );
    }

    private static final class TermInfo {
        private int frequency;
        private final List<DocumentInfo> postingList;

        public TermInfo(int initialId) {
            frequency = 1;
            postingList = new ArrayList<>();
            postingList.add(new DocumentInfo(initialId, 0));
        }
    }

    private static final class DocumentInfo {
        private final int id;
        private int frequency;

        public DocumentInfo(int id, int frequency) {
            this.id = id;
            this.frequency = frequency;
        }
    }
}
