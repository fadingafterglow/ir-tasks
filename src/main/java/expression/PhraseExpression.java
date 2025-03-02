package expression;

public class PhraseExpression implements Expression {
    private final String phrase;

    public PhraseExpression(String phrase) {
        this.phrase = phrase;
    }

    public String getPhrase() {
        return phrase;
    }
}
