package structure;

import document.Document;
import tokenizer.Tokenizer;

import java.util.*;
import java.util.stream.Stream;

public class MapInvertedIndex implements Index {

    private final Tokenizer tokenizer;
    private final Map<String, List<Integer>> index;
    private final String[] documentsMap;
    private final List<Integer> documentIds;

    public MapInvertedIndex(List<Document> documents, Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        index = new HashMap<>();
        documentsMap = new String[documents.size()];
        documentIds = Stream.iterate(0, x -> x < documents.size(), x -> x + 1).toList();
        List<Pair> pairs = computePairs(documents);
        computeIndex(pairs);
    }

    private List<Pair> computePairs(List<Document> documents) {
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            documentsMap[i] = document.getName();
            List<String> terms = tokenizer.tokenize(document);
            for (String term : terms)
                pairs.add(new Pair(term, i));
        }
        return pairs;
    }

    private void computeIndex(List<Pair> pairs) {
        if (pairs.isEmpty()) return;
        pairs.sort(Comparator.naturalOrder());
        String term = pairs.getFirst().term;
        List<Integer> ids = new ArrayList<>();
        ids.add(pairs.getFirst().documentId);
        for (Pair p: pairs.subList(1, pairs.size())) {
            if (!term.equals(p.term)) {
                index.put(term, ids);
                term = p.term;
                ids = new ArrayList<>();
                ids.add(p.documentId);
            }
            else if (!ids.getLast().equals(p.documentId))
                ids.add(p.documentId);
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
    public String getDocument(int id) {
        if (id < 0 || id >= documentsMap.length)
            return null;
        return documentsMap[id];
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

    private record Pair(String term, int documentId) implements Comparable<Pair> {
        @Override
        public int compareTo(Pair o) {
            int termCompare = term.compareTo(o.term);
            return termCompare != 0 ? termCompare : Integer.compare(documentId, o.documentId);
        }
    }
}
