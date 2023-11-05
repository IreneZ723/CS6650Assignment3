public class ImageMetaData {


    private String albumID;
    private String imageSize;

    @Override
    public String toString() {
        return "ImageMetaData{" +
                "albumID='" + albumID + '\'' +
                ", imageSize='" + imageSize + '\'' +
                '}';
    }

    public ImageMetaData(String albumID, String imageSize) {
        this.albumID = albumID;
        this.imageSize = imageSize;
    }

    // Getters and setters
    public String getAlbumID() {
        return albumID;
    }

    public void setAlbumID(String albumID) {
        this.albumID = albumID;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }
}
