package tokenizer;

import document.Document;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DefaultTokenizer implements Tokenizer {

    private static final Pattern pattern = Pattern.compile("[^\\p{L}]+['-]|['-][^\\p{L}]+|[\\s,.?!;:\"“”‘’…/\\\\—–()\\[\\]{}&|№#$%*+=^]+");

    @Override
    public List<String> tokenize(String string) {
        return pattern.splitAsStream(string.toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Override
    public List<String> tokenize(Document document) {
        return tokenize(document.getBody());
    }
}
