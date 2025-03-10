package structure.utils;

import java.util.List;

public interface Trie<T> {

    void insert(String word, T value);

    T insertIfAbsent(String word, T value);

    T search(String word);

    List<T> startsWith(String prefix);
}
