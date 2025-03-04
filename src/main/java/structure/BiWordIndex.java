package structure;

public interface BiWordIndex extends Index {
    String getProcessedDocument(int id);
    String getSeparator();
}
