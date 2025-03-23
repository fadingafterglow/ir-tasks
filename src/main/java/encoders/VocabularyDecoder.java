package encoders;

public interface VocabularyDecoder {

    void seek(int position);

    byte[] readPrefix();

    String readTerm(byte[] prefix);
}
