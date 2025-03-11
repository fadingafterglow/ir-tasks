package structure.vocabulary;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseVocabularyIndexTest<V extends VocabularyIndex> {

    protected static final List<String> terms = List.of(
            "apple",
            "ape",
            "cat",
            "car",
            "cake",
            "hell",
            "hello",
            "hi",
            "irrelevant",
            "irreversible",
            "ignorant",
            "comedydy"
    );

    protected final Supplier<V> vocabularyIndexSupplier;

    public BaseVocabularyIndexTest(Supplier<V> vocabularyIndexSupplier) {
        this.vocabularyIndexSupplier = vocabularyIndexSupplier;
    }

    @Test
    public void testAddTerm() {
        V vocabularyIndex = vocabularyIndexSupplier.get();
        for (int i = 0; i < terms.size(); i++)
            assertEquals(i, vocabularyIndex.addTerm(terms.get(i)));
        for (int i = terms.size() - 1; i >= 0; i--)
            assertEquals(i, vocabularyIndex.addTerm(terms.get(i)));
    }

    @Test
    public void testGetTermIds() {
        V vocabularyIndex = vocabularyIndexSupplier.get();
        terms.forEach(vocabularyIndex::addTerm);

        assertSame(Set.of(), vocabularyIndex.getTermIds("a"));
        assertSame(Set.of(0), vocabularyIndex.getTermIds("apple"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("appl"));
        assertSame(Set.of(1), vocabularyIndex.getTermIds("ape"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("apex"));
        assertSame(Set.of(2), vocabularyIndex.getTermIds("cat"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("ct"));
        assertSame(Set.of(10), vocabularyIndex.getTermIds("ignorant"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("igorant"));
        assertSame(Set.of(0, 1), vocabularyIndex.getTermIds("a*"));
        assertSame(Set.of(6), vocabularyIndex.getTermIds("*o"));
        assertSame(Set.of(2, 3, 4), vocabularyIndex.getTermIds("ca*"));
        assertSame(Set.of(4), vocabularyIndex.getTermIds("ca*e"));
        assertSame(Set.of(4), vocabularyIndex.getTermIds("ca*e*"));
        assertSame(Set.of(9), vocabularyIndex.getTermIds("i*re*e"));
        assertSame(Set.of(8, 9), vocabularyIndex.getTermIds("i*re*e*"));
        assertSame(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), vocabularyIndex.getTermIds("*"));
        assertSame(Set.of(0, 5, 6, 8, 9), vocabularyIndex.getTermIds("*l*"));
        assertSame(Set.of(5, 6), vocabularyIndex.getTermIds("*ll*"));
        assertSame(Set.of(8, 10), vocabularyIndex.getTermIds("i*nt"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("hel"));
        assertSame(Set.of(5), vocabularyIndex.getTermIds("hell"));
        assertSame(Set.of(5, 6), vocabularyIndex.getTermIds("hel*"));
        assertSame(Set.of(5, 6), vocabularyIndex.getTermIds("hell*"));
        assertSame(Set.of(5, 6), vocabularyIndex.getTermIds("hell**"));
        assertSame(Set.of(5, 6), vocabularyIndex.getTermIds("hell***"));
        assertSame(Set.of(0, 9), vocabularyIndex.getTermIds("*le"));
        assertSame(Set.of(0, 9), vocabularyIndex.getTermIds("**le"));
        assertSame(Set.of(0, 9), vocabularyIndex.getTermIds("***le"));
        assertSame(Set.of(0, 1), vocabularyIndex.getTermIds("a*e"));
        assertSame(Set.of(0, 1), vocabularyIndex.getTermIds("a**e"));
        assertSame(Set.of(0, 1), vocabularyIndex.getTermIds("a***e"));
        assertSame(Set.of(8), vocabularyIndex.getTermIds("i*le*nt"));
        assertSame(Set.of(8), vocabularyIndex.getTermIds("i**le*nt"));
        assertSame(Set.of(8), vocabularyIndex.getTermIds("i*le**nt"));
        assertSame(Set.of(8), vocabularyIndex.getTermIds("i**le**nt"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("i*k*nt"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("i*k**nt"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("i**k*nt"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("i**k**nt"));
        assertSame(Set.of(6), vocabularyIndex.getTermIds("h*e*l*l*o"));
        assertSame(Set.of(6), vocabularyIndex.getTermIds("*h*e*l*l*o"));
        assertSame(Set.of(6), vocabularyIndex.getTermIds("h*e*l*l*o*"));
        assertSame(Set.of(6), vocabularyIndex.getTermIds("*h*e*l*l*o*"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("ir*re*re*"));
        assertSame(Set.of(10), vocabularyIndex.getTermIds("*g*"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("*g*g*"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("ca*ca*ke"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("ca*ke*ke"));
        assertSame(Set.of(), vocabularyIndex.getTermIds("comedy"));
    }

    private void assertSame(Set<Integer> expected, List<Integer> actual) {
        assertEquals(expected.size(), actual.size());
        for (Integer integer : actual)
            assertTrue(expected.contains(integer));
    }
}
