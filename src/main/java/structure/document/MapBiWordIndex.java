package structure.document;

import document.Document;
import tokenizer.Tokenizer;

import java.util.*;
import java.util.stream.Stream;

public class MapBiWordIndex implements BiWordIndex {

    private static final String SEPARATOR = " ";
    private final Tokenizer tokenizer;
    private final Map<String, List<Integer>> index;
    private final DocumentMapEntry[] documentsMap;
    private final List<Integer> documentIds;

    public MapBiWordIndex(List<Document> documents, Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        index = new HashMap<>();
        documentsMap = new DocumentMapEntry[documents.size()];
        documentIds = Stream.iterate(0, x -> x < documents.size(), x -> x + 1).toList();
        List<Pair> pairs = computePairs(documents);
        computeIndex(pairs);
    }

    private List<Pair> computePairs(List<Document> documents) {
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            List<String> terms = tokenizer.tokenize(document);
            documentsMap[i] = new DocumentMapEntry(document.getName(), String.join(SEPARATOR, terms));
            for (int j = 1; j < terms.size(); j++) {
                String previous = terms.get(j - 1);
                pairs.add(new Pair(previous, i));
                pairs.add(new Pair(previous + SEPARATOR + terms.get(j), i));
            }
            pairs.add(new Pair(terms.getLast(), i));
        }
        return pairs;
    }

    private void computeIndex(List<Pair> pairs) {
        if (pairs.isEmpty()) return;
        Collections.sort(pairs);
        String term = pairs.getFirst().term();
        List<Integer> ids = new ArrayList<>();
        ids.add(pairs.getFirst().documentId());
        for (Pair p: pairs.subList(1, pairs.size())) {
            if (!term.equals(p.term())) {
                index.put(term, ids);
                term = p.term();
                ids = new ArrayList<>();
                ids.add(p.documentId());
            }
            else if (!ids.getLast().equals(p.documentId()))
                ids.add(p.documentId());
        }
        index.put(term, ids);
    }

    @Override
    public int documentsCount() {
        return documentsMap.length;
    }

    @Override
    public int termsCount() {
        return index.size();
    }

    @Override
    public String getDocumentName(int id) {
        if (id < 0 || id >= documentsMap.length)
            return null;
        return documentsMap[id].documentName();
    }

    @Override
    public String getProcessedDocument(int id) {
        if (id < 0 || id >= documentsMap.length)
            return null;
        return documentsMap[id].documentBody();
    }

    @Override
    public List<Integer> getDocumentIds(String term) {
        return index.getOrDefault(term, List.of());
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public List<Integer> getAllDocumentIds() {
        return documentIds;
    }

    @Override
    public int getDocumentFrequency(String term) {
        return getDocumentIds(term).size();
    }

    @Override
    public String getSeparator() {
        return SEPARATOR;
    }

    private record Pair(String term, int documentId) implements Comparable<Pair> {
        @Override
        public int compareTo(Pair o) {
            return term.compareTo(o.term);
        }
    }

    private record DocumentMapEntry(String documentName, String documentBody) {
    }
}
