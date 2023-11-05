
import java.io.File;

public class NewAlbum {

  private AlbumInfo profile;
  private File image;

  public NewAlbum(AlbumInfo profile, File image) {
    this.profile = profile;
    this.image = image;
  }

  public AlbumInfo getProfile() {
    return profile;
  }

  public void setProfile(AlbumInfo profile) {
    this.profile = profile;
  }

  public File getImage() {
    return this.image;
  }

  public void setImage(File image) {
    this.image = image;
  }
}
