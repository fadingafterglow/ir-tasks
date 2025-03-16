package structure.document.memory;

import document.Document;
import structure.document.PositionalIndex;
import tokenizer.Tokenizer;

import java.util.*;
import java.util.stream.Stream;

public class MapPositionalIndex implements PositionalIndex {
    private final Tokenizer tokenizer;
    private final Map<String, List<PositionalIndex.Entry>> index;
    private final String[] documentsMap;
    private final List<Integer> documentIds;
    
    public MapPositionalIndex(List<Document> documents, Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        index = new HashMap<>();
        documentsMap = new String[documents.size()];
        documentIds = Stream.iterate(0, x -> x < documents.size(), x -> x + 1).toList();
        List<Triple> pairs = computeTriples(documents, tokenizer);
        computeIndex(pairs);
    }

    private List<Triple> computeTriples(List<Document> documents, Tokenizer tokenizer) {
        List<Triple> triples = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            documentsMap[i] = document.getName();
            List<String> terms = tokenizer.tokenize(document);
            for (int j = 0; j < terms.size(); j++)
                triples.add(new Triple(terms.get(j), i, j));
        }
        return triples;
    }

    private void computeIndex(List<Triple> triples) {
        if (triples.isEmpty()) return;
        triples.sort(Comparator.naturalOrder());
        int i = 0;
        while (i < triples.size()) {
            String term = triples.get(i).term();
            List<PositionalIndex.Entry> entries = new ArrayList<>();
            while (i < triples.size() && triples.get(i).sameTerm(term)) {
                int documentId = triples.get(i).documentId();
                List<Integer> positions = new ArrayList<>();
                while (i < triples.size() && triples.get(i).sameTermAndDocument(term, documentId)) {
                    positions.add(triples.get(i).position());
                    i++;
                }
                entries.add(new Entry(documentId, positions));
            }
            index.put(term, entries);
        }
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
        return documentsMap[id];
    }

    @Override
    public List<Integer> getDocumentIds(String term) {
        return getPositions(term).stream().map(PositionalIndex.Entry::getDocumentId).toList();
    }
    
    @Override
    public List<PositionalIndex.Entry> getPositions(String term) {
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
        return getPositions(term).size();
    }
    
    private static class Entry implements PositionalIndex.Entry {
        
        private final int documentId;
        private final List<Integer> positions;
        
        public Entry(int documentId, List<Integer> positions) {
            this.documentId = documentId;
            this.positions = positions;
        }
        
        @Override
        public int getDocumentId() {
            return documentId;
        }

        @Override
        public int getTermFrequency() {
            return positions.size();
        }

        @Override
        public List<Integer> getPositions() {
            return positions;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PositionalIndex.Entry entry)) return false;
            return documentId == entry.getDocumentId() && Objects.equals(positions, entry.getPositions());
        }

        @Override
        public int hashCode() {
            return Objects.hash(documentId, positions);
        }
    }

    private record Triple(String term, int documentId, int position) implements Comparable<Triple> {
        
        public boolean sameTerm(String term) {
            return this.term.equals(term);
        }
        
        public boolean sameTermAndDocument(String term, int documentId) {
            return sameTerm(term) && this.documentId == documentId;
        }
        
        @Override
        public int compareTo(Triple o) {
            return term.compareTo(o.term);
        }
    }
}
