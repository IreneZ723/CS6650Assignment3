import java.io.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.sql.*;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 50,    // 50 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
@WebServlet(value = "/albums/*")
public class AlbumServlet extends HttpServlet {

    private Gson gson = new Gson();
    private AlbumsDao albumsDao;
    private DBCPDataSource dbcpDataSource;
    private Connection conn;

    @Override
    public void init() {
        this.albumsDao = new AlbumsDao();
        this.dbcpDataSource = new DBCPDataSource();
        try {
            this.conn = this.dbcpDataSource.getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Handle GET requests here

        // Retrieve the 'albumID' path parameter
        String albumID = request.getPathInfo().split("/")[1];


        // Assuming 'albumInfo' is a JSON representation of album information

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            AlbumInfo albumInfo = albumsDao.getAlbum(conn, albumID);
            if (albumInfo == null) throw new SQLException("Error getting album\n");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(this.gson.toJson(albumInfo));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            e.printStackTrace(response.getWriter());
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Part imagePart = request.getPart("image");
        if (imagePart == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("Error: imagePart or profilePart is null");
            out.flush();
        }

        // parse image
        InputStream imageInputStream = imagePart.getInputStream();
        // Process the file content
        byte[] buffer = new byte[1024];
        int bytesRead;
        long size = 0;

        while ((bytesRead = imageInputStream.read(buffer)) != -1) {
            size += bytesRead;
        }
        String sizeString = Long.toString(size);

        // Parse profile object
        // Use objectMapper to read and parse the JSON from the InputStream
//        String profileString = request.getParameter("profile");
//        if (profileString == null) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.getWriter().write("Make sure profile is valid");
//            return;
//        }
//        System.out.println(profileString);
//        String[] lines = profileString.split("\n");
        String artist = null;
        String title = null;
        String year = null;
//
//        for (String line : lines) {
//            line = line.trim();
//            if (line.startsWith("artist:")) {
//                artist = line.split(":")[1].trim();
//            } else if (line.startsWith("title:")) {
//                title = line.split(":")[1].trim();
//            } else if (line.startsWith("year:")) {
//                year = line.split(":")[1].trim();
//            }
//        }
//
//        if (artist == null || title == null || year == null) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            response.getWriter().write("Profile data is incomplete" + artist + title + year);
//            return;
//        }

        Part profilePart = request.getPart("profile");
        JsonObject profileObject = null;
        if (profilePart != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(profilePart.getInputStream()))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String profileJson = stringBuilder.toString();

                if (profileJson.substring(0, 1).equals("c")) {
                    profileJson = profileJson.replace("class AlbumsProfile {", "").replace("}", "").trim();
                    String[] keyValuePairs = profileJson.split(":\\s+string");
                    StringBuilder jsonString = new StringBuilder();
                    jsonString.append("{");
                    for (int i = 0; i < keyValuePairs.length - 1; i++) {
                        String[] parts = keyValuePairs[i].split(":\\s+");
                        jsonString.append("\"").append(parts[0].trim()).append("\":\"string\",");
                    }
                    String lastPair = keyValuePairs[keyValuePairs.length - 1].trim();
                    String[] parts = lastPair.split(":\\s+");
                    jsonString.append("\"").append(parts[0].trim()).append("\":\"string\"");
                    jsonString.append("}");
                    profileJson = jsonString.toString();
//          profileJson.replaceAll("\":\"", "\":");
                }
                Gson gson = new Gson();
                profileObject = gson.fromJson(profileJson, JsonObject.class);
            }
        }

        if (profileObject != null) {
            artist = profileObject.get("artist").getAsString();
            title = profileObject.get("title").getAsString();
            year = profileObject.get("year").getAsString();
        }

        try {
            ImageMetaData newAlbum = albumsDao.createNewAlbum(conn, new AlbumInfo(artist, title, year), sizeString);
            if (newAlbum == null) throw new SQLException("Error creating album\n");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(this.gson.toJson(newAlbum));
            return;
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("cannot add new album!\n");
            out.print(System.getProperty("MySQL_IP_ADDRESS\n"));
            out.print(System.getProperty("MySQL_PORT\n"));
            out.print(System.getProperty("DB_USERNAME\n"));
            out.print(System.getProperty("DB_PASSWORD\n"));
            e.printStackTrace(response.getWriter());
            out.flush();
        }

    }

    @Override
    public void destroy() {
        if (this.conn != null) {
            try {
                this.conn.close();
                this.dbcpDataSource.getDataSource().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Connection closed");
        }
    }


}