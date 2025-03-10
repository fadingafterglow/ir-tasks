package executor;

import expression.*;
import structure.PositionalIndex;

import java.util.*;

public class PositionalIndexQueryExecutor extends BaseIndexQueryExecutor<PositionalIndex> {

    public PositionalIndexQueryExecutor(PositionalIndex index) {
        super(index);
    }

    @Override
    protected void estimate(Expression query, Map<Expression, Integer> estimation) {
        switch (query) {
            case PhraseExpression pe -> estimatePhrase(pe, estimation);
            case NotExpression ne -> estimateNot(ne, estimation);
            case AndExpression ae -> estimateAnd(ae, estimation);
            case OrExpression oe -> estimateOr(oe, estimation);
            case ProximityExpression pe -> estimateProximity(pe, estimation);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        }
    }

    protected void estimatePhrase(PhraseExpression e, Map<Expression, Integer> estimation) {
        List<String> terms = index.getTokenizer().tokenize(e.getPhrase());
        int min = index.documentsCount();
        for (String term : terms)
            min = Math.min(min, index.getDocumentFrequency(term));
        estimation.put(e, min);
    }

    protected void estimateProximity(ProximityExpression e, Map<Expression, Integer> estimation) {
        estimate(e.getLeft(), estimation);
        estimate(e.getRight(), estimation);
        int min = Math.min(estimation.get(e.getLeft()), estimation.get(e.getRight()));
        estimation.put(e, min);
    }

    @Override
    protected List<Integer> executeForIds(Expression query, Map<Expression, Integer> estimation) {
        return switch (query) {
            case PhraseExpression pe -> executePhrase(pe);
            case NotExpression ne -> executeNot(ne, estimation);
            case AndExpression ae -> executeAnd(ae, estimation);
            case OrExpression oe -> executeOr(oe, estimation);
            case ProximityExpression pe -> executeProximity(pe);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        };
    }

    @Override
    protected List<Integer> executePhrase(PhraseExpression e) {
        return executePhraseForPositions(e).stream().map(PositionalIntersectResult::getDocumentId).toList();
    }

    protected List<PositionalIntersectResult> executePhraseForPositions(PhraseExpression e) {
        List<String> terms = index.getTokenizer().tokenize(e.getPhrase());
        List<PositionalIntersectResult> positions = index.getPositions(terms.getFirst()).stream()
                .map(entry -> (PositionalIntersectResult) new PositionalIntersectResultAdapter(entry))
                .toList();
        for (int i = 1; i < terms.size(); i++) {
            if (positions.isEmpty()) break;
            positions = phrasePositionalIntersect(positions, index.getPositions(terms.get(i)));
        }
        return positions;
    }

    protected List<Integer> executeProximity(ProximityExpression e) {
        return executeProximityForPositions(e).stream().map(PositionalIntersectResult::getDocumentId).toList();
    }

    protected List<PositionalIntersectResult> executeProximityForPositions(ProximityExpression e) {
        Expression left = e.getLeft();
        Expression right = e.getRight();
        int k = e.getProximity();
        if (left instanceof PhraseExpression l && right instanceof PhraseExpression r) {
            return positionalIntersect(executePhraseForPositions(l), executePhraseForPositions(r), k);
        }
        if (left instanceof PhraseExpression l && right instanceof ProximityExpression r) {
            return positionalIntersect(executePhraseForPositions(l), executeProximityForPositions(r), k);
        }
        if (left instanceof ProximityExpression l && right instanceof PhraseExpression r) {
            return positionalIntersect(executeProximityForPositions(l), executePhraseForPositions(r), k);
        }
        throw new RuntimeException("Unsupported expression type: " + left.getClass() + " or " + right.getClass());
    }

