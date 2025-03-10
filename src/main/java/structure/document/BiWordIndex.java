package structure.document;

public interface BiWordIndex extends Index {
    String getProcessedDocument(int id);
    String getSeparator();
}
