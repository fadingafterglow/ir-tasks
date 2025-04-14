package utils;

import document.Document;
import structure.document.PositionalIndex;
import structure.document.TfAwareIndex;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MocksFactory {

    public static PositionalIndex.Entry ofP(int documentId, Integer... positions) {
        List<Integer> positionsList = Arrays.asList(positions);
        return new PositionalIndex.Entry() {
            @Override
            public int getDocumentId() {
                return documentId;
            }

            @Override
            public int getTermFrequency() {
                return positions.length;
            }

            @Override
            public List<Integer> getPositions() {
                return positionsList;
            }

            @Override
            public String toString() {
                return String.format("(%d: %s)", documentId, positionsList);
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof PositionalIndex.Entry entry)) return false;
                return documentId == entry.getDocumentId() && Objects.equals(positionsList, entry.getPositions());
            }

            @Override
            public int hashCode() {
                return Objects.hash(documentId, positionsList);
            }
        };
    }

    public static Document of(String name, String body) {
        return new Document() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public List<String> getZones() {
                return List.of(body);
            }

            @Override
            public long getSize() {
                return 0;
            }
        };
    }

    public static TfAwareIndex.Entry ofT(int documentId, int termFrequency) {
        return new TfAwareIndex.Entry() {
            @Override
            public int getDocumentId() {
                return documentId;
            }

            @Override
            public int getTermFrequency() {
                return termFrequency;
            }

            @Override
            public String toString() {
                return String.format("(%d: %d)", documentId, termFrequency);
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof TfAwareIndex.Entry entry)) return false;
                return documentId == entry.getDocumentId() && Objects.equals(termFrequency, entry.getTermFrequency());
            }

            @Override
            public int hashCode() {
                return Objects.hash(documentId, termFrequency);
            }
        };
    }
}
