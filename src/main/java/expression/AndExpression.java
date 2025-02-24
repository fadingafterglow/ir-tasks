package expression;

public class AndExpression extends MultiOperandExpression {

    public AndExpression(Expression left, Expression right, Expression... others) {
        super(left, right, others);
    }
}
