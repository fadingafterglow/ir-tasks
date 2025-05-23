package structure.document;

import java.util.List;

public interface TfAwareIndex extends Index {

    List<Entry> getEntries(int termId);
    List<Entry> getEntries(String term);

    double getIdf(int termId);
    double getIdf(String term);

    interface Entry {
        int getDocumentId();
        int getTermFrequency();
    }
}
