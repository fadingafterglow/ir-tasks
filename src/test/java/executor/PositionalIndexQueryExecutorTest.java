package executor;

import expression.Expression;
import expression.PhraseExpression;
import expression.ProximityExpression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.MethodSources;
import parser.Parser;
import structure.PositionalIndex;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.MocksFactory.*;

public class PositionalIndexQueryExecutorTest extends BaseQueryExecutorTest<PositionalIndexQueryExecutor> {

    public PositionalIndexQueryExecutorTest() {
        super(new PositionalIndexQueryExecutor(createIndex()));
    }

    private static PositionalIndex createIndex() {
        Tokenizer tokenizer = new DefaultTokenizer();
        PositionalIndex index = mock(PositionalIndex.class);
        when(index.documentsCount()).thenReturn(5);
        when(index.termsCount()).thenReturn(10);
        when(index.getDocumentName(anyInt())).thenAnswer(inv -> inv.getArgument(0).toString());
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
        when(index.getPositions("a")).thenReturn(List.of(of(0, 0, 5), of(1, 0, 4), of(2, 0)));
        when(index.getPositions("b")).thenReturn(List.of(of(2, 1), of(3, 0)));
        when(index.getPositions("c")).thenReturn(List.of(of(4, 2)));
        when(index.getPositions("d")).thenReturn(List.of(of(0, 1, 6), of(4, 3)));
        when(index.getPositions("e")).thenReturn(List.of(of(1, 1), of(4, 4)));
        when(index.getPositions("f")).thenReturn(List.of(of(0, 2), of(2, 2), of(4, 1, 5)));
        when(index.getPositions("g")).thenReturn(List.of(of(1, 2), of(3, 2)));
        when(index.getPositions("h")).thenReturn(List.of(of(0, 3, 7), of(1, 3, 5, 6), of(2, 3), of(3, 1), of(4, 0, 6)));
        when(index.getPositions("i")).thenReturn(List.of(of(0, 4)));
        when(index.getPositions("j")).thenReturn(List.of(of(2, 4)));
        when(index.getPositions("invalid")).thenReturn(List.of());
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
        when(index.getTokenizer()).thenReturn(tokenizer);
        return index;
    }

    @ParameterizedTest
    @MethodSources({
            @MethodSource("testMultiTermPhrase"),
            @MethodSource("testSingleProximity"),
            @MethodSource("testMultipleProximities"),
            @MethodSource("testPhraseProximity"),
            @MethodSource("testComplexWithProximityAndPhrases"),
    })
    public void additionalTests(Expression expression, List<String> expected) {
        List<String> result = executor.execute(expression);
        assertEquals(expected, result);
    }

    public static Stream<Arguments> testMultiTermPhrase() {
        return Stream.of(
                Arguments.of(new PhraseExpression("a d"), List.of("0")),
                Arguments.of(new PhraseExpression("a, d"), List.of("0")),
                Arguments.of(new PhraseExpression("a! d"), List.of("0")),
                Arguments.of(new PhraseExpression("a    D"), List.of("0")),
                Arguments.of(new PhraseExpression("d a"), List.of()),
                Arguments.of(new PhraseExpression("G; h"), List.of("1")),
                Arguments.of(new PhraseExpression("f h"), List.of("0", "2", "4")),
                Arguments.of(new PhraseExpression("f h j"), List.of("2")),
                Arguments.of(new PhraseExpression("f h i"), List.of("0")),
                Arguments.of(new PhraseExpression("A E H"), List.of()),
                Arguments.of(new PhraseExpression("a D, f! H; i, a, d; H"), List.of("0")),
                Arguments.of(new PhraseExpression("invalid"), List.of())
        );
    }

