package document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TxtDocument extends FileDocument {

    public final static String TXT_EXTENSION = ".txt";

    public TxtDocument(Path path) {
        super(path);
    }

    public TxtDocument(String path) {
        super(path);
    }

    @Override
    public String getBody() {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Can not read a file: " + path, e);
        }
    }

    @Override
    protected String getExpectedExtension() {
        return TXT_EXTENSION;
    }
}
