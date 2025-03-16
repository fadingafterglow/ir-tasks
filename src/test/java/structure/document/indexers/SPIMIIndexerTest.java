package structure.document.indexers;


import structure.document.disk.SPIMIIndexer;

import java.util.List;

public class SPIMIIndexerTest extends BaseIndexerTest<SPIMIIndexer> {

    public SPIMIIndexerTest() {
        super(new SPIMIIndexer(directory.toString()));
    }

    @Override
    protected List<String> expectedDocumentsMap() {
        return List.of(
                "0", "1", "2", "3", "4"
        );
    }

    @Override
    protected byte[] expectedVocabulary() {
        return new byte[] {
                0, 0, 0, 1, 97, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 98, 0, 0, 0, 0, 0, 0, 0, 12,
                0, 0, 0, 1, 99, 0, 0, 0, 0, 0, 0, 0, 20,
                0, 0, 0, 1, 100, 0, 0, 0, 0, 0, 0, 0, 24,
                0, 0, 0, 1, 101, 0, 0, 0, 0, 0, 0, 0, 32,
                0, 0, 0, 1, 102, 0, 0, 0, 0, 0, 0, 0, 40,
                0, 0, 0, 1, 103, 0, 0, 0, 0, 0, 0, 0, 52,
                0, 0, 0, 1, 104, 0, 0, 0, 0, 0, 0, 0, 60,
                0, 0, 0, 1, 105, 0, 0, 0, 0, 0, 0, 0, 80,
                0, 0, 0, 1, 106, 0, 0, 0, 0, 0, 0, 0, 84
        };
    }

    @Override
    protected byte[] expectedPostings() {
        return new byte[] {
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2,
                0, 0, 0, 2, 0, 0, 0, 3,
                0, 0, 0, 4,
                0, 0, 0, 0, 0, 0, 0, 4,
                0, 0, 0, 1, 0, 0, 0, 4,
                0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 4,
                0, 0, 0, 1, 0, 0, 0, 3,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4,
                0, 0, 0, 0,
                0, 0, 0, 2
        };
    }

}
