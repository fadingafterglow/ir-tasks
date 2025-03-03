package structure;

import java.util.List;

public interface PositionalIndex extends Index {

    List<Entry> getPositions(String term);

    interface Entry {
        int getDocumentId();
        int getTermFrequency();
        List<Integer> getPositions();
    }
}
