package expression;

import executor.Executor;

import java.util.List;

public interface Expression {

    void setExecutor(Executor executor);

    List<Long> execute();

    long estimate();
}
