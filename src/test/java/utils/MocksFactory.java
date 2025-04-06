package utils;

import document.Document;
import structure.document.PositionalIndex;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MocksFactory {

    public static PositionalIndex.Entry of(int documentId, Integer... positions) {
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
}
