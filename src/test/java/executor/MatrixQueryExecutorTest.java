package executor;

import structure.Matrix;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatrixQueryExecutorTest extends BaseQueryExecutorTest {

    public MatrixQueryExecutorTest() {
        super(new MatrixQueryExecutor(createMatrix()));
    }

    private static Matrix createMatrix() {
        Matrix matrix = mock(Matrix.class);
        when(matrix.documentsCount()).thenReturn(5);
        when(matrix.termsCount()).thenReturn(10);
        when(matrix.getDocument(anyInt())).thenAnswer(inv -> inv.getArgument(0).toString());
        when(matrix.getDocumentsRow("a")).thenReturn(new boolean[]{true, true, true, false, false});
        when(matrix.getDocumentsRow("b")).thenReturn(new boolean[]{false, false, true, true, false});
        when(matrix.getDocumentsRow("c")).thenReturn(new boolean[]{false, false, false, false, true});
        when(matrix.getDocumentsRow("d")).thenReturn(new boolean[]{true, false, false, false, true});
        when(matrix.getDocumentsRow("e")).thenReturn(new boolean[]{false, true, false, false, true});
        when(matrix.getDocumentsRow("f")).thenReturn(new boolean[]{true, false, true, false, true});
        when(matrix.getDocumentsRow("g")).thenReturn(new boolean[]{false, true, false, true, false});
        when(matrix.getDocumentsRow("h")).thenReturn(new boolean[]{true, true, true, true, true});
        when(matrix.getDocumentsRow("i")).thenReturn(new boolean[]{true, false, false, false, false});
        when(matrix.getDocumentsRow("j")).thenReturn(new boolean[]{false, false, true, false, false});
        when(matrix.getDocumentsRow("invalid")).thenReturn(new boolean[]{false, false, false, false, false});
        return matrix;
    }

}
