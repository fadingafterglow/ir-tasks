package executor;

import expression.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.MethodSources;
import parser.Parser;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseQueryExecutorTest {

    protected final QueryExecutor executor;

    public BaseQueryExecutorTest(QueryExecutor executor) {
        this.executor = executor;
    }

    @ParameterizedTest
    @MethodSources({
            @MethodSource("testPhrase"),
            @MethodSource("testNot"),
            @MethodSource("testAnd"),
            @MethodSource("testOr"),
            @MethodSource("testComplex"),
    })
    public void test(Expression expression, List<String> expected) {
        List<String> result = executor.execute(expression);
        assertEquals(expected, result);
    }

    /*
        a: 0, 1, 2
        b: 2, 3
        с: 4
        d: 0, 4
        e: 1, 4
        f: 0, 2, 4
        g: 1, 3
        h: 0, 1, 2, 3, 4
        i: 0
        j: 2
     */

    public static Stream<Arguments> testPhrase() {
        return Stream.of(
                Arguments.of(new PhraseExpression("a"), List.of("0", "1", "2")),
                Arguments.of(new PhraseExpression("b"), List.of("2", "3")),
                Arguments.of(new PhraseExpression("c"), List.of("4")),
                Arguments.of(new PhraseExpression("invalid"), List.of())
        );
    }

    public static Stream<Arguments> testNot() {
        return Stream.of(
                Arguments.of(new NotExpression(new PhraseExpression("a")), List.of("3", "4")),
                Arguments.of(new NotExpression(new PhraseExpression("b")), List.of("0", "1", "4")),
                Arguments.of(new NotExpression(new PhraseExpression("c")), List.of("0", "1", "2", "3")),
                Arguments.of(new NotExpression(new PhraseExpression("invalid")), List.of("0", "1", "2", "3", "4"))
        );
    }

    public static Stream<Arguments> testAnd() {
        return Stream.of(
                Arguments.of(new AndExpression(new PhraseExpression("a"), new PhraseExpression("b")), List.of("2")),
                Arguments.of(new AndExpression(new PhraseExpression("a"), new PhraseExpression("c")), List.of()),
                Arguments.of(new AndExpression(new PhraseExpression("a"), new PhraseExpression("h")), List.of("0", "1", "2")),
                Arguments.of(new AndExpression(new PhraseExpression("d"), new PhraseExpression("f"), new PhraseExpression("e")), List.of("4")),
                Arguments.of(new AndExpression(new PhraseExpression("d"), new PhraseExpression("f"), new PhraseExpression("e"), new PhraseExpression("g")), List.of()),
                Arguments.of(new AndExpression(new PhraseExpression("invalid"), new PhraseExpression("a"), new PhraseExpression("b")), List.of()),
                Arguments.of(new AndExpression(new PhraseExpression("a"), new NotExpression(new PhraseExpression("c"))), List.of("0", "1", "2")),
                Arguments.of(new AndExpression(new PhraseExpression("c"), new NotExpression(new PhraseExpression("a"))), List.of("4")),
                Arguments.of(new AndExpression(new PhraseExpression("a"), new NotExpression(new PhraseExpression("h"))), List.of()),
                Arguments.of(new AndExpression(new PhraseExpression("h"), new NotExpression(new PhraseExpression("a"))), List.of("3", "4")),
                Arguments.of(new AndExpression(new PhraseExpression("a"), new NotExpression(new PhraseExpression("f"))), List.of("1")),
                Arguments.of(new AndExpression(new PhraseExpression("f"), new NotExpression(new PhraseExpression("a"))), List.of("4")),
                Arguments.of(new AndExpression(new PhraseExpression("d"), new PhraseExpression("f"), new NotExpression(new PhraseExpression("e"))), List.of("0")),
                Arguments.of(new AndExpression(new PhraseExpression("d"), new NotExpression(new PhraseExpression("f")), new NotExpression(new PhraseExpression("e"))), List.of()),
                Arguments.of(new AndExpression(new PhraseExpression("h"), new NotExpression(new PhraseExpression("d")), new NotExpression(new PhraseExpression("b"))), List.of("1")),
                Arguments.of(new AndExpression(new NotExpression(new PhraseExpression("d")), new NotExpression(new PhraseExpression("f")), new NotExpression(new PhraseExpression("e"))), List.of("3"))
        );
    }

    public static Stream<Arguments> testOr() {
        return Stream.of(
                Arguments.of(new OrExpression(new PhraseExpression("a"), new PhraseExpression("b")), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new PhraseExpression("a"), new PhraseExpression("c")), List.of("0", "1", "2", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("a"), new PhraseExpression("h")), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("d"), new PhraseExpression("f"), new PhraseExpression("e")), List.of("0", "1", "2", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("d"), new PhraseExpression("f"), new PhraseExpression("e"), new PhraseExpression("g")), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("invalid"), new PhraseExpression("a"), new PhraseExpression("b")), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new PhraseExpression("a"), new NotExpression(new PhraseExpression("c"))), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new PhraseExpression("c"), new NotExpression(new PhraseExpression("a"))), List.of("3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("a"), new NotExpression(new PhraseExpression("h"))), List.of("0", "1", "2")),
                Arguments.of(new OrExpression(new PhraseExpression("h"), new NotExpression(new PhraseExpression("a"))), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("a"), new NotExpression(new PhraseExpression("f"))), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new PhraseExpression("f"), new NotExpression(new PhraseExpression("a"))), List.of("0", "2", "3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("d"), new PhraseExpression("f"), new NotExpression(new PhraseExpression("e"))), List.of("0", "2", "3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("d"), new NotExpression(new PhraseExpression("f")), new NotExpression(new PhraseExpression("e"))), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new PhraseExpression("h"), new NotExpression(new PhraseExpression("d")), new NotExpression(new PhraseExpression("b"))), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new NotExpression(new PhraseExpression("d")), new NotExpression(new PhraseExpression("f")), new NotExpression(new PhraseExpression("e"))), List.of("0", "1", "2", "3"))
        );
    }

    public static Stream<Arguments> testComplex() {
        return Stream.of(
                Arguments.of(new Parser("a AND b OR c").parse(), List.of("2", "4")),
                Arguments.of(new Parser("a AND b OR NOT c").parse(), List.of("0", "1", "2", "3")),
                Arguments.of(new Parser("a AND b OR NOT c AND d").parse(), List.of("0", "2")),
                Arguments.of(new Parser("a AND b OR NOT (d AND NOT b)").parse(), List.of("1", "2", "3")),
                Arguments.of(new Parser("(a)").parse(), List.of("0", "1", "2")),
                Arguments.of(new Parser("(d OR c) AND (i OR j)").parse(), List.of("0")),
                Arguments.of(new Parser("(d OR c) AND i AND j").parse(), List.of()),
                Arguments.of(new Parser("NOT (d OR c) AND g OR j").parse(), List.of("1", "2", "3")),
                Arguments.of(new Parser("NOT (d OR c) AND NOT g OR NOT j").parse(), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new Parser("a AND (b OR NOT (c AND NOT i AND f)) OR j").parse(), List.of("0", "1", "2")),
                Arguments.of(new Parser("NOT (NOT a)").parse(), List.of("0", "1", "2")),
                Arguments.of(new Parser("a AND b AND c OR d OR e AND f AND h").parse(), List.of("0", "4")),
                Arguments.of(new Parser("a AND b AND NOT c OR d OR e AND f AND h").parse(), List.of("0", "2", "4"))
        );
    }
}
