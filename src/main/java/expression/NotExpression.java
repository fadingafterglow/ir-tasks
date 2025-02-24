package expression;

public class NotExpression implements Expression {
    private final Expression subExpression;

    public NotExpression(Expression subExpression) {
        this.subExpression = subExpression;
    }

    public Expression getSubExpression() {
        return subExpression;
    }
}
