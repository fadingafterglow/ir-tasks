package structure.vocabulary;

import structure.utils.MapTrie;
import structure.utils.Trie;

import java.util.List;
import java.util.function.Function;

public class TwoTriesIndex extends BaseVocabularyIndex {

    private final Trie<TermMapping> trie;
    private final Trie<TermMapping> reverseTrie;
    private int termsCount;

    public TwoTriesIndex() {
        this.trie = new MapTrie<>();
        this.reverseTrie = new MapTrie<>();
    }

    @Override
    public int addTerm(String term) {
        TermMapping current = new TermMapping(term, termsCount);
        TermMapping previous = trie.insertIfAbsent(term, current);
        if (previous != null) return previous.termId();
        reverseTrie.insert(reverse(term), current);
        return termsCount++;
    }

    @Override
    public List<Integer> getTermIds(String termPattern) {
        String[] parts = split(termPattern);
        return switch (parts.length) {
            case 1 -> handleNoWildcards(termPattern);
            case 2 -> handleOneWildcard(parts);
            default -> handleNWildcards(parts);
        };
    }

    private List<Integer> handleNoWildcards(String termPattern) {
        TermMapping termMapping = trie.search(termPattern);
        return termMapping == null ? List.of() : List.of(termMapping.termId());
    }

    private List<Integer> handleOneWildcard(String[] parts) {
        String prefix = parts[0];
        String suffix = parts[1];
        if (prefix.isEmpty())
            return map(reverseTrie.startsWith(reverse(suffix)));
        if (suffix.isEmpty())
            return map(trie.startsWith(prefix));
        return intersect(trie.startsWith(prefix), reverseTrie.startsWith(reverse(suffix)), TermMapping::termId);
    }

    private List<Integer> handleNWildcards(String[] parts) {
        String prefix = parts[0];
        String suffix = parts[parts.length - 1];
        List<TermMapping> mappings;
        if (prefix.isEmpty())
            mappings = reverseTrie.startsWith(reverse(suffix));
        else if (suffix.isEmpty())
            mappings = trie.startsWith(prefix);
        else
            mappings = intersect(trie.startsWith(prefix), reverseTrie.startsWith(reverse(suffix)), Function.identity());
        return filter(mappings, parts);
    }

    private String reverse(String term) {
        return new StringBuilder(term).reverse().toString();
    }
}
