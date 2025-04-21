package document;

import java.nio.file.Path;

public class DocumentLoader {

    public static Document loadDocument(String path) {
        return loadDocument(Path.of(path));
    }

    public static Document loadDocument(Path path) {
        return switch (getExtension(path)) {
            case TxtDocument.EXTENSION -> new TxtDocument(path);
            case PdfDocument.EXTENSION -> new PdfDocument(path);
            case CsvDocument.EXTENSION -> new CsvDocument(path, "title", "text", "authors", "tags");
            default -> null;
        };
    }

    private static String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int i = fileName.lastIndexOf('.') + 1;
        return fileName.substring(i);
    }
}
