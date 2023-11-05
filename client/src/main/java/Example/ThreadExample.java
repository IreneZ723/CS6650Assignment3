package Example;

import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumsProfile;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class ThreadExample {
    final static private int NUMTHREADS = 1000;
    private int count = 0;

    synchronized public void inc() {
        count++;
    }

    public int getVal() {
        return this.count;
    }

    public static void main(String[] args) throws InterruptedException {
        final ThreadExample counter = new ThreadExample();
        CountDownLatch completed = new CountDownLatch(NUMTHREADS);
        DefaultApi apiInstance = new DefaultApi();
        apiInstance.getApiClient().setBasePath("http://54.213.246.123:8080/javaServlet_war");
        File image = new File("albumImageTest.png");
        AlbumsProfile profile = new AlbumsProfile();

        for (int i = 0; i < NUMTHREADS; i++) {
            // lambda runnable creation - interface only has a single method so lambda works fine
            Runnable thread = () -> {
                
                counter.inc();
                completed.countDown();
            };
            new Thread(thread).start();
        }

        completed.await();
        System.out.println("Value should be equal to " + NUMTHREADS + " It is: " + counter.getVal());
    }
}
