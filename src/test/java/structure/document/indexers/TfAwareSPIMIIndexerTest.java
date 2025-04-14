package structure.document.indexers;

import structure.document.disk.SPIMIIndexer;
import structure.document.disk.TfAwareInBlock;
import structure.document.disk.TfAwareOutBlock;

import java.util.List;

public class TfAwareSPIMIIndexerTest extends BaseIndexerTest<SPIMIIndexer> {

    public TfAwareSPIMIIndexerTest() {
        super(SPIMIIndexer.builder(directory.toString()).inBlockFactory(TfAwareInBlock::new).outBlockFactory(TfAwareOutBlock::new).build());
    }

    @Override
    protected List<String> expectedDocumentsMap() {
        return List.of(
                "0", "1", "2", "3", "4"
        );
    }

    @Override
    protected byte[] expectedVocabularyString() {
        return new byte[] {
                0, 0, 0, 1, 97, 0, 0, 0, 4, 103, 111, 110, 121, 0, 0, 0, 2, 112, 101, 0, 0, 0, 4, 112, 112, 108, 101, 0, 0, 0, 2, 114, 101,
                0, 0, 0, 0, 0, 0, 0, 4, 99, 111, 111, 108, 0, 0, 0, 5, 100, 101, 97, 116, 104, 0, 0, 0, 5, 100, 101, 112, 116, 104, 0, 0, 0, 3, 100, 111, 103,
                0, 0, 0, 3, 104, 101, 108, 0, 0, 0, 1, 108, 0, 0, 0, 2, 108, 111, 0, 0, 0, 1, 112, 0, 0, 0, 4, 115, 105, 110, 103,
                0, 0, 0, 9, 115, 117, 102, 102, 101, 114, 105, 110, 103, 0, 0, 0, 0
        };
    }

    @Override
    protected byte[] expectedVocabularyTable() {
        return new byte[] {
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 8,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 24,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32,
                0, 0, 0, 33, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 40,
                0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 48,
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 88,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 104,
                0, 0, 0, 70, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 112,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 120,
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, -128,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -112,
                0, 0, 0, 101, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, -104
        };
    }

    @Override
    protected byte[] expectedPostings() {
        return new byte[] {
                0, 0, 0, 1, 0, 0, 0, 3,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 2,
                0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 4, 0, 0, 0, 4,
                0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1,
                0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 2,
                0, 0, 0, 4, 0, 0, 0, 1,
                0, 0, 0, 3, 0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2,
                0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1
        };
    }

}
