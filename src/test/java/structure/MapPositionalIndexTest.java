package structure;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tokenizer.DefaultTokenizer;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.MocksFactory.*;

public class MapPositionalIndexTest extends BaseIndexTest<MapPositionalIndex> {

    public MapPositionalIndexTest() {
        super(new MapPositionalIndex(documents, new DefaultTokenizer()));
    }

    @ParameterizedTest
    @MethodSource
    public void testGetPositions(String term, List<PositionalIndex.Entry> expected) {
        assertEquals(expected, searchStructure.getPositions(term));
    }

    public static Stream<Arguments> testGetPositions() {
        return Stream.of(
                Arguments.of("a", List.of(of(0, 0, 5), of(1, 0, 4), of(2, 0))),
                Arguments.of("b", List.of(of(2, 1), of(3, 0))),
                Arguments.of("c", List.of(of(4, 2))),
                Arguments.of("d", List.of(of(0, 1, 6), of(4, 3))),
                Arguments.of("e", List.of(of(1, 1), of(4, 4))),
                Arguments.of("f", List.of(of(0, 2), of(2, 2), of(4, 1, 5))),
                Arguments.of("g", List.of(of(1, 2), of(3, 2))),
                Arguments.of("h", List.of(of(0, 3, 7), of(1, 3, 5, 6), of(2, 3), of(3, 1), of(4, 0, 6))),
                Arguments.of("i", List.of(of(0, 4))),
                Arguments.of("j", List.of(of(2, 4))),
                Arguments.of("invalid", List.of())
        );
    }
}
