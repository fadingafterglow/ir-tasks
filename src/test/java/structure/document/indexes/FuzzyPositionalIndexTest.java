package structure.document.indexes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import structure.document.PositionalIndex;
import structure.document.memory.FuzzyPositionalIndex;
import structure.vocabulary.VocabularyIndex;
import tokenizer.DefaultTokenizer;
import utils.MocksFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class FuzzyPositionalIndexTest extends BasePositionalIndexTest<FuzzyPositionalIndex> {

    public FuzzyPositionalIndexTest() {
        super(new FuzzyPositionalIndex(documents, new DefaultTokenizer(), createVocabularyIndex()));
    }

    private static VocabularyIndex createVocabularyIndex() {
        VocabularyIndex vocabularyIndex = Mockito.mock(VocabularyIndex.class);
        when(vocabularyIndex.addTerm(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().charAt(0) - 'a');
        when(vocabularyIndex.getTermIds("a")).thenReturn(List.of(0));
        when(vocabularyIndex.getTermIds("b")).thenReturn(List.of(1));
        when(vocabularyIndex.getTermIds("c")).thenReturn(List.of(2));
        when(vocabularyIndex.getTermIds("d")).thenReturn(List.of(3));
        when(vocabularyIndex.getTermIds("e")).thenReturn(List.of(4));
        when(vocabularyIndex.getTermIds("f")).thenReturn(List.of(5));
        when(vocabularyIndex.getTermIds("g")).thenReturn(List.of(6));
        when(vocabularyIndex.getTermIds("h")).thenReturn(List.of(7));
        when(vocabularyIndex.getTermIds("i")).thenReturn(List.of(8));
        when(vocabularyIndex.getTermIds("j")).thenReturn(List.of(9));
        when(vocabularyIndex.getTermIds("invalid")).thenReturn(List.of());
        when(vocabularyIndex.getTermIds("*1")).thenReturn(List.of(0, 5, 7));
        when(vocabularyIndex.getTermIds("*2")).thenReturn(List.of(2, 9));
        when(vocabularyIndex.getTermIds("*3")).thenReturn(List.of(4, 6));
        when(vocabularyIndex.getTermIds("*4")).thenReturn(List.of(6, 4));
        return vocabularyIndex;
    }

    @ParameterizedTest
    @MethodSource
    public void testGetFuzzyPositions(String term, List<PositionalIndex.Entry> expected) {
        assertEquals(expected, searchStructure.getPositions(term));
    }

    public static Stream<Arguments> testGetFuzzyPositions() {
        return Stream.of(
                Arguments.of("*1", List.of(MocksFactory.ofP(0, 0, 2, 3, 5, 7), MocksFactory.ofP(1, 0, 3, 4, 5, 6), MocksFactory.ofP(2, 0, 2, 3), MocksFactory.ofP(3, 1), MocksFactory.ofP(4, 0, 1, 5, 6))),
                Arguments.of("*2", List.of(MocksFactory.ofP(2, 4), MocksFactory.ofP(4, 2))),
                Arguments.of("*3", List.of(MocksFactory.ofP(1, 1, 2), MocksFactory.ofP(3, 2), MocksFactory.ofP(4, 4))),
                Arguments.of("*4", List.of(MocksFactory.ofP(1, 1, 2), MocksFactory.ofP(3, 2), MocksFactory.ofP(4, 4)))
        );
    }
}
