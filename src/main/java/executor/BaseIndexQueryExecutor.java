package executor;

import expression.*;
import structure.document.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

public abstract class BaseIndexQueryExecutor<T extends Index> implements QueryExecutor {

    protected final T index;

    public BaseIndexQueryExecutor(T index) {
        this.index = index;
    }

    @Override
    public List<String> execute(Expression query) {
        Map<Expression, Integer> estimation = new HashMap<>();
        estimate(query, estimation);
        return executeForIds(query, estimation).stream().map(index::getDocumentName).toList();
    }

    protected abstract void estimate(Expression query, Map<Expression, Integer> estimation);

    protected void estimatePhrase(PhraseExpression e, Map<Expression, Integer> estimation) {
        estimation.put(e, index.getDocumentFrequency(e.getPhrase()));
    }

    protected void estimateNot(NotExpression e, Map<Expression, Integer> estimation) {
        estimate(e.getSubExpression(), estimation);
        estimation.put(e, index.documentsCount() - estimation.get(e.getSubExpression()));
    }

    protected void estimateAnd(AndExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().forEach(sub -> estimate(sub, estimation));
        int min = index.documentsCount();
        for (Expression sub : e.getSubExpressions())
            min = Math.min(min, estimation.get(sub));
        estimation.put(e, min);
    }

    protected void estimateOr(OrExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().forEach(sub -> estimate(sub, estimation));
        long sum = 0;
        for (Expression sub : e.getSubExpressions())
            sum += estimation.get(sub);
        estimation.put(e, Math.clamp(sum, 0, Integer.MAX_VALUE));
    }

    protected abstract List<Integer> executeForIds(Expression query, Map<Expression, Integer> estimation);

    protected List<Integer> executePhrase(PhraseExpression e) {
        return index.getDocumentIds(e.getPhrase());
    }

    protected List<Integer> executeNot(NotExpression e, Map<Expression, Integer> estimation) {
        return not(executeForIds(e.getSubExpression(), estimation));
    }

    protected List<Integer> executeAnd(AndExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().sort(comparing(estimation::get));
        List<Integer> result = executeForIds(e.getSubExpressions().getFirst(), estimation);
        for (Expression sub : e.getSubExpressions().subList(1, e.getSubExpressions().size())) {
            if (result.isEmpty()) break;
            if (sub instanceof NotExpression ne)
                result = andNot(result, executeForIds(ne.getSubExpression(), estimation));
            else
                result = and(result, executeForIds(sub, estimation));
        }
        return result;
    }

    protected List<Integer> executeOr(OrExpression e, Map<Expression, Integer> estimation) {
        e.getSubExpressions().sort(comparing(estimation::get).reversed());
        List<Integer> result = executeForIds(e.getSubExpressions().getFirst(), estimation);
        for (Expression sub : e.getSubExpressions().subList(1, e.getSubExpressions().size())) {
            if (result.size() == index.documentsCount()) break;
            result = or(result, executeForIds(sub, estimation));
        }
        return result;
    }

    protected List<Integer> not(List<Integer> ids) {
        return andNot(index.getAllDocumentIds(), ids);
    }

    protected List<Integer> and(List<Integer> left, List<Integer> right) {
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

    protected List<Integer> or(List<Integer> left, List<Integer> right) {
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

    protected List<Integer> andNot(List<Integer> left, List<Integer> right) {
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
