package structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrieTest {

    private Trie<String> trie;

    @BeforeEach
    public void setup() {
        trie = new MapTrie<>();
        trie.insert("hello", "hello");
        trie.insert("hell", "hell");
        trie.insert("helloo", "helloo");
        trie.insert("cat", "cat");
        trie.insert("car", "car");
        trie.insert("root", "root");
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "hell", "helloo", "cat", "car", "root"})
    public void testSuccessfulSearch(String word) {
        assertEquals(word, trie.search(word));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hel", "hellooo", "hella", "ham", "catty", "c", "owl", ""})
    public void testUnsuccessfulSearch(String word) {
        assertNull(trie.search(word));
    }

    @Test
    public void testSuccessfulStartsWith() {
        assertIterableEquals(List.of("hell", "hello", "helloo"), trie.startsWith("hel"));
        assertIterableEquals(List.of("hell", "hello", "helloo"), trie.startsWith("hell"));
        assertIterableEquals(List.of("hello", "helloo"), trie.startsWith("hello"));
        assertIterableEquals(List.of("car", "cat"), trie.startsWith("ca"));
        assertIterableEquals(List.of("cat"), trie.startsWith("cat"));
        assertIterableEquals(List.of("car", "cat", "root", "hell", "hello", "helloo"), trie.startsWith(""));
    }

    @Test
    public void testUnsuccessfulStartsWith() {
        assertTrue(trie.startsWith("hellooo").isEmpty());
        assertTrue(trie.startsWith("hey").isEmpty());
        assertTrue(trie.startsWith("cake").isEmpty());
    }
}
