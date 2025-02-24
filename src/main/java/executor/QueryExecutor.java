package executor;

import expression.Expression;

import java.util.List;

public interface QueryExecutor {

    List<String> execute(Expression query);
}
