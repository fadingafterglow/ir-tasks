package parser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Query {
    public static final char QUERY_END_CHARACTER = '\0';
    private final static Pattern REPLACE_PATTERN = Pattern.compile("\\bAND\\b|\\bOR\\b|\\bNOT\\b|[\\s,.?!;:\"&|]");
    private final String body;
    private int index;

    public Query(String body) {
        this.body = prepareQuery(body);
        if (this.body.isEmpty())
            throw new SyntaxException("Query is empty", QUERY_END_CHARACTER, 0);
    }

    private String prepareQuery(String body) {
        return REPLACE_PATTERN.matcher(body).replaceAll(mr ->
            switch (mr.group()) {
                case "AND" -> "&";
                case "OR" -> "|";
                case "NOT" -> "!";
                default -> "";
            }
        ).toLowerCase(Locale.ROOT);
    }

    public String getBody() {
        return body;
    }

    public char currentChar(){
        return index < body.length() ? body.charAt(index) : QUERY_END_CHARACTER;
    }

    public int currentIndex(){
        return index;
    }

    public void moveNext(){
        index++;
    }
}
