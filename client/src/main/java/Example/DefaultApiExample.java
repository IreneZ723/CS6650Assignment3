package Example;

import io.swagger.client.*;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;

public class DefaultApiExample {

    public static void main(String[] args) {

        DefaultApi apiInstance = new DefaultApi();
        ApiClient apiClient = apiInstance.getApiClient();
        apiClient.setBasePath("http://54.244.63.107:8080");
        String albumID = "3"; // String | path  parameter is album key to retrieve
        try {
            AlbumInfo result = apiInstance.getAlbumByKey(albumID);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DefaultApi#getAlbumByKey");
            e.printStackTrace();
        }
    }
}