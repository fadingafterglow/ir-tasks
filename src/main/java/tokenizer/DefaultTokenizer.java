package tokenizer;

import document.Document;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DefaultTokenizer implements DocumentTokenizer {

    private static final Pattern pattern = Pattern.compile("[^\\p{L}]+['-]|['-][^\\p{L}]+|[\\s,.?!;:\"“”‘’…/\\\\—–()\\[\\]{}&|№#$%*+=^]+");

    @Override
    public List<String> tokenize(Document document) {
        return Arrays.asList(pattern.split(document.getBody().toLowerCase(Locale.ROOT)));
    }
}
