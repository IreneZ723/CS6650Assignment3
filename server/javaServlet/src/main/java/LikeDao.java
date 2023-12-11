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

    public LikeInfo getReviewById(Connection conn, String albumId) throws SQLException {
        PreparedStatement preparedStatement = null;
        String queryStatement = "SELECT * FROM albumInfo WHERE albumId = ?;";

        preparedStatement = conn.prepareStatement(queryStatement);
        preparedStatement.setString(1, albumId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            // Retrieve data from the result set
            int likes = resultSet.getInt("likes");
            int dislikes = resultSet.getInt("dislikes");
            if (dislikes == 0 || likes == 0) return new LikeInfo(2, 2);
            return new LikeInfo(likes, dislikes);
        } else {
            // Handle the case where the album ID doesn't exist (return a 404 response)
            throw new SQLException("no query result");
        }
    }
}

