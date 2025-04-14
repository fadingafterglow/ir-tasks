package clusterizer;

import document.Document;
import document.DocumentLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import structure.document.TfAwareIndex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

public class DefaultClusterizer implements Clusterizer {

    private static final int MAX_NUMBER_OF_THREADS = 10;
    private TfAwareIndex index;
    private Iterator<Integer> leaders;
    private int termId;
    private double[] lengths;
    private ConcurrentHashMap<Integer, ClosestLeader> documentLeaderMap;

    @Override
    public Map<Integer, List<Clusterizer.ClusteredDocument>> clusterize(TfAwareIndex tfAwareIndex) {
        return clusterize(tfAwareIndex, chooseLeaders(tfAwareIndex));
    }

    @Override
    public Map<Integer, List<Clusterizer.ClusteredDocument>> clusterize(TfAwareIndex tfAwareIndex, Collection<Integer> clustersLeaders) {
        index = tfAwareIndex;
        leaders = clustersLeaders.iterator();
        lengths = countLengths();
        documentLeaderMap = new ConcurrentHashMap<>();
        fillDocumentLeaderMap(clustersLeaders.size());
        Map<Integer, List<Clusterizer.ClusteredDocument>> result = invertDocumentLeaderMap();
        reset();
        return result;
    }

    private Set<Integer> chooseLeaders(TfAwareIndex index) {
        Random random = new Random();
        int documentsCount = index.documentsCount();
        int leadersCount = (int) Math.floor(Math.sqrt(documentsCount));
        Set<Integer> leaders = new HashSet<>(leadersCount);
        while (leaders.size() < leadersCount)
            leaders.add(random.nextInt(documentsCount));
        return leaders;
    }

    @SneakyThrows
    private double[] countLengths() {
        DoubleAdder[] lengths = initLengths();
        calculateSums(lengths);
        return calculateRoots(lengths);
    }

    private DoubleAdder[] initLengths() {
        int documentsCount = index.documentsCount();
        DoubleAdder[] lengths = new DoubleAdder[documentsCount];
        for (int i = 0; i < documentsCount; i++)
            lengths[i] = new DoubleAdder();
        return lengths;
    }

    @SneakyThrows
    private void calculateSums(DoubleAdder[] lengths) {
        termId = index.termsCount();
        int numberOfThreads = Math.min(MAX_NUMBER_OF_THREADS, termId);
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new LengthCounterThread(lengths);
            threads[i].start();
        }
        for (Thread thread : threads)
            thread.join();
    }

    private double[] calculateRoots(DoubleAdder[] lengths) {
        double[] normalized = new double[lengths.length];
        for (int i = 0; i < lengths.length; i++)
            normalized[i] = Math.sqrt(lengths[i].sum());
        return normalized;
    }

    private synchronized int nextTermId() {
        if (termId > 0)
            return --termId;
        return -1;
    }

    @SneakyThrows
    private void fillDocumentLeaderMap(int leadersCount){
        int numberOfThreads = Math.min(MAX_NUMBER_OF_THREADS, leadersCount);
        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new SimilarityCounterThread();
            threads[i].start();
        }
        for (Thread thread : threads)
            thread.join();
    }

    private Map<Integer, List<Clusterizer.ClusteredDocument>> invertDocumentLeaderMap() {
        Map<Integer, List<Clusterizer.ClusteredDocument>> result = new HashMap<>();
        documentLeaderMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    int documentId = e.getKey();
                    ClosestLeader closestLeader = e.getValue();
                    result.computeIfAbsent(closestLeader.leaderId, k -> new ArrayList<>())
                            .add(new ClusteredDocument(documentId, closestLeader.similarity));
                });
        return result;
    }

    private void reset() {
        index = null;
        leaders = null;
        termId = 0;
        lengths = null;
        documentLeaderMap = null;
    }

    private synchronized int nextLeader() {
        if (leaders.hasNext())
            return leaders.next();
        return -1;
    }

    private static class ClosestLeader {
        private int leaderId;
        private double similarity;

        public ClosestLeader(int leaderId, double similarity) {
            this.leaderId = leaderId;
            this.similarity = similarity;
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class ClusteredDocument implements Clusterizer.ClusteredDocument {
        private final int id;
        private final double similarity;
    }

    private class LengthCounterThread extends Thread {

        private final DoubleAdder[] lengths;

        public LengthCounterThread(DoubleAdder[] lengths) {
            this.lengths = lengths;
        }

        @Override
        public void run() {
            int termId = nextTermId();
            while (termId != -1) {
                double idf = index.getIdf(termId);
                for (TfAwareIndex.Entry entry : index.getEntries(termId)) {
                    int documentId = entry.getDocumentId();
                    double weight = entry.getTermFrequency() * idf;
                    lengths[documentId].add(weight * weight);
                }
                termId = nextTermId();
            }
        }
    }

    private class SimilarityCounterThread extends Thread {

        @Override
        public void run() {
            int leaderId = nextLeader();
            while (leaderId != -1) {
                fillMapForLeader(leaderId);
                leaderId = nextLeader();
            }
        }

        private void fillMapForLeader(int leaderId) {
            double[] dotProducts = computeDotProducts(leaderId);
            double leaderLength = lengths[leaderId];
            for (int id = 0; id < dotProducts.length; id++) {
                double similarity = dotProducts[id] / (leaderLength * lengths[id]);
                documentLeaderMap.compute(id, (k, v) -> {
                    if (v == null)
                        v = new ClosestLeader(leaderId, similarity);
                    else if (similarity > v.similarity) {
                        v.leaderId = leaderId;
                        v.similarity = similarity;
                    }
                    return v;
                });
            }
        }

        private double[] computeDotProducts(int leaderId) {
            Map<String, Long> leader = getLeaderBagOfWords(leaderId);
            double[] dotProducts = new double[index.documentsCount()];
            for (Map.Entry<String, Long> termInfo : leader.entrySet()) {
                double idf = index.getIdf(termInfo.getKey());
                double leaderWeight = termInfo.getValue() * idf;
                for (TfAwareIndex.Entry entry : index.getEntries(termInfo.getKey())) {
                    int documentId = entry.getDocumentId();
                    double weight = entry.getTermFrequency() * idf;
                    dotProducts[documentId] += leaderWeight * weight;
                }
            }
            return dotProducts;
        }

        private Map<String, Long> getLeaderBagOfWords(int leaderId) {
            Document leader = DocumentLoader.loadDocument(index.getDocumentName(leaderId));
            return index.getTokenizer().tokenizeAsStream(leader)
                    .collect(Collectors.groupingBy(
                            token -> token,
                            Collectors.counting())
                    );
        }
    }
}
