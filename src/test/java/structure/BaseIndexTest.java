package structure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseIndexTest<I extends Index> extends BaseSearchStructureTest<I> {

    public BaseIndexTest(I searchStructure) {
        super(searchStructure);
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDocumentIds(String term, List<Integer> expected) {
        assertEquals(expected, searchStructure.getDocumentIds(term));

    }

    public static Stream<Arguments> testGetDocumentIds() {
        return Stream.of(
                Arguments.of("a", List.of(0, 1, 2)),
                Arguments.of("b", List.of(2, 3)),
                Arguments.of("c", List.of(4)),
                Arguments.of("d", List.of(0, 4)),
                Arguments.of("e", List.of(1, 4)),
                Arguments.of("f", List.of(0, 2, 4)),
                Arguments.of("g", List.of(1, 3)),
                Arguments.of("h", List.of(0, 1, 2, 3, 4)),
                Arguments.of("i", List.of(0)),
                Arguments.of("j", List.of(2)),
                Arguments.of("invalid", List.of())
        );
    }

    @Test
    public void testGetAllDocumentIds() {
        assertEquals(List.of(0, 1, 2, 3, 4), searchStructure.getAllDocumentIds());
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDocumentFrequency(String term, int expected) {
        assertEquals(expected, searchStructure.getDocumentFrequency(term));
    }

    public static Stream<Arguments> testGetDocumentFrequency() {
        return Stream.of(
                Arguments.of("a", 3),
                Arguments.of("b", 2),
                Arguments.of("c", 1),
                Arguments.of("d", 2),
                Arguments.of("e", 2),
                Arguments.of("f", 3),
                Arguments.of("g", 2),
                Arguments.of("h", 5),
                Arguments.of("i", 1),
                Arguments.of("j", 1),
                Arguments.of("invalid", 0)
        );
    }
}
