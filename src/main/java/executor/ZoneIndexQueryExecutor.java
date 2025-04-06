package executor;

import expression.Expression;
import structure.document.ZoneIndex;

import java.util.*;

public class ZoneIndexQueryExecutor extends BaseIndexQueryExecutor<ZoneIndex> {

    private final double[] weights;

    public ZoneIndexQueryExecutor(ZoneIndex index, double[] weights) {
        super(index);
        if (index.getZonesCount() != weights.length)
            throw new IllegalArgumentException("Weights length must match the number of zones in the index.");
        this.weights = weights;
    }

    @Override
    public List<String> execute(Expression query) {
        Map<Expression, Integer> estimation = new HashMap<>();
        estimate(query, estimation);
        List<Integer> ids = executeForIds(query, estimation);
        return computeScores(ids).stream()
                .sorted()
                .map(this::mapToResult)
                .toList();
    }

    private List<DocumentInfo> computeScores(List<Integer> ids) {
        List<DocumentInfo> result = new ArrayList<>();
        if (ids.isEmpty()) return result;

        Iterator<Integer> iterator = ids.iterator();
        int currentId = iterator.next();
        boolean hasNextDocument = true;
        while (hasNextDocument) {
            int documentId = currentId - currentId % weights.length;
            double score = 0;
            while (currentId - documentId < weights.length) {
                score += weights[currentId - documentId];
                if (!iterator.hasNext()) {
                    hasNextDocument = false;
                    break;
                }
                currentId = iterator.next();
            }
            result.add(DocumentInfo.of(documentId, score));
        }
        return result;
    }

    private String mapToResult(DocumentInfo documentInfo) {
        return String.format("%s (score: %.2f)", index.getDocumentName(documentInfo.id()), documentInfo.score());
    }

    private record DocumentInfo(int id, double score) implements Comparable<DocumentInfo> {

        public static DocumentInfo of(int id, double score) {
            return new DocumentInfo(id, score);
        }

        @Override
        public int compareTo(DocumentInfo o) {
            return -Double.compare(this.score, o.score);
        }
    }
}
