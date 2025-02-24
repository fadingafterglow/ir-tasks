package expression;

import java.util.List;

public class TermExpression extends BaseExpression {
    private final String term;

    public TermExpression(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public List<Long> execute() {
        return List.of();
    }

    @Override
    public long estimate() {
        return 0;
    }
}
