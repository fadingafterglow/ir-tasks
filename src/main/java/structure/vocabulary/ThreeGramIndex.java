package structure.vocabulary;

import java.util.*;

public class ThreeGramIndex extends BaseVocabularyIndex {

    private static final char END_CHAR = '$';
    private final Map<String, List<TermMapping>> index;
    private int termsCount;

    public ThreeGramIndex() {
        this.index = new HashMap<>();
    }

    @Override
    public int addTerm(String term) {
        List<String> threeGrams = indexingThreeGrams(term);
        TermMapping current = new TermMapping(term, termsCount);
        List<TermMapping> mappings = index.computeIfAbsent(threeGrams.getFirst(), k -> new ArrayList<>());
        for (TermMapping mapping : mappings)
            if (mapping.term().equals(current.term())) return mapping.termId();
        for (String threeGram : threeGrams) {
            mappings = index.computeIfAbsent(threeGram, k -> new ArrayList<>());
            mappings.add(current);
        }
        return termsCount++;
    }

    @Override
    public List<Integer> getTermIds(String termPattern) {
        String[] parts = split(termPattern);
        List<String> threeGrams = searchThreeGrams(parts);
        if (threeGrams.isEmpty())
            return filter(allMappings(), parts);
        List<TermMapping> termMappings = index.getOrDefault(threeGrams.getFirst(), List.of());
        for (int i = 1; i < threeGrams.size(); i++) {
            if (termMappings.isEmpty()) break;
            termMappings = intersect(termMappings, index.getOrDefault(threeGrams.get(i), List.of()));
        }
        return filter(termMappings, parts);
    }

    private List<String> indexingThreeGrams(String term) {
        List<String> result = new ArrayList<>();
        if (term.length() >= 2) {
            result.add(END_CHAR + term.substring(0, 2));
            result.add(term.substring(term.length() - 2) + END_CHAR);
        }
        threeGrams(term, result);
        result.add(END_CHAR + (END_CHAR + term.substring(0, 1)));
        result.add((term.substring(term.length() - 1) + END_CHAR) + END_CHAR);
        return result;
    }

    private List<String> searchThreeGrams(String[] parts) {
        List<String> result = new ArrayList<>();
        String prefix = parts[0];
        String suffix = parts[parts.length - 1];
        if (prefix.length() == 1)
            result.add(END_CHAR + (END_CHAR + prefix));
        else if (prefix.length() > 1) {
            result.add(END_CHAR + prefix.substring(0, 2));
            threeGrams(prefix, result);
        }
        for (int i = 1; i < parts.length - 1; i++)
            threeGrams(parts[i], result);
        if (suffix.length() == 1)
            result.add((suffix + END_CHAR) + END_CHAR);
        else if (suffix.length() > 1) {
            result.add(suffix.substring(suffix.length() - 2) + END_CHAR);
            if (parts.length != 1) threeGrams(suffix, result);
        }
        return result;
    }

    private void threeGrams(String term, List<String> result) {
        if (term.length() < 3) return;
        char[] threeGram = new char[3];
        for (int i = 0; i < term.length() - 2; i++) {
            for (int j = 0; j < 3; j++)
                threeGram[j] = term.charAt(i + j);
            result.add(new String(threeGram));
        }
    }

    private List<TermMapping> allMappings() {
        List<TermMapping> result = new ArrayList<>();
        for (List<TermMapping> mappings : index.values()) {
            result = union(result, mappings);
            if (result.size() == termsCount) break;
        }
        return result;
    }

    private List<TermMapping> intersect(List<TermMapping> left, List<TermMapping> right) {
        List<TermMapping> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            TermMapping leftMapping = left.get(l);
            int comparison = leftMapping.compareTo(right.get(r));
            if (comparison == 0) {
                result.add(leftMapping);
                l++;
                r++;
            }
            else if (comparison < 0) {
                l++;
            }
            else {
                r++;
            }
        }
        return result;
    }

    private List<TermMapping> union(List<TermMapping> left, List<TermMapping> right) {
        List<TermMapping> result = new ArrayList<>();
        int l = 0, r = 0;
        while (l < left.size() && r < right.size()) {
            TermMapping leftMapping = left.get(l);
            TermMapping rightMapping = right.get(r);
            int comparison = leftMapping.compareTo(rightMapping);
            if (comparison == 0) {
                result.add(leftMapping);
                l++;
                r++;
            }
            else if (comparison < 0) {
                result.add(leftMapping);
                l++;
            }
            else {
                result.add(rightMapping);
                r++;
            }
        }
        while (l < left.size()) result.add(left.get(l++));
        while (r < right.size()) result.add(right.get(r++));
        return result;
    }
}
