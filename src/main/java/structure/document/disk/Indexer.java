package structure.document.disk;

import document.Document;
import tokenizer.Tokenizer;

import java.util.Collection;

public interface Indexer {

    String DOCUMENTS_MAP_FILE_NAME = "documents_map";
    String VOCABULARY_FILE_NAME = "vocabulary";
    String POSTINGS_FILE_NAME = "postings";

    void index(Collection<Document> documents, Tokenizer tokenizer);
}
