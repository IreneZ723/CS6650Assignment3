import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Recv {

    private final static String QUEUE_NAME = "review";
    private static LikeDao likeDao;
    private static DBCPDataSource dbcpDataSource;
    private static ConnectionFactory factory;


    public static void main(String[] argv) throws Exception {
        likeDao = new LikeDao();
        Connection rbmqConnection = null;
//        java.sql.Connection dbConnection;
//        dbcpDataSource = new DBCPDataSource();
//        dbConnection = dbcpDataSource.getDataSource().getConnection();
        factory = new ConnectionFactory();
//        factory.setUri("amqps://b-c8349341-ec91-4a78-ad9c-a57f23f235bb.mq.us-west-2.amazonaws.com:5671");
//
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("password");
        rbmqConnection = factory.newConnection();


        Channel channel = rbmqConnection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            String[] parts = message.split("\\|");
            if (parts.length == 2) {
                String albumID = parts[0];
                String likeOrDislike = parts[1];

                // Update the database based on the received message
                boolean likeAlbum = "like".equals(likeOrDislike);
                try {
                    //likeDao.LikeOrDislikeAlbum(dbConnection, albumID, likeAlbum);
                    System.out.println("Database updated for albumID: " + albumID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }
}