package parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryTest {

    @ParameterizedTest
    @MethodSource
    public void testPreprocessing(String input, String expected) {
        Query query = new Query(input);
        assertEquals(expected, query.getBody());
    }

    private static Stream<Arguments> testPreprocessing() {
        return Stream.of(
            Arguments.of("xyz OR qwe AND NOT (abg OR NOT we)", "xyz|qwe&!(abg|!we)"),
            Arguments.of("abc OR and", "abc|and"),
            Arguments.of("abc OR ANDREW", "abc|andrew"),
            Arguments.of("abc AND notnotnot", "abc&notnotnot"),
            Arguments.of("abc AND NOT notnotnot", "abc&!notnotnot"),
            Arguments.of("NOT abc AND NOT notnotnot", "!abc&!notnotnot")
        );
    }
}
