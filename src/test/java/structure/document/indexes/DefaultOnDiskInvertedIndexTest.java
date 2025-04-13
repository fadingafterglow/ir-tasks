package structure.document.indexes;

import structure.document.disk.Indexer;
import structure.document.disk.DefaultOnDiskInvertedIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DefaultOnDiskInvertedIndexTest extends BaseIndexTest<DefaultOnDiskInvertedIndex> {

    private final static Path directory = Path.of("src/test/resources/index");

    static {
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve(Indexer.DOCUMENTS_MAP_FILE_NAME), List.of("0", "1", "2", "3", "4"));
            Files.write(directory.resolve(Indexer.VOCABULARY_STRING_FILE_NAME), new byte[] {
                    0, 0, 0, 0, 0, 0, 0, 1, 97, 0, 0, 0, 1, 98, 0, 0, 0, 1, 99, 0, 0, 0, 1, 100,
                    0, 0, 0, 0, 0, 0, 0, 1, 101, 0, 0, 0, 1, 102, 0, 0, 0, 1, 103, 0, 0, 0, 1, 104,
                    0, 0, 0, 0, 0, 0, 0, 1, 105, 0, 0, 0, 1, 106
            });
            Files.write(directory.resolve(Indexer.VOCABULARY_TABLE_FILE_NAME), new byte[] {
                    0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 12,
                    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 20,
                    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 24,
                    0, 0, 0, 24, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 32,
                    0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 40,
                    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 52,
                    0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 60,
                    0, 0, 0, 48, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 80,
                    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 84
            });
            Files.write(directory.resolve(Indexer.POSTINGS_FILE_NAME), new byte[] {
                    0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1,
                    0, 0, 0, 2, 0, 0, 0, 1,
                    0, 0, 0, 4,
                    0, 0, 0, 0, 0, 0, 0, 4,
                    0, 0, 0, 1, 0, 0, 0, 3,
                    0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2,
                    0, 0, 0, 1, 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1,
                    0, 0, 0, 0,
                    0, 0, 0, 2
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultOnDiskInvertedIndexTest() {
        super(DefaultOnDiskInvertedIndex.builder(directory).build());
    }
}
