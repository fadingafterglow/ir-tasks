package expression;

public class TermExpression implements Expression {
    private final String term;

    public TermExpression(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }
}
