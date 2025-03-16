package structure.document.indexes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import structure.document.memory.MapMatrix;
import tokenizer.DefaultTokenizer;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class MapMatrixTest extends BaseSearchStructureTest<MapMatrix> {

    public MapMatrixTest() {
        super(new MapMatrix(documents, new DefaultTokenizer()));
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDocumentIds(String term, boolean[] expected) {
        assertArrayEquals(expected, searchStructure.getDocumentIds(term));
    }

    public static Stream<Arguments> testGetDocumentIds() {
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
