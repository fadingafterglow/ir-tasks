package structure;

public interface SearchStructure<T> {
    int documentsCount();

    int termsCount();

    String getDocument(int id);

    T getDocumentIds(String term);
}
