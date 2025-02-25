package structure;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import tokenizer.DefaultTokenizer;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapMatrixTest extends BaseStructureTest {

    private static MapMatrix matrix;

    @BeforeAll
    public static void setUp() {
        matrix = new MapMatrix(documents, new DefaultTokenizer());
    }

    @Test
    public void testDocumentsCount() {
        assertEquals(5, matrix.documentsCount());
    }

    @Test
    public void testTermsCount() {
        assertEquals(10, matrix.termsCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    public void testGetDocument(int id) {
        assertEquals(String.valueOf(id), matrix.getDocument(id));
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDocumentsRow(String term, boolean[] expected) {
        assertArrayEquals(expected, matrix.getDocumentsRow(term));
    }

    public static Stream<Arguments> testGetDocumentsRow() {
        return Stream.of(
                Arguments.of("a", new boolean[]{true, true, true, false, false}),
                Arguments.of("b", new boolean[]{false, false, true, true, false}),
                Arguments.of("c", new boolean[]{false, false, false, false, true}),
                Arguments.of("d", new boolean[]{true, false, false, false, true}),
                Arguments.of("e", new boolean[]{false, true, false, false, true}),
                Arguments.of("f", new boolean[]{true, false, true, false, true}),
                Arguments.of("g", new boolean[]{false, true, false, true, false}),
                Arguments.of("h", new boolean[]{true, true, true, true, true}),
                Arguments.of("i", new boolean[]{true, false, false, false, false}),
                Arguments.of("j", new boolean[]{false, false, true, false, false}),
                Arguments.of("invalid", new boolean[]{false, false, false, false, false})
        );
    }
}
