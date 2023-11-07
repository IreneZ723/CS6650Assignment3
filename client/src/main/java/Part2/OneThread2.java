package Part2;


import Part1.Counter;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class OneThread2 extends Thread {
    private static final int MAXTRIES = 5;
    private int requestPerThread;
    private CountDownLatch threadLatch;
    private String IPAddr;
    private Counter counter;

    public OneThread2(Integer requestPerThread, CountDownLatch threadLatch, String IPAddr, Counter counter) {
        this.requestPerThread = requestPerThread;
        this.threadLatch = threadLatch;
        this.IPAddr = IPAddr;
        this.counter = counter;
    }

    @Override
    public void run() {
        DefaultApi apiInstance = new DefaultApi();
        apiInstance.getApiClient().setBasePath(IPAddr);

        File image = new File("albumImageTest.png");
        AlbumsProfile exampleProfile = new AlbumsProfile("Yanlin", "client2", "1998");

        for (int i = 0; i < requestPerThread; i++) {

            int currPostTry = 0;
            int currGetTry = 0;
            while (currPostTry < MAXTRIES) {
                try {
                    long postStartTime = System.currentTimeMillis();
                    int postStatusCode = apiInstance.newAlbumWithHttpInfo(image, exampleProfile).getStatusCode();
                    if (postStatusCode / 100 == 2) {
                        long postEndTime = System.currentTimeMillis();
                        recordRequestInfo(postStartTime, "POST", postEndTime - postStartTime, postStatusCode);
                        break;
                    }
                    if (postStatusCode >= 400 && postStatusCode <= 599) {
                        // 4XX or 5XX response, retry the request
                        //System.out.println("Retry POST Requets");
                        currPostTry++;
                        continue;
                    }
                } catch (ApiException e) {
                    currPostTry++;
                    // e.printStackTrace();
                    //System.out.println("current Try" + currPostTry);
                    if (currPostTry >= MAXTRIES) {
                        // Retry limit reached
                        counter.incFailedReq(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while (currGetTry < MAXTRIES) {
                try {
                    long getStartTime = System.currentTimeMillis();

                    int getStatusCode = apiInstance.getAlbumByKeyWithHttpInfo("3").getStatusCode();
                    if (getStatusCode == 200) {
                        long getEndTime = System.currentTimeMillis();
                        recordRequestInfo(getStartTime, "GET", getEndTime - getStartTime, getStatusCode);
                        break;
                    }
                    if (getStatusCode >= 400 && getStatusCode <= 599) {
                        // 4XX or 5XX response, retry the request
                        //System.out.println("Retry Get Requets");
                        currGetTry++;
                        continue;
                    }
                } catch (ApiException e) {
                    // e.printStackTrace();
                    currGetTry++;
                    if (currGetTry >= MAXTRIES) {
                        // Retry limit reached
                        counter.incFailedReq(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        threadLatch.countDown(); //signal that this thread has completed
        counter.incrTotoalReq(requestPerThread * 2);
    }

    private void recordRequestInfo(long startTime, String requestType, long latency, int responseCode) throws IOException {
        // Record information in CSV format (start time, request type, latency, response code)

        ClientPart2.csvPrinter.printRecord(startTime, requestType, latency, responseCode);
    }
}
