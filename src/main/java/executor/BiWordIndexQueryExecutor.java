package executor;

import expression.*;
import structure.document.BiWordIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiWordIndexQueryExecutor extends BaseIndexQueryExecutor<BiWordIndex> {

    public BiWordIndexQueryExecutor(BiWordIndex index) {
        super(index);
    }

    @Override
    protected void estimatePhrase(PhraseExpression e, Map<Expression, Integer> estimation) {
        List<String> terms = index.getTokenizer().tokenize(e.getPhrase());
        int min = index.documentsCount();
        for (String term : terms)
            min = Math.min(min, index.getDocumentFrequency(term));
        estimation.put(e, min);
    }

    @Override
    protected List<Integer> executePhrase(PhraseExpression e) {
        List<String> terms = index.getTokenizer().tokenize(e.getPhrase());
        if (terms.size() == 1)
            return index.getDocumentIds(terms.getFirst());
        List<String> searchTerms = computeSearchTerms(terms);
        List<Integer> possibleIds = computePossibleDocumentIds(searchTerms);
        return filter(possibleIds, terms);
    }

    protected List<String> computeSearchTerms(List<String> terms) {
        List<String> result = new ArrayList<>();
        for (int i = 1; i < terms.size(); i++)
            result.add(terms.get(i - 1) + index.getSeparator() + terms.get(i));
        result.sort((a, b) -> index.getDocumentFrequency(a) - index.getDocumentFrequency(b));
        return result;
    }

    protected List<Integer> computePossibleDocumentIds(List<String> searchTerms) {
        List<Integer> result = index.getDocumentIds(searchTerms.getFirst());
        for (String t : searchTerms.subList(1, searchTerms.size())) {
            if (result.isEmpty()) break;
            result = and(result, index.getDocumentIds(t));
        }
        return result;
    }

    protected List<Integer> filter(List<Integer> possibleDocumentIds, List<String> terms) {
        return possibleDocumentIds.stream()
                .filter(id -> index.getProcessedDocument(id).contains(String.join(index.getSeparator(), terms)))
                .toList();
    }
}
