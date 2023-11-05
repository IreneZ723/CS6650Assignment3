package Example;

import io.swagger.client.*;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.*;

import java.io.File;

public class DefaultPost {

    public static void main(String[] args) {

        DefaultApi apiInstance = new DefaultApi();
        apiInstance.getApiClient().setBasePath("http://54.213.246.123:8080/javaServlet_war");
        File image = new File("albumImageTest.png");
        AlbumsProfile profile = new AlbumsProfile();
        for (int i = 0; i < 10000; i++) {
            try {
                long start = System.currentTimeMillis();

                apiInstance.newAlbumWithHttpInfo(image, profile);
                long end = System.currentTimeMillis();
                System.out.println(end - start);
            } catch (ApiException e) {
                System.err.println("Exception when calling DefaultApi#post" + e.getCode());
                e.printStackTrace();
            }
        }

    }
}