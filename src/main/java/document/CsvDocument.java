package document;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvDocument extends FileDocument {

    public final static String EXTENSION = "csv";
    private final String[] headers;
    private final CSVFormat csvFormat;

    public CsvDocument(Path path, String... headers) {
        super(path);
        this.headers = headers;
        this.csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();
    }

    public CsvDocument(String path, String... headers) {
        this(Path.of(path), headers);
    }

    @Override
    public String getBody() {
        return String.join(" ", getZones());
    }

    @Override
    @SneakyThrows
    public List<String> getZones() {
        try (CSVParser parser = csvFormat.parse(Files.newBufferedReader(path))){
            CSVRecord r = parser.getRecords().getFirst();
            List<String> zones = new ArrayList<>(headers.length);
            for (String header : headers)
                zones.add(r.get(header));
            return zones;
        }
    }

    @Override
    protected String getExpectedExtension() {
        return EXTENSION;
    }
}
