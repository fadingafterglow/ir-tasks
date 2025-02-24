package parser;

import expression.*;

public class Parser {
    private final Query query;

    public Parser(String query) {
        this.query = new Query(query);
    }

    public Expression parse() throws SyntaxException {
        Expression expression = S();
        matchEnd();
        return expression;
    }

    private Expression S() throws SyntaxException {
        Expression left = T();
        return A(left);
    }

    private Expression A(Expression left) throws SyntaxException {
        if (query.currentChar() == '|') {
            query.moveNext();
            Expression right = T();
            if (left instanceof OrExpression moe)
                moe.addSubExpression(right);
            else
                left = new OrExpression(left, right);
            return A(left);
        }
        else
            return left;
    }

    private Expression T() throws SyntaxException {
        Expression left = E();
        return B(left);
    }

    private Expression B(Expression left) throws SyntaxException {
        if (query.currentChar() == '&') {
            query.moveNext();
            Expression right = E();
            if (left instanceof AndExpression moe)
                moe.addSubExpression(right);
            else
                left = new AndExpression(left, right);
            return B(left);
        }
        else
            return left;
    }

    private Expression E() throws SyntaxException {
        boolean negate = N();
        Expression inner = I();
        return negate ? new NotExpression(inner) : inner;
    }

    private boolean N() throws SyntaxException {
        if (query.currentChar() == '!') {
            query.moveNext();
            return true;
        }
        else
            return false;
    }

    private Expression I() throws SyntaxException {
        if (query.currentChar() == '(') {
            query.moveNext();
            Expression inner = S();
            match(')');
            return inner;
        }
        else
            return W();
    }

    private Expression W() throws SyntaxException {
        StringBuilder sb = new StringBuilder();
        while (isTermChar(query.currentChar())) {
            sb.append(query.currentChar());
            query.moveNext();
        }
        if (sb.isEmpty())
            throw new SyntaxException("Found empty term at " + query.currentIndex(), query.currentChar(), query.currentIndex());
        return new TermExpression(sb.toString());
    }

    private boolean isTermChar(char ch) {
        return ch != '|' && ch != '&' && ch != '!' && ch != '(' && ch != ')' && ch != Query.QUERY_END_CHARACTER;
    }

    private void match(char current) throws SyntaxException {
        if (query.currentChar() == current)
            query.moveNext();
        else
            throw new SyntaxException("Expecting \"" + current + "\", found \"" + query.currentChar() + "\" at " + query.currentIndex(),
                    query.currentChar(), query.currentIndex());
    }

    private void matchEnd() throws SyntaxException {
        if (query.currentChar() != Query.QUERY_END_CHARACTER)
            throw new SyntaxException("Expecting end of query, found \"" + query.currentChar() + "\" at " + query.currentIndex(),
                    query.currentChar(), query.currentIndex());
    }
}
