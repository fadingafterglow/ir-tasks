package document;

import java.util.List;

public interface Document {
    String getName();
    String getBody();
    List<String> getZones();
    long getSize();
}
