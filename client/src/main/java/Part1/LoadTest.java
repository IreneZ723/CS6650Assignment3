package Part1;

public class LoadTest {
    private int threadGroupSize;
    private int numThreadGroups;
    private int delaySeconds;
    private String IPAddr;

    public LoadTest(int threadGroupSize, int numThreadGroups, int delaySeconds, String IPAddr) {
        this.threadGroupSize = threadGroupSize;
        this.numThreadGroups = numThreadGroups;
        this.delaySeconds = delaySeconds;
        this.IPAddr = IPAddr;
    }

    public void callJavaServer() throws InterruptedException {

        System.out.format("[Part1 Client]Load test for Java server with ThreadGroup size of %d, numThreadGroups of %d delaySeconds of %d \n", threadGroupSize, numThreadGroups, delaySeconds);
        ClientPart1 client = new ClientPart1(threadGroupSize, numThreadGroups, delaySeconds, IPAddr);
        client.start();
    }

    public void callGoServer() throws InterruptedException {
        System.out.format("[Part1 Client]Load test for Go server with ThreadGroup size of %d, numThreadGroups of %d delaySeconds of %d \n", threadGroupSize, numThreadGroups, delaySeconds);
        ClientPart1 client = new ClientPart1(threadGroupSize, numThreadGroups, delaySeconds, IPAddr);
        client.start();
    }

    public static void main(String[] args) {
        LoadTest loadTest1 = new LoadTest(10, 10, 2, "http://34.218.242.155:8080");
        LoadTest loadTest2 = new LoadTest(10, 20, 2, "http://34.218.242.155:8080");
        LoadTest loadTest3 = new LoadTest(10, 30, 2, "http://34.218.242.155:8080");

        // Java Server
        try {
            loadTest1.callGoServer();
            loadTest2.callGoServer();
            loadTest3.callGoServer();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
