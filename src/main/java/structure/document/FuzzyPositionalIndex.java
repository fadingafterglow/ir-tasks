package structure.document;

import document.Document;
import structure.vocabulary.VocabularyIndex;
import tokenizer.Tokenizer;

import java.util.*;
import java.util.stream.Stream;

public class FuzzyPositionalIndex implements PositionalIndex {

    private final Tokenizer tokenizer;
    private final VocabularyIndex vocabularyIndex;
    private final Map<Integer, List<? extends PositionalIndex.Entry>> index;
    private final String[] documentsMap;
    private final List<Integer> documentIds;

    public FuzzyPositionalIndex(List<Document> documents, Tokenizer tokenizer, VocabularyIndex vocabularyIndex) {
        this.tokenizer = tokenizer;
        this.vocabularyIndex = vocabularyIndex;
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
                triples.add(new Triple(vocabularyIndex.addTerm(terms.get(j)), i, j));
        }
        return triples;
    }

    private void computeIndex(List<Triple> triples) {
        if (triples.isEmpty()) return;
        triples.sort(Comparator.naturalOrder());
        int i = 0;
        while (i < triples.size()) {
            int termId = triples.get(i).termId;
            List<Entry> entries = new ArrayList<>();
            while (i < triples.size() && triples.get(i).sameTerm(termId)) {
                int documentId = triples.get(i).documentId();
                List<Integer> positions = new ArrayList<>();
                while (i < triples.size() && triples.get(i).sameTermAndDocument(termId, documentId)) {
                    positions.add(triples.get(i).position());
                    i++;
                }
                entries.add(new Entry(documentId, positions));
            }
            index.put(termId, entries);
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
    @SuppressWarnings("unchecked")
    public List<PositionalIndex.Entry> getPositions(String term) {
        List<Integer> termIds = vocabularyIndex.getTermIds(term);
        if (termIds.isEmpty()) return List.of();
        if (termIds.size() == 1) return (List<PositionalIndex.Entry>) index.get(termIds.getFirst());
        return merge(termIds);
    }

    private List<PositionalIndex.Entry> merge(List<Integer> termIds) {
        List<PositionalIndex.Entry> result = new ArrayList<>();
        PriorityQueue<EntriesEnumerator> queue = createQueue(termIds);
        while (!queue.isEmpty()) {
            EntriesEnumerator enumerator = queue.poll();
            int currentDocumentId = enumerator.currentDocumentId();
            List<Integer> currentPositions = enumerator.currentPositions();
            while (!queue.isEmpty() && queue.peek().currentDocumentId() == currentDocumentId) {
                EntriesEnumerator next = queue.poll();
                currentPositions = merge(currentPositions, next.currentPositions());
                if (next.advance())
                    queue.add(next);
            }
            result.add(new Entry(currentDocumentId, currentPositions));
            if (enumerator.advance())
                queue.add(enumerator);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private PriorityQueue<EntriesEnumerator> createQueue(List<Integer> termIds) {
        PriorityQueue<EntriesEnumerator> queue = new PriorityQueue<>();
        for (int termId : termIds) {
            List<Entry> entries = (List<Entry>) index.get(termId);
            queue.add(new EntriesEnumerator(entries));
        }
        return queue;
    }

    private List<Integer> merge(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            int leftId = left.get(l), rightId = right.get(r);
            if (leftId == rightId) {
                result.add(leftId);
                l++;
                r++;
            }
            else if (leftId < rightId) {
                result.add(leftId);
                l++;
            }
            else {
                result.add(rightId);
                r++;
            }
        }
        while (l < left.size())
            result.add(left.get(l++));
        while (r < right.size())
            result.add(right.get(r++));
        return result;
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

    private static class Entry implements PositionalIndex.Entry, Comparable<PositionalIndex.Entry> {

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

        @Override
        public int compareTo(PositionalIndex.Entry o) {
            return documentId - o.getDocumentId();
        }
    }

    private static class EntriesEnumerator implements Comparable<EntriesEnumerator> {

        private final List<Entry> entries;
        private int index;

        public EntriesEnumerator(List<Entry> entries) {
            this.entries = entries;
        }

        public int currentDocumentId() {
            return entries.get(index).getDocumentId();
        }

        public List<Integer> currentPositions() {
            return entries.get(index).getPositions();
        }

        public boolean advance() {
            return ++index < entries.size();
        }

        @Override
        public int compareTo(EntriesEnumerator o) {
            return currentDocumentId() - o.currentDocumentId();
        }

    }

    private record Triple(int termId, int documentId, int position) implements Comparable<Triple> {

        public boolean sameTerm(int termId) {
            return this.termId == termId;
        }

        public boolean sameTermAndDocument(int termId, int documentId) {
            return sameTerm(termId) && this.documentId == documentId;
        }

        @Override
        public int compareTo(Triple o) {
            return termId - o.termId;
        }
    }
}
