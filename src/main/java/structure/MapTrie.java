package structure;

import java.util.*;

public class MapTrie<T> implements Trie<T> {

    private final Node<T> root;

    public MapTrie() {
        root = new Node<>();
    }

    @Override
    public void insert(String word, T value) {
        requireNotBlank(word);
        Node<T> previous = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Node<T> current = previous.getChild(c);
            if (current == null)
                current = previous.addChild(c);
            if (i == word.length() - 1) {
                current.isEndOfWord = true;
                current.value = value;
            }
            previous = current;
        }
    }

    @Override
    public T search(String word) {
        Node<T> current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            current = current.getChild(c);
            if (current == null)
                break;
            else if (i == word.length() - 1 && current.isEndOfWord)
                return current.value;
        }
        return null;
    }

    @Override
    public List<T> startsWith(String prefix) {
        Node<T> current = root;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            current = current.getChild(c);
            if (current == null)
                return List.of();
            else if (i == prefix.length() - 1)
                break;
        }
        return startsWith(current);
    }

    private List<T> startsWith(Node<T> node) {
        List<T> result = new ArrayList<>();
        Queue<Node<T>> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node<T> current = queue.poll();
            if (current.isEndOfWord)
                result.add(current.value);
            queue.addAll(current.getChildren());
        }
        return result;
    }

    private void requireNotBlank(String word) {
        if (word == null || word.isBlank())
            throw new IllegalArgumentException("Word cannot be blank");
    }

    private static class Node<T> {

        private final Map<Character, Node<T>> children;
        private T value;
        private boolean isEndOfWord;

        public Node() {
            children = new HashMap<>();
        }

        public Node<T> addChild(char c) {
            Node<T> child = new Node<>();
            children.put(c, child);
            return child;
        }

        public Node<T> getChild(char c) {
            return children.get(c);
        }

        public Collection<Node<T>> getChildren() {
            return children.values();
        }
    }
}
