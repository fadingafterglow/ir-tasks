import expression.Expression;
import parser.Parser;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser("xyz OR qwe and not (abg OR NoT we)");
        Expression expression = parser.parse();
        System.out.println();
    }
}
