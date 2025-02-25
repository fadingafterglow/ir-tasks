package structure;

import java.util.List;

public interface Index {
    int documentsCount();

    int termsCount();

    String getDocument(int id);

    List<Integer> getDocumentIds(String term);

    List<Integer> getAllDocumentIds();

    int getDocumentFrequency(String term);
}
