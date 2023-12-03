import java.sql.*;

import org.apache.commons.dbcp2.*;

import javax.servlet.http.HttpServletResponse;

public class AlbumsDao {

    public AlbumsDao() {
    }

    public ImageMetaData createNewAlbum(Connection conn, AlbumInfo albumInfo, String imageSize) throws SQLException {

        PreparedStatement preparedStatement = null;

        String insertQueryStatement = "INSERT INTO `albumInfo` (`artist`,`title`, `year`,`imageSize`) VALUES (?,?,?,?);";

        preparedStatement = conn.prepareStatement(insertQueryStatement, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, albumInfo.getArtist());
        preparedStatement.setString(2, albumInfo.getTitle());
        preparedStatement.setString(3, albumInfo.getYear());
        preparedStatement.setString(4, imageSize);
        // execute insert SQL statement

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected == 1) {
            // Retrieve the generated keys
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                String albumId = String.valueOf(generatedKeys.getInt(1));
                // Now you have the albumId for the inserted record
                return new ImageMetaData(albumId, imageSize);
            } else {
                // Handle the case where no generated keys are returned
                throw new SQLException("Error: No generated keys found.");
            }
        } else {
            throw new SQLException("rowsAffected is not 1");
        }


    }


    public AlbumInfo getAlbum(Connection conn, String albumId) throws SQLException {

        PreparedStatement preparedStatement = null;
        String queryStatement = "SELECT * FROM albumInfo WHERE albumId = ?;";

        preparedStatement = conn.prepareStatement(queryStatement);
        preparedStatement.setString(1, albumId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            // Retrieve data from the result set
            String title = resultSet.getString("title");
            String artist = resultSet.getString("artist");
            String year = resultSet.getString("year");
            if (title == "" || artist == "" || year == "") throw new SQLException("Album info is not sufficient");
            return new AlbumInfo(artist, title, year);
        } else {
            // Handle the case where the album ID doesn't exist (return a 404 response)
            throw new SQLException("no query result");
        }
    }
}