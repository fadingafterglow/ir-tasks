package tokenizer;

import document.Document;

import java.util.List;

public interface DocumentTokenizer {

    List<String> tokenize(Document document);
}
