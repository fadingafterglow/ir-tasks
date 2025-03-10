package structure.vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public abstract class BaseVocabularyIndex implements VocabularyIndex {

    protected String[] split(String termPattern) {
        return termPattern.split("\\*", -1);
    }

    protected List<Integer> map(TermMapping termMapping) {
        return termMapping == null ? List.of() : List.of(termMapping.termId());
    }

    protected List<Integer> map(List<TermMapping> termMappings) {
        return termMappings.stream().map(TermMapping::termId).toList();
    }

    protected <T> List<T> intersect(List<TermMapping> left, List<TermMapping> right, Function<TermMapping, T> mapper) {
        if (left.size() > right.size())
            return intersectHelper(right, left, mapper);
        return intersectHelper(left, right, mapper);
    }

    private  <T> List<T> intersectHelper(List<TermMapping> smaller, List<TermMapping> bigger, Function<TermMapping, T> mapper) {
        List<T> result = new ArrayList<>();
        bigger.sort(Comparator.naturalOrder());
        for (TermMapping t : smaller) {
            if (Collections.binarySearch(bigger, t) >= 0)
                result.add(mapper.apply(t));
        }
        return result;
    }

    protected List<Integer> filter(List<TermMapping> termMappings, String[] parts) {
        return termMappings.stream()
                .filter(termMapping -> matches(termMapping.term(), parts))
                .map(TermMapping::termId)
                .toList();
    }

    private boolean matches(String term, String[] parts) {
        int index = 0;
        for (String part : parts) {
            index = term.indexOf(part, index);
            if (index == -1) return false;
            index += part.length();
        }
        return true;
    }

    protected record TermMapping(String term, int termId) implements Comparable<TermMapping> {

        @Override
        public int compareTo(TermMapping other) {
            return termId - other.termId;
        }
    }
}
