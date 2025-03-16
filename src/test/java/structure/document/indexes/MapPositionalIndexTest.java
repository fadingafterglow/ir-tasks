package structure.document.indexes;

import structure.document.memory.MapPositionalIndex;
import tokenizer.DefaultTokenizer;

public class MapPositionalIndexTest extends BasePositionalIndexTest<MapPositionalIndex> {

    public MapPositionalIndexTest() {
        super(new MapPositionalIndex(documents, new DefaultTokenizer()));
    }
}
