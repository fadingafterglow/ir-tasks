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
            if (left instanceof OrExpression oe)
                oe.addSubExpression(right);
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
            if (left instanceof AndExpression ae)
                ae.addSubExpression(right);
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
        else {
            Expression left = W();
            return C(left);
        }
    }

    private Expression C(Expression left) throws SyntaxException {
        if (query.currentChar() == '/') {
            query.moveNext();
            int proximity = Q();
            match('/');
            Expression right = W();
            return C(new ProximityExpression(left, right, proximity));
        }
        else
            return left;
    }

    private int Q() throws SyntaxException {
        int proximity = 0;
        while (Character.isDigit(query.currentChar())) {
            proximity = proximity * 10 + (query.currentChar() - '0');
            query.moveNext();
        }
        if (proximity == 0)
            throw new SyntaxException("Found zero or empty proximity number at " + query.currentIndex(), query.currentChar(), query.currentIndex());
        return proximity;
    }

    private Expression W() throws SyntaxException {
        StringBuilder sb = new StringBuilder();
        while (!isReservedChar(query.currentChar())) {
            sb.append(query.currentChar());
            query.moveNext();
        }
        if (sb.isEmpty())
            throw new SyntaxException("Found empty term at " + query.currentIndex(), query.currentChar(), query.currentIndex());
        return new PhraseExpression(sb.toString());
    }

    private boolean isReservedChar(char ch) {
        return ch == '|' || ch == '&' || ch == '!' || ch == '(' || ch == ')' || ch == '/' || ch == Query.QUERY_END_CHARACTER;
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
