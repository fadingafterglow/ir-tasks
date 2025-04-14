package clusterizer;

import structure.document.TfAwareIndex;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Clusterizer {

    Map<Integer, List<ClusteredDocument>> clusterize(TfAwareIndex tfAwareIndex);

    Map<Integer, List<ClusteredDocument>> clusterize(TfAwareIndex tfAwareIndex, Collection<Integer> clustersLeaders);

    interface ClusteredDocument {
        int getId();
        double getSimilarity();
    }
}
