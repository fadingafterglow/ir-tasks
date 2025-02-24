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
            @MethodSource("testTerm"),
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

    public static Stream<Arguments> testTerm() {
        return Stream.of(
                Arguments.of(new TermExpression("a"), List.of("0", "1", "2")),
                Arguments.of(new TermExpression("b"), List.of("2", "3")),
                Arguments.of(new TermExpression("c"), List.of("4")),
                Arguments.of(new TermExpression("invalid"), List.of())
        );
    }

    public static Stream<Arguments> testNot() {
        return Stream.of(
                Arguments.of(new NotExpression(new TermExpression("a")), List.of("3", "4")),
                Arguments.of(new NotExpression(new TermExpression("b")), List.of("0", "1", "4")),
                Arguments.of(new NotExpression(new TermExpression("c")), List.of("0", "1", "2", "3")),
                Arguments.of(new NotExpression(new TermExpression("invalid")), List.of("0", "1", "2", "3", "4"))
        );
    }

    public static Stream<Arguments> testAnd() {
        return Stream.of(
                Arguments.of(new AndExpression(new TermExpression("a"), new TermExpression("b")), List.of("2")),
                Arguments.of(new AndExpression(new TermExpression("a"), new TermExpression("c")), List.of()),
                Arguments.of(new AndExpression(new TermExpression("a"), new TermExpression("h")), List.of("0", "1", "2")),
                Arguments.of(new AndExpression(new TermExpression("d"), new TermExpression("f"), new TermExpression("e")), List.of("4")),
                Arguments.of(new AndExpression(new TermExpression("d"), new TermExpression("f"), new TermExpression("e"), new TermExpression("g")), List.of()),
                Arguments.of(new AndExpression(new TermExpression("invalid"), new TermExpression("a"), new TermExpression("b")), List.of()),
                Arguments.of(new AndExpression(new TermExpression("a"), new NotExpression(new TermExpression("c"))), List.of("0", "1", "2")),
                Arguments.of(new AndExpression(new TermExpression("c"), new NotExpression(new TermExpression("a"))), List.of("4")),
                Arguments.of(new AndExpression(new TermExpression("a"), new NotExpression(new TermExpression("h"))), List.of()),
                Arguments.of(new AndExpression(new TermExpression("h"), new NotExpression(new TermExpression("a"))), List.of("3", "4")),
                Arguments.of(new AndExpression(new TermExpression("a"), new NotExpression(new TermExpression("f"))), List.of("1")),
                Arguments.of(new AndExpression(new TermExpression("f"), new NotExpression(new TermExpression("a"))), List.of("4")),
                Arguments.of(new AndExpression(new TermExpression("d"), new TermExpression("f"), new NotExpression(new TermExpression("e"))), List.of("0")),
                Arguments.of(new AndExpression(new TermExpression("d"), new NotExpression(new TermExpression("f")), new NotExpression(new TermExpression("e"))), List.of()),
                Arguments.of(new AndExpression(new TermExpression("h"), new NotExpression(new TermExpression("d")), new NotExpression(new TermExpression("b"))), List.of("1")),
                Arguments.of(new AndExpression(new NotExpression(new TermExpression("d")), new NotExpression(new TermExpression("f")), new NotExpression(new TermExpression("e"))), List.of("3"))


        );
    }

    public static Stream<Arguments> testOr() {
        return Stream.of(
                Arguments.of(new OrExpression(new TermExpression("a"), new TermExpression("b")), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new TermExpression("a"), new TermExpression("c")), List.of("0", "1", "2", "4")),
                Arguments.of(new OrExpression(new TermExpression("a"), new TermExpression("h")), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new TermExpression("d"), new TermExpression("f"), new TermExpression("e")), List.of("0", "1", "2", "4")),
                Arguments.of(new OrExpression(new TermExpression("d"), new TermExpression("f"), new TermExpression("e"), new TermExpression("g")), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new TermExpression("invalid"), new TermExpression("a"), new TermExpression("b")), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new TermExpression("a"), new NotExpression(new TermExpression("c"))), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new TermExpression("c"), new NotExpression(new TermExpression("a"))), List.of("3", "4")),
                Arguments.of(new OrExpression(new TermExpression("a"), new NotExpression(new TermExpression("h"))), List.of("0", "1", "2")),
                Arguments.of(new OrExpression(new TermExpression("h"), new NotExpression(new TermExpression("a"))), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new TermExpression("a"), new NotExpression(new TermExpression("f"))), List.of("0", "1", "2", "3")),
                Arguments.of(new OrExpression(new TermExpression("f"), new NotExpression(new TermExpression("a"))), List.of("0", "2", "3", "4")),
                Arguments.of(new OrExpression(new TermExpression("d"), new TermExpression("f"), new NotExpression(new TermExpression("e"))), List.of("0", "2", "3", "4")),
                Arguments.of(new OrExpression(new TermExpression("d"), new NotExpression(new TermExpression("f")), new NotExpression(new TermExpression("e"))), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new TermExpression("h"), new NotExpression(new TermExpression("d")), new NotExpression(new TermExpression("b"))), List.of("0", "1", "2", "3", "4")),
                Arguments.of(new OrExpression(new NotExpression(new TermExpression("d")), new NotExpression(new TermExpression("f")), new NotExpression(new TermExpression("e"))), List.of("0", "1", "2", "3"))
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
