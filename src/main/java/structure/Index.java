package structure;

import java.util.List;

public interface Index extends SearchStructure<List<Integer>> {
    List<Integer> getAllDocumentIds();

    int getDocumentFrequency(String term);
}
