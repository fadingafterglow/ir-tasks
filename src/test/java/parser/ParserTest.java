package parser;

import expression.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    @Test
    public void testPhrase() {
        Parser parser = new Parser("abc");
        PhraseExpression expression = (PhraseExpression) parser.parse();
        assertEquals("abc", expression.getPhrase());
    }

    @Test
    public void testNot() {
        Parser parser = new Parser("NOT abc");
        NotExpression expression = (NotExpression) parser.parse();
        assertEquals(PhraseExpression.class, expression.getSubExpression().getClass());
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
    public void testProximity() {
        Parser parser = new Parser("abc /5/ asd");
        ProximityExpression expression = (ProximityExpression) parser.parse();
        assertEquals("abc", ((PhraseExpression)expression.getLeft()).getPhrase());
        assertEquals("asd", ((PhraseExpression)expression.getRight()).getPhrase());
        assertEquals(5, expression.getProximity());
    }

    @Test
    public void testComplex() {
        Parser parser = new Parser("NOT 1 OR 2 AND NOT 3 OR (4 AND (NOT 5) OR 6) OR (7 /3/ 8 /4/ 9 10 )");

        OrExpression e = (OrExpression) parser.parse();
        assertEquals(4, e.getSubExpressions().size());

        NotExpression e_1 = (NotExpression) e.getSubExpressions().getFirst();
        assertEquals("1", ((PhraseExpression)e_1.getSubExpression()).getPhrase());

        AndExpression e_2 = (AndExpression) e.getSubExpressions().get(1);
        assertEquals(2, e_2.getSubExpressions().size());
        PhraseExpression e_2_1 = (PhraseExpression) e_2.getSubExpressions().getFirst();
        assertEquals("2", e_2_1.getPhrase());
        NotExpression e_2_2 = (NotExpression) e_2.getSubExpressions().getLast();
        assertEquals("3", ((PhraseExpression)e_2_2.getSubExpression()).getPhrase());

        OrExpression e_3 = (OrExpression) e.getSubExpressions().get(2);
        assertEquals(2, e_3.getSubExpressions().size());
        AndExpression e_3_1 = (AndExpression) e_3.getSubExpressions().getFirst();
        assertEquals(2, e_3_1.getSubExpressions().size());
        PhraseExpression e_3_1_1 = (PhraseExpression) e_3_1.getSubExpressions().getFirst();
        assertEquals("4", e_3_1_1.getPhrase());
        NotExpression e_3_1_2 = (NotExpression) e_3_1.getSubExpressions().getLast();
        assertEquals("5", ((PhraseExpression)e_3_1_2.getSubExpression()).getPhrase());
        PhraseExpression e_3_2 = (PhraseExpression) e_3.getSubExpressions().getLast();
        assertEquals("6", e_3_2.getPhrase());

        ProximityExpression e_4 = (ProximityExpression) e.getSubExpressions().getLast();
        assertEquals("9 10", ((PhraseExpression)e_4.getRight()).getPhrase());
        ProximityExpression e_4_1 = (ProximityExpression) e_4.getLeft();
        assertEquals("7", ((PhraseExpression)e_4_1.getLeft()).getPhrase());
        assertEquals("8", ((PhraseExpression)e_4_1.getRight()).getPhrase());
    }
}
