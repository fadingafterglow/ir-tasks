package structure.document.indexers;

import document.Document;
import org.junit.jupiter.api.Test;
import structure.document.disk.Indexer;
import tokenizer.DefaultTokenizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static utils.MocksFactory.of;

public abstract class BaseIndexerTest<I extends Indexer> {

    protected final static List<Document> documents = List.of(
            of("0", "ape, apple help! hello? -Helsing :cool: death"),
            of("1", "help? death, suffering, agony. depth"),
            of("2", "ape - death - suffering"),
            of("3", "death/depth hell"),
            of("4", " death suffering#are dog")
    );
    protected final static Path directory = Path.of("src/test/resources/indexer");

    protected final I indexer;

    public BaseIndexerTest(I indexer) {
        this.indexer = indexer;
        indexer.index(documents, new DefaultTokenizer());
    }

    @Test
    public void testDocumentsMap() throws Exception {
        assertIterableEquals(expectedDocumentsMap(), Files.readAllLines(directory.resolve(Indexer.DOCUMENTS_MAP_FILE_NAME)));
    }

    @Test
    public void testVocabularyString() throws Exception {
        assertArrayEquals(expectedVocabularyString(), Files.readAllBytes(directory.resolve(Indexer.VOCABULARY_STRING_FILE_NAME)));
    }

    @Test
    public void testVocabularyTable() throws Exception {
        assertArrayEquals(expectedVocabularyTable(), Files.readAllBytes(directory.resolve(Indexer.VOCABULARY_TABLE_FILE_NAME)));
    }

    @Test
    public void testPostings() throws Exception {
        assertArrayEquals(expectedPostings(), Files.readAllBytes(directory.resolve(Indexer.POSTINGS_FILE_NAME)));
    }

    protected abstract List<String> expectedDocumentsMap();

    protected abstract byte[] expectedVocabularyString();

    protected abstract byte[] expectedVocabularyTable();

    protected abstract byte[] expectedPostings();
}
