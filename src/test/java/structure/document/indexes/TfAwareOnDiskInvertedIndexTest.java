package structure.document.indexes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import structure.document.TfAwareIndex;
import structure.document.disk.Indexer;
import structure.document.disk.TfAwareOnDiskInvertedIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static utils.MocksFactory.ofT;

public class TfAwareOnDiskInvertedIndexTest extends BaseIndexTest<TfAwareOnDiskInvertedIndex> {

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
                    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 24,
                    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 40,
                    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 48,
                    0, 0, 0, 24, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 64,
                    0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 80,
                    0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 104,
                    0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 120,
                    0, 0, 0, 48, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -96,
                    0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -88
            });
            Files.write(directory.resolve(Indexer.POSTINGS_FILE_NAME), new byte[] {
                    0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1,
                    0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1,
                    0, 0, 0, 4, 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 4, 0, 0, 0, 1,
                    0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 2,
                    0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2,
                    0, 0, 0, 0, 0, 0, 0, 1,
                    0, 0, 0, 2, 0, 0, 0, 1
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TfAwareOnDiskInvertedIndexTest() {
        super(TfAwareOnDiskInvertedIndex.builder(directory).build());
    }

    @ParameterizedTest
    @MethodSource
    public void testGetEntries(String term, List<TfAwareIndex.Entry> expected) {
        assertEquals(expected, searchStructure.getEntries(term));
    }

    public static Stream<Arguments> testGetEntries() {
        return Stream.of(
                Arguments.of("a", List.of(ofT(0, 2), ofT(1, 2), ofT(2, 1))),
                Arguments.of("b", List.of(ofT(2, 1), ofT(3, 1))),
                Arguments.of("c", List.of(ofT(4, 1))),
                Arguments.of("d", List.of(ofT(0, 2), ofT(4, 1))),
                Arguments.of("e", List.of(ofT(1, 1), ofT(4, 1))),
                Arguments.of("f", List.of(ofT(0, 1), ofT(2, 1), ofT(4, 2))),
                Arguments.of("g", List.of(ofT(1, 1), ofT(3, 1))),
                Arguments.of("h", List.of(ofT(0, 2), ofT(1, 3), ofT(2, 1), ofT(3, 1), ofT(4, 2))),
                Arguments.of("i", List.of(ofT(0, 1))),
                Arguments.of("j", List.of(ofT(2, 1))),
                Arguments.of("invalid", List.of())
        );
    }
}
