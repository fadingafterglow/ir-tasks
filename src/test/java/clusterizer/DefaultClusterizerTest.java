package clusterizer;

import encoders.VBEncodedInputStream;
import encoders.VBEncodedOutputStream;
import structure.document.TfAwareIndex;
import structure.document.disk.SPIMIIndexer;
import structure.document.disk.TfAwareInBlock;
import structure.document.disk.TfAwareOnDiskInvertedIndex;
import structure.document.disk.TfAwareOutBlock;
import tokenizer.DefaultTokenizer;

public class DefaultClusterizerTest extends BaseClusterizerTest<DefaultClusterizer> {

    static {
        SPIMIIndexer indexer = SPIMIIndexer.builder(directory)
                .encodedInputStreamFactory(VBEncodedInputStream::new)
                .encodedOutputStreamFactory(VBEncodedOutputStream::new)
                .inBlockFactory(TfAwareInBlock::new)
                .outBlockFactory(TfAwareOutBlock::new)
                .build();
        indexer.index(documents, new DefaultTokenizer());
    }

    private final TfAwareIndex index;

    public DefaultClusterizerTest() {
        super(new DefaultClusterizer());
        this.index = TfAwareOnDiskInvertedIndex.builder(directory)
                .encodedInputStreamFactory(VBEncodedInputStream::new)
                .build();
    }

    protected TfAwareIndex getIndex() {
        return index;
    }
}
