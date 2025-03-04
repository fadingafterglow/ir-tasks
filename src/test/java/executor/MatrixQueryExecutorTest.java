package executor;

import structure.Matrix;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatrixQueryExecutorTest extends BaseQueryExecutorTest<MatrixQueryExecutor> {

    public MatrixQueryExecutorTest() {
        super(new MatrixQueryExecutor(createMatrix()));
    }

    private static Matrix createMatrix() {
        Tokenizer tokenizer = new DefaultTokenizer();
        Matrix matrix = mock(Matrix.class);
        when(matrix.documentsCount()).thenReturn(5);
        when(matrix.termsCount()).thenReturn(10);
        when(matrix.getDocumentName(anyInt())).thenAnswer(inv -> inv.getArgument(0).toString());
        when(matrix.getDocumentIds("a")).thenReturn(new boolean[]{true, true, true, false, false});
        when(matrix.getDocumentIds("b")).thenReturn(new boolean[]{false, false, true, true, false});
        when(matrix.getDocumentIds("c")).thenReturn(new boolean[]{false, false, false, false, true});
        when(matrix.getDocumentIds("d")).thenReturn(new boolean[]{true, false, false, false, true});
        when(matrix.getDocumentIds("e")).thenReturn(new boolean[]{false, true, false, false, true});
        when(matrix.getDocumentIds("f")).thenReturn(new boolean[]{true, false, true, false, true});
        when(matrix.getDocumentIds("g")).thenReturn(new boolean[]{false, true, false, true, false});
        when(matrix.getDocumentIds("h")).thenReturn(new boolean[]{true, true, true, true, true});
        when(matrix.getDocumentIds("i")).thenReturn(new boolean[]{true, false, false, false, false});
        when(matrix.getDocumentIds("j")).thenReturn(new boolean[]{false, false, true, false, false});
        when(matrix.getDocumentIds("invalid")).thenReturn(new boolean[]{false, false, false, false, false});
        when(matrix.getTokenizer()).thenReturn(tokenizer);
        return matrix;
    }

}
