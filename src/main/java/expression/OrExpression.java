package expression;

public class OrExpression extends MultiOperandExpression {

    public OrExpression(Expression left, Expression right, Expression... others) {
        super(left, right, others);
    }
}
