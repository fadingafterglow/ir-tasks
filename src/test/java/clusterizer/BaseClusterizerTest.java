package clusterizer;

import document.Document;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import structure.document.TfAwareIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static utils.MocksFactory.of;

public abstract class BaseClusterizerTest<C extends Clusterizer> {

    protected final static Path directory = Path.of("src/test/resources/clusterizer");

    protected final static List<Document> documents = List.of(
            of(directory.resolve("0.txt").toString(), "cat cat dog cat"),
            of(directory.resolve("1.txt").toString(), "cat cat dog"),
            of(directory.resolve("2.txt").toString(), "cat dog pony"),
            of(directory.resolve("3.txt").toString(), "car car car"),
            of(directory.resolve("4.txt").toString(), "car plane train"),
            of(directory.resolve("5.txt").toString(), "car car auto bus"),
            of(directory.resolve("6.txt").toString(), "virus dead bacteria"),
            of(directory.resolve("7.txt").toString(), "virus infection"),
            of(directory.resolve("8.txt").toString(), "bacteria catastrophe")
    );

    static {
        try {
            Files.createDirectories(directory);
            for (Document document : documents)
                Files.writeString(Path.of(document.getName()), document.getBody());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected final C clusterizer;

    public BaseClusterizerTest(C clusterizer) {
        this.clusterizer = clusterizer;
    }

    @ParameterizedTest
    @MethodSource
    public void testClusterize(Map<Integer, List<Clusterizer.ClusteredDocument>> expected) {
        TfAwareIndex index = getIndex();
        Map<Integer, List<Clusterizer.ClusteredDocument>> result = clusterizer.clusterize(index, expected.keySet());
        for (Map.Entry<Integer, List<Clusterizer.ClusteredDocument>> entry : expected.entrySet())
            assertIterableEquals(
                    entry.getValue(),
                    result.get(entry.getKey()).stream().filter(d -> d.getSimilarity() != 0).toList()
            );
    }

    public static Stream<Arguments> testClusterize() {
        return Stream.of(
                Arguments.of(Map.of(
                        0, List.of(
                                of(0, 1.0),
                                of(1, 0.99),
                                of(2, 0.52)
                        ),
                        3, List.of(
                                of(3, 1.0),
                                of(4, 0.33),
                                of(5, 0.58)
                        ),
                        6, List.of(
                                of(6, 1.0),
                                of(7, 0.28),
                                of(8, 0.28)
                        )
                    )
                ),
                Arguments.of(Map.of(
                        1, List.of(
                                of(0, 0.99),
                                of(1, 1.0),
                                of(2, 0.55)
                        ),
                        4, List.of(
                                of(3, 0.33),
                                of(4, 1.0),
                                of(5, 0.19)
                        ),
                        7, List.of(
                                of(6, 0.28),
                                of(7, 1.0)
                        )
                    )
                ),
                Arguments.of(Map.of(
                        2, List.of(
                                of(0, 0.52),
                                of(1, 0.55),
                                of(2, 1.0)
                        ),
                        5, List.of(
                                of(3, 0.58),
                                of(4, 0.19),
                                of(5, 1.0)
                        ),
                        8, List.of(
                                of(6, 0.28),
                                of(8, 1.0)
                        )
                    )
                )
        );
    }

    protected abstract TfAwareIndex getIndex();
}
