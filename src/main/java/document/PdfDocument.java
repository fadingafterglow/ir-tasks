package document;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PdfDocument extends FileDocument {

    public final static String PDF_EXTENSION = ".pdf";

    public PdfDocument(Path path) {
        super(path);
    }

    public PdfDocument(String path) {
        super(path);
    }

    @Override
    public String getBody() {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Can not read a file: " + path, e);
        }
    }

    @Override
    public List<String> getZones() {
        return List.of(getBody());
    }

    @Override
    protected String getExpectedExtension() {
        return PDF_EXTENSION;
    }
}
