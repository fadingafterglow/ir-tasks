package tokenizer;

import document.Document;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DefaultTokenizer implements Tokenizer {

    private static final Pattern pattern = Pattern.compile("[^\\p{L}]{1,3}['-]|['-][^\\p{L}]{1,3}|[\\s,.?!;:\"“”‘’…/\\\\—–()\\[\\]{}&|№#$%+=^]");

    @Override
    public Stream<String> tokenizeAsStream(String string) {
        return pattern.splitAsStream(string.toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank() && s.length() < 128);
    }

    @Override
    public Stream<String> tokenizeAsStream(Document document) {
        return tokenizeAsStream(document.getBody());
    }

    @Override
    public List<String> tokenize(String string) {
        return tokenizeAsStream(string).toList();
    }

    @Override
    public List<String> tokenize(Document document) {
        return tokenize(document.getBody());
    }
}
