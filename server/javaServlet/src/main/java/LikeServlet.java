import com.google.gson.Gson;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

@WebServlet(value = "/review/*")
public class LikeServlet extends HttpServlet {
    private final static String QUEUE_NAME = "review";
    private Gson gson;
    private ConnectionFactory rbmqfactory;
    private Connection rbmqConnection;
    private LikeDao likeDao;
    private DBCPDataSource dbcpDataSource;
    private java.sql.Connection conn;

    @Override
    public void init() throws ServletException {
        this.gson = new Gson();
        this.likeDao = new LikeDao();
        this.dbcpDataSource = new DBCPDataSource();
        try {
            this.conn = this.dbcpDataSource.getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            rbmqfactory = new ConnectionFactory();
            rbmqfactory.setHost("localhost");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Error during initializing connection factory" + e.getMessage());
        }

        try {
            this.rbmqConnection = rbmqfactory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Error during initializing new connection" + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Extract the albumID from the request URL
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            // Handle invalid or missing albumID in the path
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{ \"error\": \"Invalid or missing albumID in the path\" }");
            return;
        }

        String albumID = pathInfo.split("/")[1];

        PrintWriter out = response.getWriter();
        try {
            LikeInfo likeInfo = likeDao.getReviewById(conn, albumID);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(this.gson.toJson(likeInfo));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            e.printStackTrace(response.getWriter());
            out.print("Fail to get liks and dislikes");
            out.flush();
        }
        // Replace this with your actual logic


    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] pathInfo = request.getPathInfo().split("/");

        // Check if the path is valid
        if (pathInfo.length != 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Extract values from the path
        String likeOrNot = pathInfo[1];
        String albumID = pathInfo[2];

        // Perform the like or dislike logic
        int like = performReview(likeOrNot, albumID);

        if (like == -1) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("There should be like or dislike in the url");
        }
        // Set the response status and content
        else {
            boolean likeAlbum = like == 1;
            try {
                String msg = albumID + "|" + (likeAlbum ? "like" : "dislike");
                sendToQueue(msg);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(" [x] Sent '" + gson.toJson(msg) + "'");

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                e.printStackTrace(response.getWriter());

            }
        }

        response.setContentType("application/json");
    }

    private int performReview(String likeOrNot, String albumID) {
        // Implement your logic to update likes or dislikes for the given albumID
        // You may want to interact with your database here
        // For simplicity, this example assumes that the albumID is valid
        // and does not perform actual database operations

        // Placeholder logic
        if ("like".equals(likeOrNot)) {
            // Increment likes
            System.out.println("Liked album with ID: " + albumID);
            return 1;
        } else if ("dislike".equals(likeOrNot)) {
            // Increment dislikes
            System.out.println("Disliked album with ID: " + albumID);
            return 0;
        } else {
            // Invalid likeOrNot value
            return -1;
        }
    }

    private void sendToQueue(String msg) throws Exception {
        try (Channel channel = rbmqConnection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + msg + "'");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            throw new Exception("Failed to send to the queue");
        }
    }
}