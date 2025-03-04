import document.Document;
import document.PdfDocument;
import document.TxtDocument;
import executor.IndexQueryExecutor;
import executor.MatrixQueryExecutor;
import expression.Expression;
import parser.Parser;
import parser.SyntaxException;
import structure.Index;
import structure.MapInvertedIndex;
import structure.MapMatrix;
import structure.Matrix;
import tokenizer.DefaultTokenizer;
import tokenizer.Tokenizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Main {
    private static final String DEFAULT_DOCUMENTS_DIRECTORY = "/Users/nick/Downloads/documents";

    public static void main(String[] args) {
        List<Document> documents = loadDocuments();
        Tokenizer tokenizer = new DefaultTokenizer();

        log("Building matrix...");
        Matrix matrix = logExecutionTime(() -> new MapMatrix(documents, tokenizer));
        MatrixQueryExecutor matrixQueryExecutor= new MatrixQueryExecutor(matrix);

        log("Building index...");
        Index index = logExecutionTime(() -> new MapInvertedIndex(documents, tokenizer));
        IndexQueryExecutor indexQueryExecutor = new IndexQueryExecutor(index);

        while (true) {
            try {
                String query = getLine("Enter a query (blank to exit): ", "");
                if (query.isEmpty()) break;
                log("Parsing query...");
                Expression expression = logExecutionTime(() -> new Parser(query).parse());
                log("Executing query with matrix...");
                displayResults(logExecutionTime(() -> matrixQueryExecutor.execute(expression)));
                log("Executing query with index...");
                displayResults(logExecutionTime(() -> indexQueryExecutor.execute(expression)));
            }
            catch (SyntaxException e) {
                System.out.println("Syntax error: " + e.getMessage());
            }
        }
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

    private static void displayResults(List<String> results) {
        if (results.isEmpty()) {
            System.out.println("No documents found");
        } else {
            System.out.println("Found documents:");
            for (int i = 0; i < results.size(); i++)
                System.out.println((i + 1) + ". " + results.get(i));
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
}
