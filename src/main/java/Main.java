import document.Document;
import document.PdfDocument;
import document.TxtDocument;
import encoders.VBEncodedInputStream;
import encoders.VBEncodedOutputStream;
import executor.*;
import expression.Expression;
import parser.Parser;
import structure.document.*;
import structure.document.disk.SPIMIIndexer;
import structure.document.disk.OnDiskInvertedIndex;
import structure.document.memory.*;
import structure.vocabulary.PermutermIndex;
import structure.vocabulary.ThreeGramIndex;
import structure.vocabulary.TwoTriesIndex;
import structure.vocabulary.VocabularyIndex;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Main {
    private static final String DEFAULT_DOCUMENTS_DIRECTORY = "/Users/nick/Downloads/documents";
    private static final String DEFAULT_DISK_INDEX_DIRECTORY = "/Users/nick/Downloads/index";

    public static void main(String[] args) {
        index();
        QueryExecutor queryExecutor = createExecutor();
        while (true) {
            try {
                String query = getLine("Enter a query (blank to exit): ", "");
                if (query.isBlank()) break;
                log("Parsing query...");
                Expression expression = logExecutionTime(() -> new Parser(query).parse());
                log("Executing query...");
                displayResults(logExecutionTime(() -> queryExecutor.execute(expression)));
            }
            catch (Exception e) {
                log("Syntax error: " + e.getMessage());
            }
        }
    }

    private static void index() {
        if (getOption("Continue", "Index documents") != 1) return;
        String indexDirectory = getLine("Enter a path to the disk index directory (blank for default): ", DEFAULT_DISK_INDEX_DIRECTORY);
        List<Document> documents = loadDocuments();
        SPIMIIndexer indexer = new SPIMIIndexer(indexDirectory, VBEncodedOutputStream::new, VBEncodedInputStream::new);
        logExecutionTime(() -> indexer.index(documents, new DefaultTokenizer()));
    }

    private static QueryExecutor createExecutor() {
        Tokenizer tokenizer = new DefaultTokenizer();
        return switch (getOption("Use in-memory index", "Use on-disk index")) {
            case 0 -> executorForInMemoryIndex(tokenizer);
            case 1 -> executorForOnDiskIndex(tokenizer);
            default -> throw new IllegalArgumentException("Invalid option");
        };
    }

    private static QueryExecutor executorForInMemoryIndex(Tokenizer tokenizer) {
        List<Document> documents = loadDocuments();
        return switch (getOption("Matrix", "Inverted Index", "Biword index", "Positional Index", "Fuzzy Positional Index")) {
            case 0 -> {
                log("Building matrix...");
                Matrix matrix = logExecutionTime(() -> new MapMatrix(documents, tokenizer));
                yield new MatrixQueryExecutor(matrix);
            }
            case 1 -> {
                log("Building inverted index...");
                Index index = logExecutionTime(() -> new MapInvertedIndex(documents, tokenizer));
                yield new IndexQueryExecutor(index);
            }
            case 2 -> {
                log("Building biword index...");
                BiWordIndex index = logExecutionTime(() -> new MapBiWordIndex(documents, tokenizer));
                yield new BiWordIndexQueryExecutor(index);
            }
            case 3 -> {
                log("Building positional index...");
                PositionalIndex index = logExecutionTime(() -> new MapPositionalIndex(documents, tokenizer));
                yield new PositionalIndexQueryExecutor(index);
            }
            case 4 -> {
                VocabularyIndex vocabularyIndex = createVocabularyIndex();
                log("Building fuzzy positional index...");
                FuzzyPositionalIndex index = logExecutionTime(() -> new FuzzyPositionalIndex(documents, tokenizer, vocabularyIndex));
                yield new PositionalIndexQueryExecutor(index);
            }
            default -> throw new IllegalArgumentException("Invalid option");
        };
    }

    private static QueryExecutor executorForOnDiskIndex(Tokenizer tokenizer) {
        String indexDirectory = getLine("Enter a path to the disk index directory (blank for default): ", DEFAULT_DISK_INDEX_DIRECTORY);
        return switch (getOption("Inverted Index")) {
            case 0 -> {
                log("Loading disk index...");
                OnDiskInvertedIndex index = logExecutionTime(() -> new OnDiskInvertedIndex(Path.of(indexDirectory), tokenizer, VBEncodedInputStream::new));
                yield new IndexQueryExecutor(index);
            }
            default -> throw new IllegalArgumentException("Invalid option");
        };
    }

    private static List<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        while (documents.isEmpty()) {
            String path = getLine("Enter a path to a document or a directory with documents (blank for default): ", DEFAULT_DOCUMENTS_DIRECTORY);
            addDocument(new File(path), documents);
        }
        System.out.println("Size of documents: " + documents.stream().mapToLong(Document::getSize).sum() + " bytes");
        return documents;
    }

    private static void addDocument(File file, List<Document> documents) {
        if (file.isDirectory())
            Arrays.asList(file.listFiles()).forEach(f -> addDocument(f, documents));
        else {
            String name = file.getName();
            if (!file.exists()) {
                System.out.println("File \"" + name + "\" does not exist");
                return;
            }
            if (name.endsWith(TxtDocument.TXT_EXTENSION))
                documents.add(new TxtDocument(file.toPath()));
            else if (name.endsWith(PdfDocument.PDF_EXTENSION))
                documents.add(new PdfDocument(file.toPath()));
            else
                System.out.println("Unsupported file type: " + name);
        }
    }

    private static VocabularyIndex createVocabularyIndex() {
        return switch (getOption("Two Tries", "Permuterm", "3-gram")) {
            case 0 -> new TwoTriesIndex();
            case 1 -> new PermutermIndex();
            case 2 -> new ThreeGramIndex();
            default -> throw new IllegalArgumentException("Invalid option");
        };
    }

    private static void displayResults(List<String> results) {
        if (results.isEmpty()) {
            System.out.println("No documents found");
        } else {
            System.out.println("Found documents:");
            for (int i = 0; i < results.size(); i++)
                System.out.println((i + 1) + ". " + results.get(i));
        }
    }

    private static int getOption(String... names) {
        while (true) {
            System.out.print("Choose an option (");
            for (int i = 0; i < names.length; i++)
                System.out.print(i + " - " + names[i] + (i == names.length- 1 ? "): " : ", "));
            try {
                int option = Integer.parseInt(System.console().readLine());
                if (option >= 0 && option < names.length)
                    return option;
            }
            catch (NumberFormatException e) {/* ignore */}
            System.out.println("Invalid option, try again");
        }
    }

    private static String getLine(String message) {
        System.out.print(message);
        return System.console().readLine();
    }

    private static String getLine(String message, String defaultValue) {
        String line = getLine(message);
        return  (line == null || line.isBlank()) ? defaultValue : line;
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static <T> T logExecutionTime(Supplier<T> action) {
        long start = System.currentTimeMillis();
        T result = action.get();
        System.out.println("Execution time: " + (System.currentTimeMillis() - start) + " ms");
        return result;
    }

    private static void logExecutionTime(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        System.out.println("Execution time: " + (System.currentTimeMillis() - start) + " ms");
    }
}
