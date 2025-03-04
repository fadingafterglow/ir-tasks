package executor;

import expression.*;
import structure.Index;

import java.util.*;

public class IndexQueryExecutor extends BaseIndexQueryExecutor<Index> {

    public IndexQueryExecutor(Index index) {
        super(index);
    }

    @Override
    protected void estimate(Expression query, Map<Expression, Integer> estimation) {
        switch (query) {
            case PhraseExpression pe -> estimatePhrase(pe, estimation);
            case NotExpression ne -> estimateNot(ne, estimation);
            case AndExpression ae -> estimateAnd(ae, estimation);
            case OrExpression oe -> estimateOr(oe, estimation);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        }
    }

    @Override
    protected List<Integer> executeForIds(Expression query, Map<Expression, Integer> estimation) {
        return switch (query) {
            case PhraseExpression pe -> executePhrase(pe);
            case NotExpression ne -> executeNot(ne, estimation);
            case AndExpression ae -> executeAnd(ae, estimation);
            case OrExpression oe -> executeOr(oe, estimation);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        };
    }
}
