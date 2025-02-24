package expression;

import java.util.List;

public class AndExpression extends MultiOperandExpression {

    public AndExpression(Expression left, Expression right, Expression... others) {
        super(left, right, others);
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
