package structure;

import tokenizer.Tokenizer;

public interface SearchStructure<T> {
    int documentsCount();

    int termsCount();

    String getDocumentName(int id);

    T getDocumentIds(String term);

    Tokenizer getTokenizer();
}
