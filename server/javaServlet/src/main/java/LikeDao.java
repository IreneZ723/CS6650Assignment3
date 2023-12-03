import java.sql.*;

public class LikeDao {
    public LikeDao() {
    }

    public void LikeOrDislikeAlbum(Connection conn, String albumId, Boolean likeOrNot) throws SQLException {

        PreparedStatement preparedStatement = null;

        String updateQueryStatement = likeOrNot ? "UPDATE albumInfo SET likes = likes + 1 WHERE albumID = ?" : "UPDATE albumInfo SET dislikes = dislikes + 1 WHERE albumID = ?";

        preparedStatement = conn.prepareStatement(updateQueryStatement);
        preparedStatement.setString(1, albumId);
        // execute insert SQL statement

        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected != 1) {
            throw new SQLException("rowsAffected is not 1");
        }
    }
}

