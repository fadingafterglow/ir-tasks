package tokenizer;

import document.Document;

import java.util.List;

public interface Tokenizer {

    List<String> tokenize(String string);

    List<String> tokenize(Document document);
}
