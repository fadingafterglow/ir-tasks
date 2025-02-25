package executor;

import expression.*;
import structures.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MatrixQueryExecutor implements QueryExecutor {

    private final Matrix matrix;

    public MatrixQueryExecutor(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    public List<String> execute(Expression query) {
        List<String> result = new ArrayList<>();
        boolean[] row = executeForRow(query);
        for (int i = 0; i < matrix.documentsCount(); i++) {
            if (row[i])
                result.add(matrix.getDocument(i));
        }
        return result;
    }

    private boolean[] executeForRow(Expression query) {
        return switch (query) {
            case TermExpression te -> executeTerm(te);
            case NotExpression ne -> executeNot(ne);
            case AndExpression ae -> executeAnd(ae);
            case OrExpression oe -> executeOr(oe);
            default -> throw new RuntimeException("Unsupported expression type: " + query.getClass());
        };
    }

    private boolean[] executeTerm(TermExpression e) {
        boolean[] row = matrix.getDocumentsRow(e.getTerm());
        return Arrays.copyOf(row, row.length);
    }

    private boolean[] executeNot(NotExpression e) {
        boolean[] row = executeForRow(e.getSubExpression());
        for (int i = 0; i < row.length; i++)
            row[i] = !row[i];
        return row;
    }

    private boolean[] executeAnd(AndExpression e) {
        LazyRowCollection rows = new LazyRowCollection(e.getSubExpressions());
        boolean[] result = rows.getResultRow();
        for (int i = 0; i < matrix.documentsCount(); i++) {
            for (int j = 0; j < e.getSubExpressions().size(); j++) {
                if (!rows.getValue(j, i)) {
                    result[i] = false;
                    break;
                }
            }
        }
        return result;
    }

    private boolean[] executeOr(OrExpression e) {
        LazyRowCollection rows = new LazyRowCollection(e.getSubExpressions());
        boolean[] result = rows.getResultRow();
        for (int i = 0; i < matrix.documentsCount(); i++) {
            for (int j = 0; j < e.getSubExpressions().size(); j++) {
                if (rows.getValue(j, i)) {
                    result[i] = true;
                    break;
                }
            }
        }
        return result;
    }

    private class LazyRowCollection {
        private final List<Expression> expressions;
        private final RowInfo[] info;

        public LazyRowCollection(List<Expression> expressions) {
            this.expressions = expressions;
            this.info = new RowInfo[expressions.size()];
            this.info[0] = new RowInfo(false, executeForRow(expressions.getFirst()));
        }

        public boolean[] getResultRow() {
            return info[0].values;
        }

        public boolean getValue(int row, int column) {
            if (info[row] == null)
                loadRow(row);
            RowInfo rowInfo = info[row];
            return rowInfo.shouldNegate() ^ rowInfo.values[column];
        }

        private void loadRow(int row) {
            Expression expression = expressions.get(row);
            if (expression instanceof NotExpression ne)
                info[row] = new RowInfo(true, executeForRow(ne.getSubExpression()));
            else
                info[row] = new RowInfo(false, executeForRow(expression));
        }

        public record RowInfo(boolean shouldNegate, boolean[] values) {}
    }
}
