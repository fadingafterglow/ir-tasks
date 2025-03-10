package structure.vocabulary;

import java.util.List;

public interface VocabularyIndex {

    int addTerm(String term);

    List<Integer> getTermIds(String termPattern);
}
