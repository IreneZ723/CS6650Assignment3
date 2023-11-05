package Part2;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CalculateResponseTime {
    private String csvPath;

    public CalculateResponseTime(String csvPath) {
        this.csvPath = csvPath;
    }

    public String getCsvPath() {
        return csvPath;
    }

    public void setCsvPath(String csvPath) {
        this.csvPath = csvPath;
    }

    public void printResult() throws IOException {
        List<ResponseRecord> responseRecords = readCSV(csvPath);

        List<Long> getResponseTimes = getResponseTimesByType(responseRecords, "GET");
        List<Long> postResponseTimes = getResponseTimesByType(responseRecords, "POST");

        System.out.println("GET Request Statistics:");
        printStatistics(getResponseTimes);

        System.out.println("\nPOST Request Statistics:");
        printStatistics(postResponseTimes);
    }

    public static List<ResponseRecord> readCSV(String csvFilePath) throws IOException {
        List<ResponseRecord> responseRecords = new ArrayList<>();
        FileReader fileReader = new FileReader(csvFilePath);
        CSVParser csvParser = CSVParser.parse(fileReader, CSVFormat.DEFAULT);

        for (CSVRecord record : csvParser) {
            
            if (record.get(0).equals("startTime")) continue;
            if (record.size() == 4 && !record.get(0).isEmpty() && !record.get(1).isEmpty() && !record.get(2).isEmpty() && !record.get(3).isEmpty()) {
                ResponseRecord responseRecord = new ResponseRecord(
                        record.get(0), // Timestamp
                        record.get(1), // Request Type
                        Long.parseLong(record.get(2)), // Latency
                        Integer.parseInt(record.get(3)) // Response Code
                );
                responseRecords.add(responseRecord);
            }

        }

        return responseRecords;
    }

    public static List<Long> getResponseTimesByType(List<ResponseRecord> responseRecords, String requestType) {
        return responseRecords.stream()
                .filter(record -> record.getRequestType().equals(requestType))
                .map(ResponseRecord::getLatency)
                .collect(Collectors.toList());
    }

    public static void printStatistics(List<Long> responseTimes) {
        if (responseTimes.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        // Sort the response times
        Collections.sort(responseTimes);

        // Calculate mean response time
        long mean = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();

        // Calculate median response time
        int middle = responseTimes.size() / 2;
        long median = middle % 2 == 0 ?
                (responseTimes.get(middle - 1) + responseTimes.get(middle)) / 2 :
                responseTimes.get(middle);

        // Calculate p99 (99th percentile) response time
        int p99Index = (int) Math.ceil(0.99 * responseTimes.size()) - 1;
        long p99 = responseTimes.get(p99Index);

        // Calculate min and max response times
        long min = responseTimes.get(0);
        long max = responseTimes.get(responseTimes.size() - 1);

        System.out.println("Mean Response Time (ms): " + mean);
        System.out.println("Median Response Time (ms): " + median);
        System.out.println("P99 Response Time (ms): " + p99);
        System.out.println("Min Response Time (ms): " + min);
        System.out.println("Max Response Time (ms): " + max);
    }
}

class ResponseRecord {
    private String timestamp;
    private String requestType;
    private long latency;
    private int responseCode;

    public ResponseRecord(String timestamp, String requestType, long latency, int responseCode) {
        this.timestamp = timestamp;
        this.requestType = requestType;
        this.latency = latency;
        this.responseCode = responseCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getRequestType() {
        return requestType;
    }

    public long getLatency() {
        return latency;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
