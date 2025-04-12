package structure.document.disk;

import encoders.EncodedOutputStream;

public interface OutBlock {

    long size();

    boolean isEmpty();

    void add(String term, int documentId);

    void flush(EncodedOutputStream os);
}
