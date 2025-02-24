package structures;

public interface Matrix {
    int documentsCount();

    int termsCount();

    String getDocument(int id);

    boolean[] getDocumentsRow(String term);
}
