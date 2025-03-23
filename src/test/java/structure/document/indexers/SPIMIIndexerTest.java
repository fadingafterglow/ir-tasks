package structure.document.indexers;

import encoders.NotEncodedInputStream;
import encoders.NotEncodedOutputStream;
import structure.document.disk.SPIMIIndexer;

import java.util.List;

public class SPIMIIndexerTest extends BaseIndexerTest<SPIMIIndexer> {

    public SPIMIIndexerTest() {
        super(new SPIMIIndexer(directory.toString(), NotEncodedOutputStream::new, NotEncodedInputStream::new));
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
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 4,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 12,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 16,
                0, 0, 0, 33, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 20,
                0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 24,
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 44,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 52,
                0, 0, 0, 70, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 56,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 60,
                0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 64,
                0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 72,
                0, 0, 0, 101, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 76
        };
    }

    @Override
    protected byte[] expectedPostings() {
        return new byte[] {
                0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 2,
                0, 0, 0, 0,
                0, 0, 0, 4,
                0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1,
                0, 0, 0, 1, 0, 0, 0, 2,
                0, 0, 0, 4,
                0, 0, 0, 3,
                0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 0,
                0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2
        };
    }

}
