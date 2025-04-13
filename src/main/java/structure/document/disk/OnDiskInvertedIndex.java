package structure.document.disk;

import encoders.EncodedInputStream;
import encoders.VocabularyDecoder;
import encoders.VocabularyFrontDecoder;
import encoders.VocabularyFrontEncoder;
import lombok.SneakyThrows;
import structure.document.ZoneIndex;
import tokenizer.Tokenizer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;


public abstract class OnDiskInvertedIndex implements ZoneIndex, Closeable {

    protected final Tokenizer tokenizer;
    protected final int zonesCount;
    protected final Function<InputStream, EncodedInputStream> encodedInputStreamFactory;
    protected final List<String> documentsMap;
    protected final byte[] vocabularyString;
    protected final List<VocabularyBlock> vocabularyBlocks;
    protected final List<PostingListInfo> postingListInfos;
    protected final ThreadLocal<VocabularyDecoder> vocabularyDecoder;
    protected final ThreadLocal<FileChannel> postings;

    public OnDiskInvertedIndex(Path indexDirectory, Tokenizer tokenizer, int zonesCount,
                               Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        this.tokenizer = tokenizer;
        this.zonesCount = zonesCount;
        this.encodedInputStreamFactory = encodedInputStreamFactory;
        documentsMap = loadDocumentsMap(indexDirectory);
        vocabularyString = loadVocabularyString(indexDirectory);
        vocabularyBlocks = new ArrayList<>();
        postingListInfos = new ArrayList<>();
        loadVocabularyTable(indexDirectory);
        vocabularyDecoder = ThreadLocal.withInitial(this::initVocabularyDecoder);
        postings = ThreadLocal.withInitial(() -> initPostings(indexDirectory));
    }

    @SneakyThrows
    private List<String> loadDocumentsMap(Path indexDirectory) {
        return new ArrayList<>(Files.readAllLines(indexDirectory.resolve(Indexer.DOCUMENTS_MAP_FILE_NAME)));
    }

    @SneakyThrows
    private byte[] loadVocabularyString(Path indexDirectory) {
        return Files.readAllBytes(indexDirectory.resolve(Indexer.VOCABULARY_STRING_FILE_NAME));
    }

    @SneakyThrows
    private void loadVocabularyTable(Path indexDirectory) {
        try (EncodedInputStream is = encodedInputStreamFactory.apply(new BufferedInputStream(Files.newInputStream(indexDirectory.resolve(Indexer.VOCABULARY_TABLE_FILE_NAME))))) {
            while (true) {
                int position = is.readInt();
                if (is.eofReached()) break;
                vocabularyBlocks.add(new VocabularyBlock(position));
                for (int i = 0; i < VocabularyFrontEncoder.TERMS_PER_BLOCK; i++) {
                    PostingListInfo info = new PostingListInfo(is.readInt(), is.readLong());
                    if (is.eofReached()) break;
                    postingListInfos.add(info);
                }
            }
        }
    }

    @SneakyThrows
    private VocabularyDecoder initVocabularyDecoder() {
        return new VocabularyFrontDecoder(vocabularyString, encodedInputStreamFactory);
    }

    @SneakyThrows
    private FileChannel initPostings(Path indexDirectory) {
        return FileChannel.open(indexDirectory.resolve(Indexer.POSTINGS_FILE_NAME));
    }

    @Override
    public int documentsCount() {
        return documentsMap.size();
    }

    @Override
    public int termsCount() {
        return postingListInfos.size();
    }

    @Override
    public String getDocumentName(int id) {
        int documentId = id / zonesCount;
        if (documentId < 0 || documentId >= documentsMap.size())
            return null;
        return documentsMap.get(documentId);
    }

    @Override
    @SneakyThrows
    public List<Integer> getDocumentIds(String term) {
        int index = getPostingListInfoIndex(term);
        if (index == -1) return List.of();
        PostingListInfo info = postingListInfos.get(index);
        byte[] list = readPostingList(index, info);
        try (EncodedInputStream is = encodedInputStreamFactory.apply(new ByteArrayInputStream(list))) {
            return extractIds(is, info);
        }
    }

    protected int getPostingListInfoIndex(String term) {
        int left = 0; int right = vocabularyBlocks.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            VocabularyBlock block = vocabularyBlocks.get(mid);
            int index = block.indexInBlock(term);
            if (index == -1)
                right = mid - 1;
            else if (index == VocabularyFrontEncoder.TERMS_PER_BLOCK)
                left = mid + 1;
            else if (index == -2)
                break;
            else
                return mid * VocabularyFrontEncoder.TERMS_PER_BLOCK + index;
        }
        return -1;
    }

    @SneakyThrows
    protected byte[] readPostingList(int index, PostingListInfo info) {
        byte[] ids = new byte[getPostingListSize(index)];
        ByteBuffer buffer = ByteBuffer.wrap(ids);
        postings.get().read(buffer, info.position());
        return ids;
    }

    @SneakyThrows
    protected int getPostingListSize(int index) {
        if (index == postingListInfos.size() - 1)
            return (int) (postings.get().size() - postingListInfos.getLast().position());
        else
            return (int) (postingListInfos.get(index + 1).position() - postingListInfos.get(index).position());
    }

    protected abstract List<Integer> extractIds(EncodedInputStream is, PostingListInfo info);

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public List<Integer> getAllDocumentIds() {
        return Stream.iterate(0, x -> x < documentsMap.size() * zonesCount, x -> x + 1).toList();
    }

    @Override
    public int getDocumentFrequency(String term) {
        int index = getPostingListInfoIndex(term);
        return index == -1 ? 0 : postingListInfos.get(index).frequency();
    }

    @Override
    public int getZonesCount() {
        return zonesCount;
    }

    @Override
    @SneakyThrows
    public void close() {
        postings.get().close();
    }

    protected record PostingListInfo(int frequency, long position) {}

    protected final class VocabularyBlock {
        private final int position;

        private VocabularyBlock(int position) {
            this.position = position;
        }

        public int indexInBlock(String value) {
            VocabularyDecoder decoder = vocabularyDecoder.get();
            decoder.seek(position);
            byte[] prefix = decoder.readPrefix();
            for (int i = 0; i < VocabularyFrontEncoder.TERMS_PER_BLOCK; i++) {
                String term = decoder.readTerm(prefix);
                int comparison = value.compareTo(term);
                if (comparison == 0) return i;
                else if (comparison < 0 && i == 0) return -1;
                else if (comparison > 0 && i == VocabularyFrontEncoder.TERMS_PER_BLOCK - 1) return VocabularyFrontEncoder.TERMS_PER_BLOCK;
            }
            return -2;
        }
    }
}
