package structure.vocabulary;

import java.util.*;

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

    protected List<Integer> filter(List<TermMapping> termMappings, String[] parts) {
        return termMappings.stream()
                .filter(termMapping -> matches(termMapping.term(), parts))
                .map(TermMapping::termId)
                .toList();
    }

    private boolean matches(String term, String[] parts) {
        int index = term.indexOf(parts[0]);
        if (index != 0) return false;
        index += parts[0].length();
        for (int i = 1; i < parts.length - 1; i++) {
            index = term.indexOf(parts[i], index);
            if (index == -1) return false;
            index += parts[i].length();
        }
        int suffixIndex = term.lastIndexOf(parts[parts.length - 1]);
        if (suffixIndex < index && parts.length != 1) return false;
        index = suffixIndex + parts[parts.length - 1].length();
        return index == term.length();
    }

    protected record TermMapping(String term, int termId) implements Comparable<TermMapping> {

        @Override
        public int compareTo(TermMapping other) {
            return termId - other.termId;
        }
    }
}
