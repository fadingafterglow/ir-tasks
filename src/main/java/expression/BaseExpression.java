package expression;

import executor.Executor;

public abstract class BaseExpression implements Expression {

    protected Executor executor;

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
