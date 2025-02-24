package expression;

import java.util.List;

public class NotExpression extends BaseExpression {
    private final Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
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
