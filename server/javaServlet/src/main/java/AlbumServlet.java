import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.http.Part;

import java.sql.*;
import java.util.LinkedList;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 50,    // 50 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
@WebServlet(value = "/albums/*")
public class AlbumServlet extends HttpServlet {

    private Gson gson = new Gson();
    private static final String UPLOAD_DIR = "uploads";

    private static final String DB_URL = "jdbc:mysql://localhost:3306/Albums";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "88545344zhang";
    private Connection databaseConnection = initializeDatabaseConnection();


    private Connection initializeDatabaseConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace(); // Handle errors appropriately in your application
        }
        return connection;
    }
    // Simulate retrieving album information from a data source
    private String getAlbumInfo(String albumID) {
        // Replace this with your actual logic to fetch album info
        // Return null if album with the given key is not found
        // Otherwise, return JSON representation of the album info
        AlbumInfo albumExample = new AlbumInfo("Yanlin", "best", "2023");
        String albumInfo = this.gson.toJson(albumExample);
        return albumInfo;
    }

    private String postAlbum() {
        // Replace this with your actual logic to fetch album info
        // Return null if album with the given key is not found
        // Otherwise, return JSON representation of the album info
        ImageMetaData imageExample = new ImageMetaData("javaAlbumID", "normal");
        String imageInfo = this.gson.toJson(imageExample);
        return imageInfo;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Handle GET requests here

        // Retrieve the 'albumID' path parameter
        String albumID = request.getPathInfo().split("/")[1];


        // Assuming 'albumInfo' is a JSON representation of album information
//        String albumInfo = getAlbumInfo(albumID);

        if (databaseConnection != null) {
                // Perform database operations here

                System.out.println("Connection made");
                String selectQuery = "SELECT * FROM albumInfo WHERE albumId = ?";
                try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(selectQuery)) {
                    System.out.println("Connection made");
                    preparedStatement.setString(1, albumID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        // Retrieve data from the result set
                        String title = resultSet.getString("title");
                        String artist = resultSet.getString("artist");
                        String year = resultSet.getString("year");

                        // Process the retrieved data
                        AlbumInfo albumExample = new AlbumInfo(artist, title, year);
                        String albumInfo = this.gson.toJson(albumExample);


                        response.setContentType("application/json");
                        PrintWriter out = response.getWriter();
                        if (albumInfo != null) {
                            // If album information is found, send a 200 OK response with JSON data
                            response.setStatus(HttpServletResponse.SC_OK);

                            out.print(albumInfo);
                            out.flush();
                        } else {
                            System.out.println("is null");
                            // If album information is not found, send a 404 response
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.print("{\"Error in doGet\": \"Key not found\"}");
                            out.flush();
                        }

                    } else {
                        // Handle the case where the album ID doesn't exist (return a 404 response)
                        System.out.println("no query result");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } catch (SQLException e) {
                    // Handle SQL-related exceptios
                    e.printStackTrace();
                }
            } else {
                // Handle the case where the connection couldn't be established
                System.out.println("the connection couldn't be established");
            }


    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (databaseConnection != null) {
            // Perform database operations here
            LinkedList
            System.out.println("Connection made");
            String insertQuery = "INSERT INTO `Albums`.`albumInfo` (`artist`,`title`, `year`,`imageSize`) VALUES (?,?,?,?);";
            Part imagePart = request.getPart("image");
            Part profilePart = request.getPart("profile");
            if (imagePart == null || profilePart == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("Error: imagePart or profilePart is null");
                out.flush();
            }
            // gets absolute path of the web application
            String applicationPath = request.getServletContext().getRealPath("");
            // constructs path of the directory to save uploaded file
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
            request.setAttribute("message", uploadFilePath + " uploadFilePath!");
            // creates the save directory if it does not exists
            File fileSaveDir = new File(uploadFilePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdirs();
            }
            // Read 'image' binary data and save it to a file (you may change the file path as needed)
            String fileName = getFileName(imagePart);
            System.out.println("filename: " + fileName);
            imagePart.write(uploadFilePath + File.separator + fileName);
            request.setAttribute("message", fileName + " File uploaded successfully!");

            // TODO: Parse profile object
            // Use objectMapper to read and parse the JSON from the InputStream
            InputStream profileInputStream = profilePart.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode profileJsonNode = objectMapper.readTree(profileInputStream);
            String artist = profileJsonNode.get("artist").asText();
            String title = profileJsonNode.get("title").asText();
            String year = profileJsonNode.get("year").asText();
            System.out.println("artist: "+ artist + "title: " + title + "year: " + year);


            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery,Statement.RETURN_GENERATED_KEYS)) {
                System.out.println("Connection made");

                preparedStatement.setString(1,artist);

                preparedStatement.setString(2,title);

                preparedStatement.setString(3,year);

                preparedStatement.setString(4,"normal");
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected == 1) {
                    // Retrieve the generated keys
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int albumId = generatedKeys.getInt(1);
                        // Now you have the albumId for the inserted record
                        out.print("Generated albumId: " + albumId);
                        out.flush();
                        // You can use albumId or return it as needed
                    } else {
                        // Handle the case where no generated keys are returned
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("Error: No generated keys found.");
                        out.flush();
                    }
                } else {
                    // Handle the case where no rows were inserted
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("Error: No rows inserted.");
                    out.flush();
                }
            } catch (SQLException e) {
                // Handle SQL-related exceptios
                e.printStackTrace();
            }
        } else {
            // Handle the case where the connection couldn't be established
            System.out.println("the connection couldn't be established");
        }
    }

    /**
     * Utility method to get file name from HTTP header content-disposition
     */
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println("content-disposition header= " + contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
