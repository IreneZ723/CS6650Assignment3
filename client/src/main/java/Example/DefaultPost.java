package Example;

import io.swagger.client.*;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.io.File;

public class DefaultPost {

    public static void main(String[] args) {

        DefaultApi apiInstance = new DefaultApi();
        apiInstance.getApiClient().setBasePath("http://54.201.6.1:8080/javaServlet_war");
        File image = new File("albumImageTest.png");
        AlbumsProfile profile = new AlbumsProfile("Yanlin", "Title", "Year");
        for (int i = 0; i < 100; i++) {
            try {
                long start = System.currentTimeMillis();

                apiInstance.newAlbumWithHttpInfo(image, profile);
                apiInstance.getAlbumByKey("1");
                long end = System.currentTimeMillis();
                System.out.println(end - start);
            } catch (ApiException e) {
                System.err.println("Exception when calling DefaultApi#post" + e.getCode());
                e.printStackTrace();
            }
        }

    }
}