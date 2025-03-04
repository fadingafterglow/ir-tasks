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
        List<String> terms = index.getTokenizer().tokenize(e.getPhrase());
        if (terms.size() == 1)
            return index.getDocumentIds(terms.getFirst());
        return executePhraseForPositions(terms).stream().map(PositionalResult::documentId).distinct().toList();
    }

    protected List<PositionalResult> executePhraseForPositions(List<String> terms) {
        List<PositionalResult> positions = phraseBeginningPositionalIntersect(index.getPositions(terms.get(0)), index.getPositions(terms.get(1)));
        for (int i = 2; i < terms.size(); i++) {
            if (positions.isEmpty()) break;
            positions = phrasePositionalIntersect(positions, index.getPositions(terms.get(i)));
        }
        return positions;
    }

    protected List<Integer> executeProximity(ProximityExpression e) {
        return executeProximityForPositions(e).stream().map(PositionalResult::documentId).distinct().toList();
    }

    protected List<PositionalResult> executeProximityForPositions(ProximityExpression e) {
        Expression left = e.getLeft();
        Expression right = e.getRight();
        int k = e.getProximity();
        if (left instanceof PhraseExpression l && right instanceof PhraseExpression r) {
            List<String> lTerms = index.getTokenizer().tokenize(l.getPhrase());
            List<String> rTerms = index.getTokenizer().tokenize(r.getPhrase());
            if (lTerms.size() == 1 && rTerms.size() == 1)
                return positionalIntersect1(index.getPositions(lTerms.getFirst()), index.getPositions(rTerms.getFirst()), k);
            if (lTerms.size() == 1)
                return positionalIntersect2(executePhraseForPositions(rTerms), index.getPositions(lTerms.getFirst()), k);
            if (rTerms.size() == 1)
                return positionalIntersect2(executePhraseForPositions(lTerms), index.getPositions(rTerms.getFirst()), k);
            return positionalIntersect3(executePhraseForPositions(lTerms), executePhraseForPositions(rTerms), k);
        }
        if (left instanceof PhraseExpression l && right instanceof ProximityExpression r) {
            List<String> lTerms = index.getTokenizer().tokenize(l.getPhrase());
            if (lTerms.size() == 1)
                return positionalIntersect2(executeProximityForPositions(r), index.getPositions(lTerms.getFirst()), k);
            return positionalIntersect3(executePhraseForPositions(lTerms), executeProximityForPositions(r), k);
        }
        if (left instanceof ProximityExpression l && right instanceof PhraseExpression r) {
            List<String> rTerms = index.getTokenizer().tokenize(r.getPhrase());
            if (rTerms.size() == 1)
                return positionalIntersect2(executeProximityForPositions(l), index.getPositions(rTerms.getFirst()), k);
            return positionalIntersect3(executeProximityForPositions(l), executePhraseForPositions(rTerms), k);
        }
        throw new RuntimeException("Unsupported expression type: " + left.getClass() + " or " + right.getClass());
    }

    protected List<PositionalResult> phraseBeginningPositionalIntersect(List<PositionalIndex.Entry> left, List<PositionalIndex.Entry> right) {
        ArrayList<PositionalResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).getDocumentId() == right.get(r).getDocumentId()) {
                phraseBeginningPositionalIntersect(left.get(l).getDocumentId(), left.get(l).getPositions(), right.get(r).getPositions(), result);
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

    protected void phraseBeginningPositionalIntersect(int documentId, List<Integer> positionsLeft, List<Integer> positionsRight, List<PositionalResult> result) {
        int pl = 0, pr = 0;
        while (pl < positionsLeft.size()) {
            int positionLeft = positionsLeft.get(pl);
            while (pr < positionsRight.size()) {
                int positionRight = positionsRight.get(pr);
                if (positionRight - positionLeft == 1)
                    result.add(new PositionalResult(documentId, positionLeft, positionRight));
                else if (positionRight > positionLeft)
                    break;
                pr++;
            }
            pl++;
        }
    }

    protected List<PositionalResult> phrasePositionalIntersect(List<PositionalResult> left, List<PositionalIndex.Entry> right) {
        ArrayList<PositionalResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).documentId() == right.get(r).getDocumentId()) {
                l = phrasePositionalIntersect(left, l, right.get(r).getPositions(), result);
                r++;
            }
            else if (left.get(l).documentId() < right.get(r).getDocumentId())
                l++;
            else
                r++;
        }
        return result;
    }

    protected int phrasePositionalIntersect(List<PositionalResult> resultsLeft, int rl, List<Integer> positionsRight, List<PositionalResult> result) {
        int pr = 0;
        int documentId = resultsLeft.get(rl).documentId();
        while (rl < resultsLeft.size() && resultsLeft.get(rl).documentId() == documentId) {
            PositionalResult resultLeft = resultsLeft.get(rl);
            while (pr < positionsRight.size()) {
                int positionRight = positionsRight.get(pr);
                if (positionRight - resultLeft.positionRight() == 1)
                    result.add(new PositionalResult(documentId, resultLeft.positionLeft(), positionRight));
                else if (positionRight > resultLeft.positionRight())
                    break;
                pr++;
            }
            rl++;
        }
        return rl;
    }

    protected List<PositionalResult> positionalIntersect1(List<PositionalIndex.Entry> left, List<PositionalIndex.Entry> right, int k) {
        ArrayList<PositionalResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).getDocumentId() == right.get(r).getDocumentId()) {
                positionalIntersect1(left.get(l).getDocumentId(), left.get(l).getPositions(), right.get(r).getPositions(), k, result);
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

    protected void positionalIntersect1(int documentId, List<Integer> positionsLeft, List<Integer> positionsRight, int k, List<PositionalResult> result) {
        List<Integer> window = new LinkedList<>();
        int pl = 0, pr = 0;
        while (pl < positionsLeft.size()) {
            int positionLeft = positionsLeft.get(pl);
            while (pr < positionsRight.size()) {
                int positionRight = positionsRight.get(pr);
                int distance = positionRight - positionLeft;
                if (distance != 0 && Math.abs(distance) <= k)
                    window.add(positionRight);
                else if (distance > k)
                    break;
                pr++;
            }
            while (!window.isEmpty() && positionLeft - window.getFirst() > k)
                window.removeFirst();
            for (int p : window)
                if (positionLeft != p)
                    result.add(new PositionalResult(documentId, positionLeft, p));
            pl++;
        }
    }

    protected List<PositionalResult> positionalIntersect2(List<PositionalResult> left, List<PositionalIndex.Entry> right, int k) {
        ArrayList<PositionalResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).documentId() == right.get(r).getDocumentId()) {
                l = positionalIntersect2(left, l, right.get(r).getPositions(), k, result);
                r++;
            }
            else if (left.get(l).documentId() < right.get(r).getDocumentId())
                l++;
            else
                r++;
        }
        Collections.sort(result);
        return result;
    }

    protected int positionalIntersect2(List<PositionalResult> resultsLeft, int rl, List<Integer> positionsRight, int k, List<PositionalResult> result) {
        List<Integer> leftWindow = new LinkedList<>();
        List<Integer> rightWindow = new LinkedList<>();
        int pr1 = 0, pr2 = 0;
        int documentId = resultsLeft.get(rl).documentId();
        while (rl < resultsLeft.size() && resultsLeft.get(rl).documentId() == documentId) {
            PositionalResult resultLeft = resultsLeft.get(rl);
            while (pr1 < positionsRight.size()) {
                int positionRight = positionsRight.get(pr1);
                int distanceLeft = resultLeft.minPosition() - positionRight;
                if (distanceLeft > 0 && distanceLeft <= k)
                    leftWindow.add(positionRight);
                else if (distanceLeft <= 0)
                    break;
                pr1++;
            }
            while (pr2 < positionsRight.size()) {
                int positionRight = positionsRight.get(pr2);
                int distanceRight = positionRight - resultLeft.maxPosition();
                if (distanceRight > 0 && distanceRight <= k)
                    rightWindow.add(positionRight);
                else if (distanceRight > k)
                    break;
                pr2++;
            }
            while (!leftWindow.isEmpty() && resultLeft.minPosition() - leftWindow.getFirst() > k)
                leftWindow.removeFirst();
            while (!rightWindow.isEmpty() && rightWindow.getFirst() - resultLeft.maxPosition() <= 0)
                rightWindow.removeFirst();
            for (int p : leftWindow)
                result.add(new PositionalResult(documentId, p, resultLeft.maxPosition()));
            for (int p : rightWindow)
                result.add(new PositionalResult(documentId, resultLeft.minPosition(), p));
            rl++;
        }
        return rl;
    }

    protected List<PositionalResult> positionalIntersect3(List<PositionalResult> left, List<PositionalResult> right, int k) {
        ArrayList<PositionalResult> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).documentId() == right.get(r).documentId()) {
                int[] indexes = positionalIntersect3(left, l, right, r, k, result);
                l = indexes[0];
                r = indexes[1];
            }
            else if (left.get(l).documentId() < right.get(r).documentId())
                l++;
            else
                r++;
        }
        Collections.sort(result);
        return result;
    }

    protected int[] positionalIntersect3(List<PositionalResult> resultsLeft, int rl, List<PositionalResult> resultsRight, int rr, int k, List<PositionalResult> result) {
        List<PositionalResult> leftWindow = new LinkedList<>();
        List<PositionalResult> rightWindow = new LinkedList<>();
        int pr1 = rr, pr2 = rr;
        int documentId = resultsLeft.get(rl).documentId();
        while (rl < resultsLeft.size() && resultsLeft.get(rl).documentId() == documentId) {
            PositionalResult resultLeft = resultsLeft.get(rl);
            while (pr1 < resultsRight.size() && resultsRight.get(pr1).documentId() == documentId) {
                PositionalResult resultRight = resultsRight.get(pr1);
                int distanceLeft = resultLeft.minPosition() - resultRight.maxPosition();
                if (distanceLeft > 0 && distanceLeft <= k)
                    leftWindow.add(resultRight);
                else if (distanceLeft <= 0)
                    break;
                pr1++;
            }
            while (pr2 < resultsRight.size() && resultsRight.get(pr2).documentId() == documentId) {
                PositionalResult resultRight = resultsRight.get(pr2);
                int distanceRight = resultRight.minPosition() - resultLeft.maxPosition();
                if (distanceRight > 0 && distanceRight <= k)
                    rightWindow.add(resultRight);
                else if (distanceRight > k)
                    break;
                pr2++;
            }
            while (!leftWindow.isEmpty() && resultLeft.minPosition() - leftWindow.getFirst().maxPosition() > k)
                leftWindow.removeFirst();
            while (!rightWindow.isEmpty() && rightWindow.getFirst().minPosition() - resultLeft.maxPosition() <= 0)
                rightWindow.removeFirst();
            for (PositionalResult p : leftWindow)
                result.add(new PositionalResult(documentId, p.minPosition(), resultLeft.maxPosition()));
            for (PositionalResult p : rightWindow)
                result.add(new PositionalResult(documentId, resultLeft.minPosition(), p.maxPosition()));
            rl++;
        }
        return new int[] {rl, pr2};
    }

    protected record PositionalResult(int documentId, int positionLeft, int positionRight) implements Comparable<PositionalResult> {

        public int minPosition() {
            return Math.min(positionLeft, positionRight);
        }

        public int maxPosition() {
            return Math.max(positionLeft, positionRight);
        }

        @Override
        public int compareTo(PositionalResult o) {
            int documentIdComparison = Integer.compare(documentId, o.documentId);
            if (documentIdComparison != 0)
                return documentIdComparison;
            int positionLeftComparison = Integer.compare(positionLeft, o.positionLeft);
            if (positionLeftComparison != 0)
                return positionLeftComparison;
            return Integer.compare(positionRight, o.positionRight);
        }
    }
}
