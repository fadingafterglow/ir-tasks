package structure.document.disk;

import encoders.EncodedOutputStream;

import java.util.*;

public class DefaultOutBlock implements OutBlock {

    private static final int POSTING_SIZE = 20;
    private static final int TERM_SIZE = 80;
    private final int zonesCount;
    private final Map<String, TermInfo> block;
    private long postingsSize;

    public DefaultOutBlock(int zonesCount) {
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
        int lastId = t.postingList.getLast();
        if (lastId == id) return;
        t.postingList.add(id);
        if (documentId(id) != documentId(lastId))
            ++t.frequency;
        postingsSize += POSTING_SIZE;
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
                    for (int documentId : termInfo.postingList)
                        os.write(documentId);
                }
        );
    }

    private static final class TermInfo {
        private int frequency;
        private final List<Integer> postingList;

        public TermInfo(int initialId) {
            frequency = 1;
            postingList = new ArrayList<>();
            postingList.add(initialId);
        }
    }
}
