package structure.document.disk;

import encoders.EncodedInputStream;
import encoders.VocabularyDecoder;
import encoders.VocabularyFrontDecoder;
import encoders.VocabularyFrontEncoder;
import lombok.SneakyThrows;
import structure.document.Index;
import tokenizer.Tokenizer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;


public class OnDiskInvertedIndex implements Index, Closeable {

    private final Tokenizer tokenizer;
    private final Function<InputStream, EncodedInputStream> encodedInputStreamFactory;
    private final List<String> documentsMap;
    private final VocabularyDecoder vocabularyDecoder;
    private final List<VocabularyBlock> vocabularyBlocks;
    private final List<PostingListInfo> postingListInfos;
    private final FileChannel postings;

    public OnDiskInvertedIndex(Path indexDirectory, Tokenizer tokenizer, Function<InputStream, EncodedInputStream> encodedInputStreamFactory) {
        this.tokenizer = tokenizer;
        this.encodedInputStreamFactory = encodedInputStreamFactory;
        documentsMap = loadDocumentsMap(indexDirectory);
        vocabularyDecoder = initVocabularyDecoder(indexDirectory);
        vocabularyBlocks = new ArrayList<>();
        postingListInfos = new ArrayList<>();
        loadVocabularyTable(indexDirectory);
        postings = initPostings(indexDirectory);
    }

    @SneakyThrows
    private List<String> loadDocumentsMap(Path indexDirectory) {
        return new ArrayList<>(Files.readAllLines(indexDirectory.resolve(Indexer.DOCUMENTS_MAP_FILE_NAME)));
    }

    @SneakyThrows
    private VocabularyDecoder initVocabularyDecoder(Path indexDirectory) {
        byte[] vocabularyString = Files.readAllBytes(indexDirectory.resolve(Indexer.VOCABULARY_STRING_FILE_NAME));
        return new VocabularyFrontDecoder(vocabularyString, encodedInputStreamFactory);
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
        if (id < 0 || id >= documentsMap.size())
            return null;
        return documentsMap.get(id);
    }

    @Override
    @SneakyThrows
    public List<Integer> getDocumentIds(String term) {
        int index = getPostingListInfoIndex(term);
        if (index == -1) return List.of();
        PostingListInfo info = postingListInfos.get(index);
        byte[] ids = new byte[getPostingListSize(index)];
        ByteBuffer buffer = ByteBuffer.wrap(ids);
        postings.read(buffer, info.position());
        try (EncodedInputStream is = encodedInputStreamFactory.apply(new ByteArrayInputStream(ids))) {
            List<Integer> result = new ArrayList<>(info.frequency());
            int previousId = 0;
            for (int i = 0; i < info.frequency(); i++) {
                previousId += is.readInt();
                result.add(previousId);
            }
            return result;
        }
    }

    @Override
    public Tokenizer getTokenizer() {
        return tokenizer;
    }

    @Override
    public List<Integer> getAllDocumentIds() {
        return Stream.iterate(0, x -> x < documentsMap.size(), x -> x + 1).toList();
    }

    @Override
    public int getDocumentFrequency(String term) {
        int index = getPostingListInfoIndex(term);
        return index == -1 ? 0 : postingListInfos.get(index).frequency();
    }

    @Override
    @SneakyThrows
    public void close() {
        postings.close();
    }

    private int getPostingListInfoIndex(String term) {
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
    private int getPostingListSize(int index) {
        if (index == postingListInfos.size() - 1)
            return (int) (postings.size() - postingListInfos.getLast().position());
        else
            return (int) (postingListInfos.get(index + 1).position() - postingListInfos.get(index).position());
    }

    private record PostingListInfo(int frequency, long position) {}

    private final class VocabularyBlock {
        private final int position;

        private VocabularyBlock(int position) {
            this.position = position;
        }

        public int indexInBlock(String value) {
            vocabularyDecoder.seek(position);
            byte[] prefix = vocabularyDecoder.readPrefix();
            for (int i = 0; i < VocabularyFrontEncoder.TERMS_PER_BLOCK; i++) {
                String term = vocabularyDecoder.readTerm(prefix);
                int comparison = value.compareTo(term);
                if (comparison == 0) return i;
                else if (comparison < 0 && i == 0) return -1;
                else if (comparison > 0 && i == VocabularyFrontEncoder.TERMS_PER_BLOCK - 1) return VocabularyFrontEncoder.TERMS_PER_BLOCK;
            }
            return -2;
        }
    }
}
