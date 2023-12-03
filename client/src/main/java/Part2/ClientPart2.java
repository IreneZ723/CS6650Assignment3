package Part2;

import Part1.Counter;
import Part1.OneThread;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientPart2 {
    final static private int NUMTHREADS = 10; // Number of threads in the initialization phase

    private int threadGroupSize;
    private int numThreadGroups;
    private int delaySeconds;
    private String IPAddr;
    private Counter counter;
    protected static CSVPrinter csvPrinter;


    public ClientPart2(int threadGroupSize, int numThreadGroups, int delaySeconds, String IPAddr) throws IOException {
        this.threadGroupSize = threadGroupSize;
        this.numThreadGroups = numThreadGroups;
        this.delaySeconds = delaySeconds;
        this.IPAddr = IPAddr;
        this.counter = new Counter();
        String server = IPAddr.contains("cs6650-a2-alb") ? "LB" : "non-LB";
        String filename = String.format("latency_rcds-%s-%d.csv", server, numThreadGroups);
        //System.out.println("filename: " + filename);
        this.csvPrinter = new CSVPrinter(new FileWriter(filename, false), CSVFormat.DEFAULT);

    }


    public void start() throws InterruptedException, IOException {

        //  Initialization phase : hard-coded URI
        long startUpStartTime = System.currentTimeMillis();

        OneGroup2(100, 1, 10, -1, IPAddr, counter);
        long startUpEndTime = System.currentTimeMillis();

        System.out.println("[Start Up]: The initialization phase completed in " + (startUpEndTime - startUpStartTime) + " milliseconds");
        System.out.println("[Start UP]: Throughput " + NUMTHREADS * 200 * 1000 / (startUpEndTime - startUpStartTime) + " req/sec");


        // Start step3
        csvPrinter.printRecord("startTime", "type", "latency", "statusCode");
        long startTime = System.currentTimeMillis();
        OneGroup2(1000, numThreadGroups, threadGroupSize, delaySeconds, IPAddr, counter);

        long endTime = System.currentTimeMillis();
        csvPrinter.close();
        double wallTime = (endTime - startTime) / 1000;
        Integer totalRequests = counter.getTotalReq();
        System.out.println("Total Request: " + totalRequests);
        System.out.println("Failed Request: " + counter.getFailedReq());
        System.out.println("Wall time " + wallTime + " seconds");
        System.out.println("Throughput: " + totalRequests / wallTime + "/s");


    }

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length != 4) {
            System.err.println("Usage: java Part2.ClientPart2 <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
            System.exit(1);
        }

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delaySeconds = Integer.parseInt(args[2]);
        String IPAddr = args[3];
        System.out.println("threadGroupSize :" + threadGroupSize + " numThreadGroups " + numThreadGroups + " delaySeconds " + delaySeconds + " IPAddr " + IPAddr);
        ClientPart2 client = new ClientPart2(threadGroupSize, numThreadGroups, delaySeconds, IPAddr);
        client.start();
    }


    private static void OneGroup2(int requestPerThread, int numThreadGroups,
                                  int threadGroupSize, long delay, String IPAddr, Counter counter)
            throws InterruptedException {
        CountDownLatch completed2 = new CountDownLatch(numThreadGroups * threadGroupSize);
        // Create a thread pool with the desired number of threads
        ExecutorService executorService = Executors.newFixedThreadPool(threadGroupSize * numThreadGroups);

        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                // Use the thread pool to execute tasks
                executorService.execute(new OneThread2(requestPerThread, completed2, IPAddr, counter));
            }

            if (delay > 0) {
                TimeUnit.SECONDS.sleep(delay);
            }
        }

        try {
            completed2.await(); // Wait for all threads to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown(); // Shutdown the thread pool
        }

    }
}


