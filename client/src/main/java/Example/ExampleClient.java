package Example;

import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.api.LikeApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class ExampleClient {
    final static private int NUMTHREADS = 100;
    private int count = 0;

    synchronized public void inc() {
        count++;
    }

    public int getVal() {
        return this.count;
    }

    public static void main(String[] args) throws InterruptedException {
        final ExampleClient counter = new ExampleClient();
        CountDownLatch completed = new CountDownLatch(NUMTHREADS);


        File image = new File("albumImageTest.png");
        AlbumsProfile profile = new AlbumsProfile("yanlin", "sda", "year");
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUMTHREADS; i++) {
            // lambda runnable creation - interface only has a single method so lambda works fine

            Runnable thread = () -> {
                DefaultApi apiInstance = new DefaultApi();
                LikeApi likeApiInstance = new LikeApi();
                apiInstance.getApiClient().setBasePath("http://54.213.210.122:8080/javaServlet_war");
                likeApiInstance.getApiClient().setBasePath("http://54.213.210.122:8080/javaServlet_war");
                for (int j = 0; j < 100; j++) {
                    try {
                        apiInstance.newAlbum(image, profile);
                        likeApiInstance.reviewWithHttpInfo("dislike", "1");
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                }
                counter.inc();
                completed.countDown();
            };
            new Thread(thread).start();
        }

        completed.await();

        long end = System.currentTimeMillis();
        System.out.println("Walltime:" + (end - start));
        System.out.println("Value should be equal to " + NUMTHREADS + " It is: " + counter.getVal());
        System.out.println("[Start UP]: Throughput " + NUMTHREADS * 200 * 1000 / (end - start) + " req/sec");


    }
}
