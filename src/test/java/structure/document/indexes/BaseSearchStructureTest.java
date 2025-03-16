package structure.document.indexes;

import document.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import structure.document.SearchStructure;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.MocksFactory.*;

public abstract class BaseSearchStructureTest<S extends SearchStructure<?>> {
    /*
        a: 0, 1, 2
        b: 2, 3
        с: 4
        d: 0, 4
        e: 1, 4
        f: 0, 2, 4
        g: 1, 3
        h: 0, 1, 2, 3, 4
        i: 0
        j: 2
    */
    protected final static List<Document> documents = List.of(
            of("0", "a D, f! H; i, a, d; H"),
            of("1", "a E' \"g h\" [a h] H"),
            of("2", "a #b f&h j"),
            of("3", "b h g"),
            of("4", "h 'f c?! d (e) f h")
    );

    protected final S searchStructure;

    public BaseSearchStructureTest(S searchStructure) {
        this.searchStructure = searchStructure;
    }

    @Test
    public void testDocumentsCount() {
        assertEquals(5, searchStructure.documentsCount());
    }

    @Test
    public void testTermsCount() {
        assertEquals(10, searchStructure.termsCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    public void testGetDocumentName(int id) {
        assertEquals(String.valueOf(id), searchStructure.getDocumentName(id));
    }
}
