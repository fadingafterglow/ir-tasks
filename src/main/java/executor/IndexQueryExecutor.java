package executor;

import expression.*;
import structure.Index;

import java.util.*;

import static java.util.Comparator.*;

public class IndexQueryExecutor implements QueryExecutor {

    private final Index index;

    public IndexQueryExecutor(Index index) {
        this.index = index;
    }

    @Override
    public List<String> execute(Expression query) {
        Map<Expression, Integer> estimation = new HashMap<>();
        estimate(query, estimation);
        return executeForIds(query, estimation).stream().map(index::getDocument).toList();
    }

    private void estimate(Expression query, Map<Expression, Integer> estimation) {
        switch (query) {
            case PhraseExpression pe -> estimation.put(pe, index.getDocumentFrequency(pe.getPhrase()));
            case NotExpression ne -> estimateNot(ne, estimation);
            case AndExpression ae -> estimateAnd(ae, estimation);
            case OrExpression oe -> estimateOr(oe, estimation);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        }
    }

    private void estimateNot(NotExpression e, Map<Expression, Integer> estimation) {
        estimate(e.getSubExpression(), estimation);
        estimation.put(e, index.documentsCount() - estimation.get(e.getSubExpression()));
    }

    private void estimateAnd(AndExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().forEach(sub -> estimate(sub, estimation));
        int min = index.documentsCount();
        for (Expression sub : e.getSubExpressions())
            min = Math.min(min, estimation.get(sub));
        estimation.put(e, min);
    }

    private void estimateOr(OrExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().forEach(sub -> estimate(sub, estimation));
        long sum = 0;
        for (Expression sub : e.getSubExpressions())
            sum += estimation.get(sub);
        estimation.put(e, Math.clamp(sum, 0, Integer.MAX_VALUE));
    }

    private List<Integer> executeForIds(Expression query, Map<Expression, Integer> estimation) {
        return switch (query) {
            case PhraseExpression pe -> index.getDocumentIds(pe.getPhrase());
            case NotExpression ne -> executeNot(ne, estimation);
            case AndExpression ae -> executeAnd(ae, estimation);
            case OrExpression oe -> executeOr(oe, estimation);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        };
    }

    private List<Integer> executeNot(NotExpression e, Map<Expression, Integer> estimation) {
        return executeNot(executeForIds(e.getSubExpression(), estimation));
    }

    private List<Integer> executeAnd(AndExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().sort(comparing(estimation::get));
        List<Integer> result = executeForIds(e.getSubExpressions().getFirst(), estimation);
        for (Expression sub : e.getSubExpressions().subList(1, e.getSubExpressions().size())) {
            if (result.isEmpty()) break;
            if (sub instanceof NotExpression ne)
                result = executeAndNot(result, executeForIds(ne.getSubExpression(), estimation));
            else
                result = executeAnd(result, executeForIds(sub, estimation));
        }
        return result;
    }

    private List<Integer> executeOr(OrExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().sort(comparing(estimation::get).reversed());
        List<Integer> result = executeForIds(e.getSubExpressions().getFirst(), estimation);
        for (Expression sub : e.getSubExpressions().subList(1, e.getSubExpressions().size())) {
            if (result.size() == index.documentsCount()) break;
            result = executeOr(result, executeForIds(sub, estimation));
        }
        return result;
    }

    private List<Integer> executeNot(List<Integer> ids) {
        return executeAndNot(index.getAllDocumentIds(), ids);
    }

    private List<Integer> executeAnd(List<Integer> left, List<Integer> right) {
        ArrayList<Integer> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).equals(right.get(r))) {
                result.add(left.get(l));
                l++;
                r++;
            }
            else if (left.get(l) < right.get(r))
                l++;
            else
                r++;
        }
        return result;
    }

    private List<Integer> executeOr(List<Integer> left, List<Integer> right) {
        ArrayList<Integer> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).equals(right.get(r))) {
                result.add(left.get(l));
                l++;
                r++;
            }
            else if (left.get(l) < right.get(r)) {
                result.add(left.get(l));
                l++;
            }
            else {
                result.add(right.get(r));
                r++;
            }
        }
        while (l < left.size()) {
            result.add(left.get(l));
            l++;
        }
        while (r < right.size()) {
            result.add(right.get(r));
            r++;
        }
        return result;
    }

    private List<Integer> executeAndNot(List<Integer> left, List<Integer> right) {
        ArrayList<Integer> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            if (left.get(l).equals(right.get(r))) {
                l++;
                r++;
            }
            else if (left.get(l) < right.get(r)) {
                result.add(left.get(l));
                l++;
            }
            else
                r++;
        }
        while (l < left.size()) {
            result.add(left.get(l));
            l++;
        }
        return result;
    }
}
