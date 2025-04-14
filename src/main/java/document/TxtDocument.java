package document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TxtDocument extends FileDocument {

    public final static String EXTENSION = "txt";

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
    public List<String> getZones() {
        return List.of(getBody());
    }

    @Override
    protected String getExpectedExtension() {
        return EXTENSION;
    }
}
