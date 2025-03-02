package expression;

public class ProximityExpression implements Expression {

    private final Expression left;
    private final Expression right;
    private final int proximity;

    public ProximityExpression(Expression left, Expression right, int proximity) {
        this.left = left;
        this.right = right;
        this.proximity = proximity;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public int getProximity() {
        return proximity;
    }
}
