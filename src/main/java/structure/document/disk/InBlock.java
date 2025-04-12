package structure.document.disk;

import encoders.EncodedOutputStream;
import encoders.VocabularyEncoder;

import java.io.Closeable;

public interface InBlock extends Comparable<InBlock>, Closeable {

    boolean advance();

    MergeResult toMergeResult();

    interface MergeResult {

        boolean shouldMerge(InBlock block);

        void merge(InBlock block);

        long save(long position, EncodedOutputStream osPostings, VocabularyEncoder osVocabulary);
    }
}
