package Part1;

import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class OneThread extends Thread {
    private static final int MAXTRIES = 5;
    private int requestPerThread;
    private CountDownLatch threadLatch;
    private String IPAddr;
    private Counter counter;

    public OneThread(Integer requestPerThread, CountDownLatch threadLatch, String IPAddr, Counter counter) {
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
        AlbumsProfile exampleProfile = new AlbumsProfile("Yanlin", "client1", "1998");

        for (int i = 0; i < requestPerThread; i++) {


            int currentTry = 0;
            int maxRetries = 5;
            int statusCode = -1;
            while (currentTry < maxRetries) {
                try {

                    statusCode = apiInstance.newAlbumWithHttpInfo(image, exampleProfile).getStatusCode();
                    if (statusCode / 100 == 2) break;
                    if (statusCode >= 400 && statusCode <= 599) {
                        // 4XX or 5XX response, retry the request
                        //System.out.println("Retry POST Requets");
                        currentTry++;
                        continue;
                    }
                } catch (ApiException e) {
                    currentTry++;
                    //e.printStackTrace();
                    //System.out.println("current Try" + currentTry);
                    if (currentTry >= maxRetries) {
                        // Retry limit reached
                        counter.incFailedReq(1);
                    }
                }
            }
            currentTry = 0;
            while (currentTry < MAXTRIES) {
                try {

                    statusCode = apiInstance.getAlbumByKeyWithHttpInfo("3").getStatusCode();
                    if (statusCode == 200) break;
                    if (statusCode >= 400 && statusCode <= 599) {
                        // 4XX or 5XX response, retry the request
                        System.out.println("Retry Get Requets");
                        currentTry++;
                        continue;
                    }
                } catch (ApiException e) {
                    //e.printStackTrace();
                    currentTry++;
                    if (currentTry >= maxRetries) {
                        // Retry limit reached
                        counter.incFailedReq(1);
                    }
                }
            }

        }

        threadLatch.countDown(); //signal that this thread has completed
        counter.incrTotoalReq(requestPerThread * 2);
    }
}