    public static Stream<Arguments> testSingleProximity() {
        return Stream.of(
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("d"), 2), List.of("0")),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("g"), 1), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("h"), new PhraseExpression("g"), 1), List.of("1", "3")),
                Arguments.of(new ProximityExpression(new PhraseExpression("h"), new PhraseExpression("g"), 3), List.of("1", "3")),
                Arguments.of(new ProximityExpression(new PhraseExpression("h"), new PhraseExpression("g"), 100), List.of("1", "3")),
                Arguments.of(new ProximityExpression(new PhraseExpression("g"), new PhraseExpression("h"), 1), List.of("1", "3")),
                Arguments.of(new ProximityExpression(new PhraseExpression("g"), new PhraseExpression("h"), 3), List.of("1", "3")),
                Arguments.of(new ProximityExpression(new PhraseExpression("g"), new PhraseExpression("h"), 100), List.of("1", "3")),
                Arguments.of(new ProximityExpression(new PhraseExpression("g"), new PhraseExpression("g"), 1), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("h"), new PhraseExpression("h"), 1), List.of("1")),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("a"), 1), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("a"), 2), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("a"), 3), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("a"), 4), List.of("1")),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("a"), 5), List.of("0", "1")),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("a"), 6), List.of("0", "1")),
                Arguments.of(new ProximityExpression(new PhraseExpression("invalid"), new PhraseExpression("a"), 1), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("a"), new PhraseExpression("invalid"), 5), List.of()),
                Arguments.of(new ProximityExpression(new PhraseExpression("invalid"), new PhraseExpression("invalid"), 10), List.of())
        );
    }

    public static Stream<Arguments> testMultipleProximities() {
        return Stream.of(
                Arguments.of(new Parser("a /3/ f /5/ i").parse(), List.of("0")),
                Arguments.of(new Parser("g /1/ h /1/ b").parse(), List.of("3")),
                Arguments.of(new Parser("g /1/ h /1/ a").parse(), List.of("1")),
                Arguments.of(new Parser("f /2/ d /10/ h").parse(), List.of("0", "4")),
                Arguments.of(new Parser("f /2/ d /1/ h").parse(), List.of("0", "4")),
                Arguments.of(new Parser("f /2/ d /10/ c").parse(), List.of("4")),
                Arguments.of(new Parser("b /2/ h /10/ f").parse(), List.of()),
                Arguments.of(new Parser("h /10/ h /100/ e").parse(), List.of("1")),
                Arguments.of(new Parser("f /2/ h /10/ i /3/ y").parse(), List.of()),
                Arguments.of(new Parser("f /2/ h /10/ i /3/ a").parse(), List.of("0")),
                Arguments.of(new Parser("h /5/ h /5/ h").parse(), List.of("1")),
                Arguments.of(new Parser("h /5/ invalid /5/ h").parse(), List.of()),
                Arguments.of(new Parser("invalid /10/ h /10/ h").parse(), List.of()),
                Arguments.of(new Parser("h /10/ h /10/ invalid").parse(), List.of())
        );
    }

    public static Stream<Arguments> testPhraseProximity() {
        return Stream.of(
                Arguments.of(new Parser("d f /3/ i a").parse(), List.of("0")),
                Arguments.of(new Parser("i a /3/ d f").parse(), List.of("0")),
                Arguments.of(new Parser("d f /1/ i a").parse(), List.of()),
                Arguments.of(new Parser("i a /1/ d f").parse(), List.of()),
                Arguments.of(new Parser("f h /1/ d").parse(), List.of("0")),
                Arguments.of(new Parser("f h /2/ d").parse(), List.of("0", "4")),
                Arguments.of(new Parser("f h /10/ d").parse(), List.of("0", "4")),
                Arguments.of(new Parser("d /1/ f h").parse(), List.of("0")),
                Arguments.of(new Parser("d /2/ f h").parse(), List.of("0", "4")),
                Arguments.of(new Parser("d /10/ f h").parse(), List.of("0", "4")),
                Arguments.of(new Parser("d /10/ f h /1/ a").parse(), List.of("0")),
                Arguments.of(new Parser("d /10/ f h /1/ f c").parse(), List.of("4")),
                Arguments.of(new Parser("f h /3/ f c /10/ d").parse(), List.of()),
                Arguments.of(new Parser("f h i /2/ h").parse(), List.of()),
                Arguments.of(new Parser("f h i /3/ h").parse(), List.of("0")),
                Arguments.of(new Parser("h /5/ f h i").parse(), List.of("0")),
                Arguments.of(new Parser("a e /2/ g h /1/ a h").parse(), List.of("1")),
                Arguments.of(new Parser("a e /2/ g h /1/ invalid").parse(), List.of())
        );
    }

    public static Stream<Arguments> testComplexWithProximityAndPhrases() {
        return Stream.of(
                Arguments.of(new Parser("f h AND d").parse(), List.of("0", "4")),
                Arguments.of(new Parser("f h OR d").parse(), List.of("0", "2", "4")),
                Arguments.of(new Parser("f h AND NOT d").parse(), List.of("2")),
                Arguments.of(new Parser("g /1/ h AND b").parse(), List.of("3")),
                Arguments.of(new Parser("g /1/ h AND a h").parse(), List.of("1")),
                Arguments.of(new Parser("g /1/ h AND a h OR i").parse(), List.of("0", "1")),
                Arguments.of(new Parser("NOT g /1/ h").parse(), List.of("0", "2", "4")),
                Arguments.of(new Parser("NOT g /1/ h AND NOT f /2/ a").parse(), List.of("4")),
                Arguments.of(new Parser("NOT g h AND NOT f /2/ a").parse(), List.of("3", "4")),
                Arguments.of(new Parser("NOT g h AND NOT invalid").parse(), List.of("0", "2", "3", "4"))
        );
    }
}
