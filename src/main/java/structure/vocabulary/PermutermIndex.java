package structure.vocabulary;

import structure.utils.MapTrie;
import structure.utils.Trie;

import java.util.ArrayList;
import java.util.List;

public class PermutermIndex extends BaseVocabularyIndex {

    private static final char END_CHAR = '$';
    private final Trie<TermMapping> trie;
    private int termsCount;

    public PermutermIndex() {
        this.trie = new MapTrie<>();
    }

    @Override
    public int addTerm(String term) {
        List<String> rotations = rotate(term);
        TermMapping current = new TermMapping(term, termsCount);
        for (String rotation : rotations) {
            TermMapping previous = trie.insertIfAbsent(rotation, current);
            if (previous != null) return previous.termId();
        }
        return termsCount++;
    }

    @Override
    public List<Integer> getTermIds(String termPattern) {
        String[] parts = split(termPattern);
        String searchPermuterm = createSearchPermuterm(parts);
        return switch (parts.length) {
            case 1 -> map(trie.search(searchPermuterm));
            case 2 -> map(trie.startsWith(searchPermuterm));
            default -> filter(trie.startsWith(searchPermuterm), parts);
        };
    }

    private List<String> rotate(String term) {
        int rotationLength = term.length() + 1;
        List<String> rotations = new ArrayList<>(rotationLength);
        char[] chars = new char[rotationLength];
        for (int i = 0; i < rotationLength; i++) {
            for (int j = 0; j < rotationLength; j++)
                chars[j] = i + j == term.length() ? END_CHAR : term.charAt((i + j) % rotationLength);
            rotations.add(new String(chars));
        }
        return rotations;
    }

    private String createSearchPermuterm(String[] parts) {
        if (parts.length == 1) return parts[0] + END_CHAR;
        return parts[parts.length - 1] + END_CHAR + parts[0];
    }
}
