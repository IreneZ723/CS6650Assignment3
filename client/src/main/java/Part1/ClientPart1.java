package Part1;

import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientPart1 {
    final static private int NUMTHREADS = 10; // Number of threads in the initialization phase
    private int threadGroupSize;
    private int numThreadGroups;
    private int delaySeconds;
    private String IPAddr;
    private Counter counter;

    public ClientPart1(int threadGroupSize, int numThreadGroups, int delaySeconds, String IPAddr) {
        this.threadGroupSize = threadGroupSize;
        this.numThreadGroups = numThreadGroups;
        this.delaySeconds = delaySeconds;
        this.IPAddr = IPAddr;
        this.counter = new Counter();
    }


    public void start() throws InterruptedException {

        //  Initialization phase : hard-coded URI
        long startUpStartTime = System.currentTimeMillis();


        // lambda runnable creation - interface only has a single method so lambda works fine
        // It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

        OneGroup(100, 1, 10, -1, IPAddr, counter);
        long startUpEndTime = System.currentTimeMillis();

        System.out.println("[Start Up]: The initialization phase completed in " + (startUpEndTime - startUpStartTime) + " milliseconds");
        System.out.println("[Start UP]: Throughput " + NUMTHREADS * 400 * 1000 / (startUpEndTime - startUpStartTime) + " req/sec");


        // Start step3
        long startTime = System.currentTimeMillis();
        OneGroup(100, numThreadGroups, threadGroupSize, delaySeconds, IPAddr, counter);

        long endTime = System.currentTimeMillis();
        double wallTime = (endTime - startTime) / 1000;
        Integer totalRequests = counter.getTotalReq();
        System.out.println("Total Request: " + totalRequests);
        System.out.println("Failed Request: " + counter.getFailedReq());
        System.out.println("Wall time " + wallTime + " seconds");
        System.out.println("Throughput: " + totalRequests / wallTime + "/s");


    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 4) {
            System.err.println("Usage: java Part1.ClientPart1 <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
            System.exit(1);
        }

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delaySeconds = Integer.parseInt(args[2]);
        String IPAddr = args[3];

        System.out.println("threadGroupSize :" + threadGroupSize + " numThreadGroups " + numThreadGroups + " delaySeconds " + delaySeconds + " IPAddr " + IPAddr);
        ClientPart1 client = new ClientPart1(threadGroupSize, numThreadGroups, delaySeconds, IPAddr);
        client.start();
    }

    private static void OneGroup(int requestPerThread, int numThreadGroups,
                                 int threadGroupSize, long delay, String IPAddr, Counter counter)
            throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(numThreadGroups * threadGroupSize);

        // Create a thread pool with the desired number of threads
        ExecutorService executorService = Executors.newFixedThreadPool(threadGroupSize * numThreadGroups);

        for (int i = 0; i < numThreadGroups; i++) {
            for (int j = 0; j < threadGroupSize; j++) {
                // Use the thread pool to execute tasks
                executorService.execute(new OneThread(requestPerThread, completed, IPAddr, counter));
            }

            if (delay > 0) {
                TimeUnit.SECONDS.sleep(delay);
            }
        }

        try {
            completed.await(); // Wait for all threads to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown(); // Shutdown the thread pool
        }

    }
}


