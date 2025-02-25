package document;

import java.nio.file.Path;

public abstract class FileDocument implements Document {

    protected final Path path;

    public FileDocument(Path path) {
        this.path = path;
        validateFile();
    }

    public FileDocument(String path) {
        this(Path.of(path));
    }

    @Override
    public String getName() {
        return path.toString();
    }

    protected void validateFile() {
        if (path == null)
            throw new IllegalArgumentException("File path can not be null");
        if (!path.getFileName().toString().endsWith(getExpectedExtension()))
            throw new IllegalArgumentException("Invalid file type");
    }

    protected abstract String getExpectedExtension();
}
