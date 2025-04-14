package structure.document;

import java.util.List;

public interface TfAwareIndex extends Index {

    List<Entry> getEntries(String term);

    interface Entry {
        int getDocumentId();
        int getTermFrequency();
    }
}
