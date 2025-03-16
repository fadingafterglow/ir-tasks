package structure.document.indexes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import structure.document.memory.MapBiWordIndex;
import tokenizer.DefaultTokenizer;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapBiWordIndexTest extends BaseIndexTest<MapBiWordIndex> {

    public MapBiWordIndexTest() {
        super(new MapBiWordIndex(documents, new DefaultTokenizer()));
    }

    @Override
    @Test
    public void testTermsCount() {
        assertEquals(32, searchStructure.termsCount());
    }

    @ParameterizedTest
    @MethodSource
    public void testGetProcessedDocument(int documentId, String expected) {
        assertEquals(expected, searchStructure.getProcessedDocument(documentId));
    }

    public static Stream<Arguments> testGetProcessedDocument() {
        return Stream.of(
                Arguments.of("0", "a d f h i a d h"),
                Arguments.of("1", "a e g h a h h"),
                Arguments.of("2", "a b f h j"),
                Arguments.of("3", "b h g"),
                Arguments.of("4", "h f c d e f h")
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDocumentIdsByBiWord(String term, List<Integer> expected) {
        assertEquals(expected, searchStructure.getDocumentIds(term));
    }

    public static Stream<Arguments> testGetDocumentIdsByBiWord() {
        return Stream.of(
                Arguments.of("a d", List.of(0)),
                Arguments.of("f h", List.of(0, 2, 4)),
                Arguments.of("c d", List.of(4)),
                Arguments.of("h h", List.of(1)),
                Arguments.of("a a", List.of()),
                Arguments.of("a e g", List.of())
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testGetDocumentFrequencyWithBiWord(String term, int expected) {
        assertEquals(expected, searchStructure.getDocumentFrequency(term));
    }

    public static Stream<Arguments> testGetDocumentFrequencyWithBiWord() {
        return Stream.of(
                Arguments.of("a d", 1),
                Arguments.of("f h", 3),
                Arguments.of("c d", 1),
                Arguments.of("h h", 1),
                Arguments.of("a a", 0),
                Arguments.of("a e g", 0)
        );
    }
}
