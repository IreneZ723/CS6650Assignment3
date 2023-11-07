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

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 50,    // 50 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
@WebServlet(value = "/albums/*")
public class AlbumServlet extends HttpServlet {

    private Gson gson = new Gson();
    AlbumsDao albumsDao = new AlbumsDao();

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
            AlbumInfo albumInfo = albumsDao.getAlbum(albumID);
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
        Part profilePart = request.getPart("profile");
        if (imagePart == null || profilePart == null) {
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
        String profileString = request.getParameter("profile");
        if (profileString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Make sure profile is valid");
            return;
        }
        
        if (profileString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Make sure profile is valid");
            return;
        }

        // Parse the JSON string into a Profile object
        ObjectMapper objectMapper = new ObjectMapper();
        Profile profile = null;

        try {
            profile = objectMapper.readValue(profileString, Profile.class);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid profile JSON");
            return;
        }

        // Now you can access the profile data
        String artist = profile.getArtist();
        String title = profile.getTitle();
        String year = profile.getYear();

        try {
            ImageMetaData newAlbum = albumsDao.createNewAlbum(new AlbumInfo(artist, title, year), sizeString);
            if (newAlbum == null) throw new SQLException("Error creating album\n");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(this.gson.toJson(newAlbum));

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
        ;
    }


}
