package Example;

import com.squareup.okhttp.Response;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;

public class ApiCaller {

    private String IPAddr;
    private DefaultApi apiInstance;


    public ApiCaller(String IPAddr, DefaultApi apiInstance) {
        apiInstance.getApiClient().setBasePath(IPAddr);
        this.apiInstance = apiInstance;
    }


    public int getAlbum(String albumID) throws ApiException {
        ApiResponse<AlbumInfo> resp = this.apiInstance.getAlbumByKeyWithHttpInfo(albumID);
        return resp.getStatusCode();
    }

    public int postAlbum(String filePath, AlbumsProfile profile) throws ApiException {
        File image = new File(filePath);
        ApiResponse<ImageMetaData> response = this.apiInstance.newAlbumWithHttpInfo(image, profile);
        // If the call was successful, break out of the loop
        return response.getStatusCode();

    }

}
