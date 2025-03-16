package tokenizer;

import document.Document;

import java.util.List;
import java.util.stream.Stream;

public interface Tokenizer {

    Stream<String> tokenizeAsStream(String string);

    Stream<String> tokenizeAsStream(Document document);

    List<String> tokenize(String string);

    List<String> tokenize(Document document);
}
