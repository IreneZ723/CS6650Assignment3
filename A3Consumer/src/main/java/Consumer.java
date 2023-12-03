import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Consumer {
    private final static  int numThreads = 20; // Adjust the number of threads as needed
    private final static String QUEUE_NAME = "review";
    private static LikeDao likeDao;
    private static DBCPDataSource dbcpDataSource;
    private static ConnectionFactory factory;
    private static Connection rbmqConnection;
    private static java.sql.Connection dbConnection;
    private static ExecutorService executorService;



    public static void main(String[] argv) throws Exception {
        likeDao = new LikeDao();
        dbcpDataSource = new DBCPDataSource();
        dbConnection = dbcpDataSource.getDataSource().getConnection();

        factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        rbmqConnection = factory.newConnection();
        // Create multiple threads to consume messages
        executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    consumeMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Shutdown the thread pool when your application exits or is done consuming messages
        executorService.shutdown();
        System.out.println("Finished all threads");

    }

    private static void consumeMessages() throws Exception {

            Channel channel = rbmqConnection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Thread " + Thread.currentThread().getId() + " Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Thread " + Thread.currentThread().getId() + " Received '" + message + "'");
                String[] parts = message.split("\\|");
                if (parts.length == 2) {
                    String albumID = parts[0];
                    String likeOrDislike = parts[1];

                    // Update the database based on the received message
                    boolean likeAlbum = "like".equals(likeOrDislike);
                    try {
                        likeDao.LikeOrDislikeAlbum(dbConnection, albumID, likeAlbum);
                        System.out.println("Database updated for albumID: " + albumID + " like = " + likeAlbum + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
            Thread.sleep(Long.MAX_VALUE);


    }
}
