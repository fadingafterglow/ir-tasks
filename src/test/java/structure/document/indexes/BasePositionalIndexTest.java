package structure.document.indexes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import structure.document.PositionalIndex;
import utils.MocksFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BasePositionalIndexTest<T extends PositionalIndex> extends BaseIndexTest<PositionalIndex> {

    public BasePositionalIndexTest(T searchStructure) {
        super(searchStructure);
    }

    @ParameterizedTest
    @MethodSource
    public void testGetPositions(String term, List<PositionalIndex.Entry> expected) {
        assertEquals(expected, searchStructure.getPositions(term));
    }

    public static Stream<Arguments> testGetPositions() {
        return Stream.of(
                Arguments.of("a", List.of(MocksFactory.ofP(0, 0, 5), MocksFactory.ofP(1, 0, 4), MocksFactory.ofP(2, 0))),
                Arguments.of("b", List.of(MocksFactory.ofP(2, 1), MocksFactory.ofP(3, 0))),
                Arguments.of("c", List.of(MocksFactory.ofP(4, 2))),
                Arguments.of("d", List.of(MocksFactory.ofP(0, 1, 6), MocksFactory.ofP(4, 3))),
                Arguments.of("e", List.of(MocksFactory.ofP(1, 1), MocksFactory.ofP(4, 4))),
                Arguments.of("f", List.of(MocksFactory.ofP(0, 2), MocksFactory.ofP(2, 2), MocksFactory.ofP(4, 1, 5))),
                Arguments.of("g", List.of(MocksFactory.ofP(1, 2), MocksFactory.ofP(3, 2))),
                Arguments.of("h", List.of(MocksFactory.ofP(0, 3, 7), MocksFactory.ofP(1, 3, 5, 6), MocksFactory.ofP(2, 3), MocksFactory.ofP(3, 1), MocksFactory.ofP(4, 0, 6))),
                Arguments.of("i", List.of(MocksFactory.ofP(0, 4))),
                Arguments.of("j", List.of(MocksFactory.ofP(2, 4))),
                Arguments.of("invalid", List.of())
        );
    }
}