    protected List<PositionalIntersectResult> phrasePositionalIntersect(List<PositionalIntersectResult> left, List<PositionalIndex.Entry> right) {
        ArrayList<PositionalIntersectResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).getDocumentId() == right.get(r).getDocumentId()) {
                PositionalIntersectResult res = phrasePositionalIntersect(left.get(l), right.get(r).getPositions());
                if (res.getPositionsCount() > 0) result.add(res);
                l++;
                r++;
            }
            else if (left.get(l).getDocumentId() < right.get(r).getDocumentId())
                l++;
            else
                r++;
        }
        return result;
    }

    protected PositionalIntersectResult phrasePositionalIntersect(PositionalIntersectResult positionsLeft, List<Integer> positionsRight) {
        PositionalIntersectResultImpl result = new PositionalIntersectResultImpl(positionsLeft.getDocumentId());
        int pl = 0, pr = 0;
        while (pl < positionsLeft.getPositionsCount()) {
            MatchPosition positionLeft = positionsLeft.getPosition(pl);
            while (pr < positionsRight.size()) {
                int positionRight = positionsRight.get(pr);
                if (positionRight - positionLeft.end() == 1)
                    result.addPosition(positionLeft.start(), positionRight);
                else if (positionRight > positionLeft.end())
                    break;
                pr++;
            }
            pl++;
        }
        return result;
    }

    protected List<PositionalIntersectResult> positionalIntersect(List<PositionalIntersectResult> left, List<PositionalIntersectResult> right, int k) {
        ArrayList<PositionalIntersectResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).getDocumentId() == right.get(r).getDocumentId()) {
                PositionalIntersectResult res = positionalIntersect(left.get(l), right.get(r), k);
                if (res.getPositionsCount() > 0) result.add(res);
                l++;
                r++;
            }
            else if (left.get(l).getDocumentId() < right.get(r).getDocumentId())
                l++;
            else
                r++;
        }
        return result;
    }

    protected PositionalIntersectResult positionalIntersect(PositionalIntersectResult positionsLeft, PositionalIntersectResult positionsRight, int k) {
        PositionalIntersectResultImpl result = new PositionalIntersectResultImpl(positionsLeft.getDocumentId());
        List<MatchPosition> leftWindow = new LinkedList<>();
        List<MatchPosition> rightWindow = new LinkedList<>();
        int pl = 0, pr1 = 0, pr2 = 0;
        while (pl < positionsLeft.getPositionsCount()) {
            MatchPosition positionLeft = positionsLeft.getPosition(pl);
            while (pr1 < positionsRight.getPositionsCount()) {
                MatchPosition positionRight = positionsRight.getPosition(pr1);
                int distanceLeft = positionLeft.start() - positionRight.end();
                if (distanceLeft > 0 && distanceLeft <= k)
                    leftWindow.add(positionRight);
                else if (distanceLeft <= 0)
                    break;
                pr1++;
            }
            while (pr2 < positionsRight.getPositionsCount()) {
                MatchPosition positionRight = positionsRight.getPosition(pr2);
                int distanceRight = positionRight.start() - positionLeft.end();
                if (distanceRight > 0 && distanceRight <= k)
                    rightWindow.add(positionRight);
                else if (distanceRight > k)
                    break;
                pr2++;
            }
            while (!leftWindow.isEmpty() && positionLeft.start() - leftWindow.getFirst().end() > k)
                leftWindow.removeFirst();
            while (!rightWindow.isEmpty() && rightWindow.getFirst().start() - positionLeft.end() <= 0)
                rightWindow.removeFirst();
            for (MatchPosition p : leftWindow)
                result.addPosition(p.start(), positionLeft.end());
            for (MatchPosition p : rightWindow)
                result.addPosition(positionLeft.start(), p.end());
            pl++;
        }
        result.sortPositions();
        return result;
    }

    protected interface PositionalIntersectResult {
        int getDocumentId();
        MatchPosition getPosition(int index);
        int getPositionsCount();
    }

    protected static class PositionalIntersectResultImpl implements PositionalIntersectResult {

        private final int documentId;
        private final List<MatchPosition> positions;

        public PositionalIntersectResultImpl(int documentId) {
            this.documentId = documentId;
            this.positions = new ArrayList<>();
        }

        @Override
        public int getDocumentId() {
            return documentId;
        }

        @Override
        public MatchPosition getPosition(int index) {
            return positions.get(index);
        }

        @Override
        public int getPositionsCount() {
            return positions.size();
        }

        public void addPosition(int start, int end) {
            positions.add(new MatchPosition(start, end));
        }

        public void sortPositions() {
            positions.sort(MatchPosition::compareTo);
        }
    }

    protected static class PositionalIntersectResultAdapter implements PositionalIntersectResult {

        private final PositionalIndex.Entry entry;

        public PositionalIntersectResultAdapter(PositionalIndex.Entry entry) {
            this.entry = entry;
        }

        @Override
        public int getDocumentId() {
            return entry.getDocumentId();
        }

        @Override
        public MatchPosition getPosition(int index) {
            int position = entry.getPositions().get(index);
            return new MatchPosition(position, position);
        }

        @Override
        public int getPositionsCount() {
            return entry.getTermFrequency();
        }
    }

    protected record MatchPosition(int start, int end) implements Comparable<MatchPosition> {

        @Override
        public int compareTo(MatchPosition o) {
            int startComparison = Integer.compare(start, o.start);
            return startComparison != 0 ? startComparison : Integer.compare(end, o.end);
        }
    }
}
