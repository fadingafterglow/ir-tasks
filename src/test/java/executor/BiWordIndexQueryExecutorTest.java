package executor;

import expression.Expression;
import expression.PhraseExpression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.MethodSources;
import parser.Parser;
import structure.document.BiWordIndex;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.MocksFactory.of;

public class BiWordIndexQueryExecutorTest extends BaseQueryExecutorTest<BiWordIndexQueryExecutor> {

    public BiWordIndexQueryExecutorTest() {
        super(new BiWordIndexQueryExecutor(createIndex()));
    }

    private static BiWordIndex createIndex() {
        String[] documents = {
                "a d f h i a d h",
                "a e g h a h h",
                "a b f h j",
                "b h g n y y k",
                "h f c d e f h"
        };
        Tokenizer tokenizer = new DefaultTokenizer();
        BiWordIndex index = mock(BiWordIndex.class);
        when(index.documentsCount()).thenReturn(5);
        when(index.termsCount()).thenReturn(36);
        when(index.getDocumentName(anyInt())).thenAnswer(inv -> inv.getArgument(0).toString());
        when(index.getDocumentIds(anyString())).thenAnswer(invocation -> {
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < documents.length; i++)
                if (documents[i].contains(invocation.getArgument(0)))
                    result.add(i);
            return result;
        });
        when(index.getAllDocumentIds()).thenReturn(List.of(0, 1, 2, 3, 4));
        when(index.getDocumentFrequency(anyString())).thenAnswer(invocation -> {
            int result = 0;
            for (String document : documents)
                if (document.contains(invocation.getArgument(0)))
                    result++;
            return result;
        });
        when(index.getTokenizer()).thenReturn(tokenizer);
        when(index.getProcessedDocument(anyInt())).thenAnswer(inv -> documents[(int)inv.getArgument(0)]);
        when(index.getSeparator()).thenReturn(" ");
        return index;
    }

    @ParameterizedTest
    @MethodSources({
            @MethodSource("testMultiTermPhrase"),
            @MethodSource("testComplexWithPhrases")
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
                Arguments.of(new PhraseExpression("n y k"), List.of()),
                Arguments.of(new PhraseExpression("n y y k"), List.of("3")),
                Arguments.of(new PhraseExpression("n y y"), List.of("3")),
                Arguments.of(new PhraseExpression("y y k"), List.of("3")),
                Arguments.of(new PhraseExpression("invalid"), List.of())
        );
    }

    public static Stream<Arguments> testComplexWithPhrases() {
        return Stream.of(
                Arguments.of(new Parser("f h AND d").parse(), List.of("0", "4")),
                Arguments.of(new Parser("f h OR d").parse(), List.of("0", "2", "4")),
                Arguments.of(new Parser("f h AND NOT d").parse(), List.of("2")),
                Arguments.of(new Parser("NOT g h AND NOT invalid").parse(), List.of("0", "2", "3", "4")),
                Arguments.of(new Parser("n y y AND NOT y y k").parse(), List.of()),
                Arguments.of(new Parser("a d OR a b").parse(), List.of("0", "2")),
                Arguments.of(new Parser("d a OR b a").parse(), List.of()),
                Arguments.of(new Parser("NOT n y k").parse(), List.of("0", "1", "2", "3", "4"))
        );
    }
}
