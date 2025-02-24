package parser;

import expression.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    @Test
    public void testTerm() {
        Parser parser = new Parser("abc");
        TermExpression expression = (TermExpression) parser.parse();
        assertEquals("abc", expression.getTerm());
    }

    @Test
    public void testNot() {
        Parser parser = new Parser("NOT abc");
        NotExpression expression = (NotExpression) parser.parse();
        assertEquals(TermExpression.class, expression.getSubExpression().getClass());
    }

    @Test
    public void testAnd() {
        Parser parser = new Parser("abc AND asd AND zxc AND fgh");
        AndExpression expression = (AndExpression) parser.parse();
        assertEquals(4, expression.getSubExpressions().size());
    }

    @Test
    public void testOr() {
        Parser parser = new Parser("abc OR asd OR zxc OR fgh");
        OrExpression expression = (OrExpression) parser.parse();
        assertEquals(4, expression.getSubExpressions().size());
    }

    @Test
    public void testComplex() {
        Parser parser = new Parser("NOT 1 OR 2 AND NOT 3 OR (4 AND (NOT 5) OR 6)");

        OrExpression e = (OrExpression) parser.parse();
        assertEquals(3, e.getSubExpressions().size());

        NotExpression e_1 = (NotExpression) e.getSubExpressions().getFirst();
        assertEquals("1", ((TermExpression)e_1.getSubExpression()).getTerm());

        AndExpression e_2 = (AndExpression) e.getSubExpressions().get(1);
        assertEquals(2, e_2.getSubExpressions().size());
        TermExpression e_2_1 = (TermExpression) e_2.getSubExpressions().getFirst();
        assertEquals("2", e_2_1.getTerm());
        NotExpression e_2_2 = (NotExpression) e_2.getSubExpressions().getLast();
        assertEquals("3", ((TermExpression)e_2_2.getSubExpression()).getTerm());

        OrExpression e_3 = (OrExpression) e.getSubExpressions().getLast();
        assertEquals(2, e_3.getSubExpressions().size());
        AndExpression e_3_1 = (AndExpression) e_3.getSubExpressions().getFirst();
        assertEquals(2, e_3_1.getSubExpressions().size());
        TermExpression e_3_1_1 = (TermExpression) e_3_1.getSubExpressions().getFirst();
        assertEquals("4", e_3_1_1.getTerm());
        NotExpression e_3_1_2 = (NotExpression) e_3_1.getSubExpressions().getLast();
        assertEquals("5", ((TermExpression)e_3_1_2.getSubExpression()).getTerm());
        TermExpression e_3_2 = (TermExpression) e_3.getSubExpressions().getLast();
        assertEquals("6", e_3_2.getTerm());
    }
}
