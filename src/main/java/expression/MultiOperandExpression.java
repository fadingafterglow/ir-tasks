package expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiOperandExpression extends BaseExpression {
    protected final List<Expression> subExpressions;

    public MultiOperandExpression(Expression left, Expression right, Expression... others) {
        subExpressions = new ArrayList<>();
        subExpressions.add(left);
        subExpressions.add(right);
        subExpressions.addAll(Arrays.asList(others));
    }

    public void addSubExpression(Expression expression) {
        subExpressions.add(expression);
    }

    public List<Expression> getSubExpressions() {
        return subExpressions;
    }
}
