package structure.document.indexers;

import document.Document;
import org.junit.jupiter.api.Test;
import structure.document.disk.Indexer;
import tokenizer.DefaultTokenizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static utils.MocksFactory.of;

public abstract class BaseIndexerTest<I extends Indexer> {

    protected final static List<Document> documents = List.of(
            of("0", "a D, f! H; i, a, d; H"),
            of("1", "a E' \"g h\" [a h] H"),
            of("2", "a #b f&h j"),
            of("3", "b h g"),
            of("4", "h 'f c?! d (e) f h")
    );
    protected final static Path directory = Path.of("src/test/resources/indexer");

    protected final I indexer;

    public BaseIndexerTest(I indexer) {
        this.indexer = indexer;
    }

    @Test
    public void testIndex() throws Exception {
        indexer.index(documents, new DefaultTokenizer());
        assertArrayEquals(expectedDocumentsMap(), Files.readAllBytes(directory.resolve(Indexer.DOCUMENTS_MAP_FILE_NAME)));
        assertArrayEquals(expectedVocabulary(), Files.readAllBytes(directory.resolve(Indexer.VOCABULARY_FILE_NAME)));
        assertArrayEquals(expectedPostings(), Files.readAllBytes(directory.resolve(Indexer.POSTINGS_FILE_NAME)));
    }

    protected abstract byte[] expectedDocumentsMap();

    protected abstract byte[] expectedVocabulary();

    protected abstract byte[] expectedPostings();
}
