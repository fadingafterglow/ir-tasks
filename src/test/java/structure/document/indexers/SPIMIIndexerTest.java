package structure.document.indexers;


import structure.document.disk.SPIMIIndexer;

public class SPIMIIndexerTest extends BaseIndexerTest<SPIMIIndexer> {

    public SPIMIIndexerTest() {
        super(new SPIMIIndexer(directory.toString()));
    }

    @Override
    protected byte[] expectedDocumentsMap() {
        return new byte[] {
                0, 0, 0, 1, 48, 0, 0, 0, 0,
                0, 0, 0, 1, 49, 0, 0, 0, 1,
                0, 0, 0, 1, 50, 0, 0, 0, 2,
                0, 0, 0, 1, 51, 0, 0, 0, 3,
                0, 0, 0, 1, 52, 0, 0, 0, 4
        };
    }

    @Override
    protected byte[] expectedVocabulary() {
        return new byte[] {
                0, 0, 0, 1, 97, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 1, 98, 0, 0, 0, 0, 0, 0, 0, 16,
                0, 0, 0, 1, 99, 0, 0, 0, 0, 0, 0, 0, 28,
                0, 0, 0, 1, 100, 0, 0, 0, 0, 0, 0, 0, 36,
                0, 0, 0, 1, 101, 0, 0, 0, 0, 0, 0, 0, 48,
                0, 0, 0, 1, 102, 0, 0, 0, 0, 0, 0, 0, 60,
                0, 0, 0, 1, 103, 0, 0, 0, 0, 0, 0, 0, 76,
                0, 0, 0, 1, 104, 0, 0, 0, 0, 0, 0, 0, 88,
                0, 0, 0, 1, 105, 0, 0, 0, 0, 0, 0, 0, 112,
                0, 0, 0, 1, 106, 0, 0, 0, 0, 0, 0, 0, 120
        };
    }

    @Override
    protected byte[] expectedPostings() {
        return new byte[] {
                0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2,
                0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3,
                0, 0, 0, 1, 0, 0, 0, 4,
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 4,
                0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 4,
                0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 4,
                0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 3,
                0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4,
                0, 0, 0, 1, 0, 0, 0, 0,
                0, 0, 0, 1, 0, 0, 0, 2,
        };
    }

}
