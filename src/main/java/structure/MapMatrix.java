package structure;

import document.Document;
import tokenizer.DocumentTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapMatrix implements Matrix {

    private final Map<String, boolean[]> matrix;
    private final String[] documentsMap;
    private final boolean[] falseRow;

    public MapMatrix(List<Document> documents, DocumentTokenizer tokenizer) {
        matrix = new HashMap<>();
        documentsMap = new String[documents.size()];
        falseRow = new boolean[documents.size()];
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            documentsMap[i] = document.getName();
            List<String> terms = tokenizer.tokenize(document);
            for (String term : terms) {
                boolean[] row = matrix.computeIfAbsent(term, _ -> new boolean[documents.size()]);
                row[i] = true;
            }
        }
    }

    @Override
    public int documentsCount() {
        return documentsMap.length;
    }

    @Override
    public int termsCount() {
        return matrix.size();
    }

    @Override
    public String getDocument(int id) {
        if (id < 0 || id >= documentsMap.length)
            return null;
        return documentsMap[id];
    }

    @Override
    public boolean[] getDocumentIds(String term) {
        return matrix.getOrDefault(term, falseRow);
    }
}
