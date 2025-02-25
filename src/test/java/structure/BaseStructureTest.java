package structure;

import document.Document;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseStructureTest {
    protected final static List<Document> documents = List.of(
            documentOf("0", "a D, f! H; i, a, d; H"),
            documentOf("1", "a E' \"g h\" [a h]"),
            documentOf("2", "a #b f&h j"),
            documentOf("3", "b g h"),
            documentOf("4", "h 'f c?! d (e) f h")
    );;

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

    protected static Document documentOf(String name, String body) {
        Document document = mock(Document.class);
        when(document.getName()).thenReturn(name);
        when(document.getBody()).thenReturn(body);
        return document;
    }
}
