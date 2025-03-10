package structure.document;

import tokenizer.DefaultTokenizer;

public class MapInvertedIndexTest extends BaseIndexTest<MapInvertedIndex> {

    public MapInvertedIndexTest() {
        super(new MapInvertedIndex(documents, new DefaultTokenizer()));
    }
}
