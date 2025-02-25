package executor;

import structure.Index;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexQueryExecutorTest extends BaseQueryExecutorTest {

    public IndexQueryExecutorTest() {
        super(new IndexQueryExecutor(createIndex()));
    }

    private static Index createIndex() {
        Index index = mock(Index.class);
        when(index.documentsCount()).thenReturn(5);
        when(index.termsCount()).thenReturn(10);
        when(index.getDocument(anyInt())).thenAnswer(inv -> inv.getArgument(0).toString());
        when(index.getDocumentIds("a")).thenReturn(List.of(0, 1, 2));
        when(index.getDocumentIds("b")).thenReturn(List.of(2, 3));
        when(index.getDocumentIds("c")).thenReturn(List.of(4));
        when(index.getDocumentIds("d")).thenReturn(List.of(0, 4));
        when(index.getDocumentIds("e")).thenReturn(List.of(1, 4));
        when(index.getDocumentIds("f")).thenReturn(List.of(0, 2, 4));
        when(index.getDocumentIds("g")).thenReturn(List.of(1, 3));
        when(index.getDocumentIds("h")).thenReturn(List.of(0, 1, 2, 3, 4));
        when(index.getDocumentIds("i")).thenReturn(List.of(0));
        when(index.getDocumentIds("j")).thenReturn(List.of(2));
        when(index.getDocumentIds("invalid")).thenReturn(List.of());
        when(index.getAllDocumentIds()).thenReturn(List.of(0, 1, 2, 3, 4));
        when(index.getDocumentFrequency("a")).thenReturn(3);
        when(index.getDocumentFrequency("b")).thenReturn(2);
        when(index.getDocumentFrequency("c")).thenReturn(1);
        when(index.getDocumentFrequency("d")).thenReturn(2);
        when(index.getDocumentFrequency("e")).thenReturn(2);
        when(index.getDocumentFrequency("f")).thenReturn(3);
        when(index.getDocumentFrequency("g")).thenReturn(2);
        when(index.getDocumentFrequency("h")).thenReturn(5);
        when(index.getDocumentFrequency("i")).thenReturn(1);
        when(index.getDocumentFrequency("j")).thenReturn(1);
        when(index.getDocumentFrequency("invalid")).thenReturn(0);
        return index;
    }

}
