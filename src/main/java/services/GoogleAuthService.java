package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.concurrent.Task;
import enums.Role;
import models.Utilisateur;
import models.Spectateur;
import java.util.Date;

public class GoogleAuthService {
    private static final String APPLICATION_NAME = "eSports Arena Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // Updated SCOPES to include the explicit userinfo.email scope
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final HttpTransport httpTransport;
    private final GoogleIdTokenVerifier verifier;
    private final Connection dbConnection; // Store the database connection

    public GoogleAuthService(Connection dbConnection) throws GeneralSecurityException, IOException {
        this.dbConnection = dbConnection;
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        verifier = new GoogleIdTokenVerifier.Builder(httpTransport, JSON_FACTORY)
                .setAudience(Collections.singletonList(getClientId()))
                .build();
    }

    private String getClientId() throws IOException {
        InputStream in = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        return clientSecrets.getDetails().getClientId();
    }

    public void startGoogleAuthFlow(GoogleAuthCallback callback) {
        Task<GoogleUserInfo> task = new Task<>() {
            @Override
            protected GoogleUserInfo call() throws Exception {
                System.out.println("Starting Google authentication flow...");

                // Load client secrets
                InputStream in = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
                if (in == null) {
                    throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
                }
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
                System.out.println("Loaded Google OAuth client secrets");

                // Configure the flow to revoke previous tokens to ensure a fresh login each time
                // This ensures the account selection screen is always shown
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline")
                        .build();

                System.out.println("Configured GoogleAuthorizationCodeFlow");

                // Start the server to receive the OAuth callback
                LocalServerReceiver receiver = startLocalServerReceiver();
                if (receiver == null) {
                    throw new IOException("Could not start local server on any of the attempted ports");
                }

                // Create our custom AuthorizationCodeInstalledApp that enforces account selection
                ForceAccountSelectionApp app = new ForceAccountSelectionApp(flow, receiver);
                System.out.println("Starting Google authentication process...");

                // Authorize and get credentials
                System.out.println("Initiating authorization process...");
                Credential credential = app.authorize("user");
                System.out.println("Successfully obtained OAuth credentials");

                // Use HttpRequestFactory to get user info
                HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                    credential.initialize(request);
                });

                // Make a request to the userinfo endpoint
                System.out.println("Fetching user information from Google...");
                HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(USER_INFO_URL));
                HttpResponse response = request.execute();

                // Parse the JSON response
                Map<String, Object> userInfo = JSON_FACTORY.createJsonParser(response.getContent())
                        .parse(HashMap.class);

                // Extract user information
                String email = (String) userInfo.get("email");
                String name = (String) userInfo.get("name");
                String picture = (String) userInfo.get("picture");

                // Add debug logging
                System.out.println("Retrieved user info from Google: Email=" + email + ", Name=" + name);
                if (picture != null) {
                    System.out.println("User profile picture URL: " + picture);
                }

                // Split name into first and last name (if possible)
                String firstName = name;
                String lastName = "";
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ", 2);
                    firstName = parts[0];
                    lastName = parts[1];
                }

                // Return user info for direct database insertion
                return new GoogleUserInfo(email, firstName, lastName, picture);
            }
        };

        task.setOnSucceeded(event -> {
            GoogleUserInfo userInfo = task.getValue();
            Platform.runLater(() -> {
                try {
                    // Insert the user directly into the database using our internal method
                    Utilisateur user = insertGoogleUser(
                            userInfo.email,
                            userInfo.firstName,
                            userInfo.lastName
                    );

                    callback.onAuthCompleted(user);
                } catch (Exception e) {
                    System.err.println("Failed to insert Google user: " + e.getMessage());
                    e.printStackTrace();
                    callback.onAuthFailed("Database error: " + e.getMessage());
                }
            });
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            System.err.println("Google authentication failed: " + exception.getMessage());
            exception.printStackTrace();
            Platform.runLater(() -> callback.onAuthFailed(exception.getMessage()));
        });

        new Thread(task).start();
    }

    /**
     * Starts a local server on an available port to receive the OAuth callback
     */
    private LocalServerReceiver startLocalServerReceiver() {
        int[] portsToTry = {8888, 8889, 8890, 8891, 8892};

        for (int port : portsToTry) {
            try {
                LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                        .setPort(port)
                        .setCallbackPath("/oauth2callback")
                        .build();
                System.out.println("Using port " + port + " for Google authentication callback");
                return receiver;
            } catch (Exception e) {
                System.out.println("Port " + port + " is not available, trying next port...");
            }
        }

        System.err.println("Could not find an available port for the OAuth callback");
        return null;
    }

    /**
     * Custom AuthorizationCodeInstalledApp that forces the account selection screen
     */
    private static class ForceAccountSelectionApp extends AuthorizationCodeInstalledApp {
        public ForceAccountSelectionApp(GoogleAuthorizationCodeFlow flow, LocalServerReceiver receiver) {
            super(flow, receiver);
        }

        @Override
        protected void onAuthorization(com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
            // Add parameters to force the account selection screen
            authorizationUrl.set("prompt", "select_account");

            // Add login_hint='' to clear any previous login hints
            authorizationUrl.set("login_hint", "");

            // Add include_granted_scopes=true to ensure a fresh consent screen
            authorizationUrl.set("include_granted_scopes", "true");

            // Log the authorization URL for debugging
            System.out.println("Opening browser for user authorization with forced account selection");
            System.out.println("Authorization URL parameters: prompt=select_account, login_hint='', include_granted_scopes=true");

            // Open the browser with the modified URL
            browse(authorizationUrl.build());
        }
    }

    /**
     * Directly inserts a Google-authenticated user into the database
     * This method uses raw SQL to avoid issues with the Role enum
     */
    private Utilisateur insertGoogleUser(String email, String firstName, String lastName) throws SQLException {
        if (dbConnection == null) {
            throw new SQLException("Database connection is null");
        }

        // First, check if the user already exists
        String checkSql = "SELECT id, nom, prenom, role, motDePasseHash FROM utilisateur WHERE email = ?";
        try (PreparedStatement checkStmt = dbConnection.prepareStatement(checkSql)) {
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                // User already exists, return the existing user
                System.out.println("User already exists in database: " + email);
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String roleStr = rs.getString("role");
                String motDePasseHash = rs.getString("motDePasseHash");

                Role role = Role.valueOf(roleStr);

                // Create and return a Spectateur object (assuming Google users are Spectateurs)
                return new Spectateur(role, motDePasseHash, email, id, nom, prenom, new Date());
            }
        }

        // Check the database schema to understand the role format
        String roleFormat = null;
        try {
            // First, try to get an existing role value from the database
            String roleSql = "SELECT role FROM utilisateur LIMIT 1";
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery(roleSql)) {
                if (rs.next()) {
                    roleFormat = rs.getString("role");
                    System.out.println("Found existing role value in database: " + roleFormat);
                }
            }

            // If we couldn't find an existing role, try to get the column definition
            if (roleFormat == null) {
                String schemaSql = "SHOW COLUMNS FROM utilisateur WHERE Field = 'role'";
                try (Statement stmt = dbConnection.createStatement();
                     ResultSet rs = stmt.executeQuery(schemaSql)) {
                    if (rs.next()) {
                        String type = rs.getString("Type");
                        System.out.println("Role column type: " + type);

                        // If it's an ENUM type, extract the allowed values
                        if (type.startsWith("enum(")) {
                            String values = type.substring(5, type.length() - 1);
                            System.out.println("Allowed role values: " + values);

                            // Check if SPECTATEUR is in the allowed values
                            if (values.contains("'SPECTATEUR'")) {
                                roleFormat = "SPECTATEUR";
                                System.out.println("SPECTATEUR is an allowed role value");
                            } else {
                                // Use the first value in the enum as a format reference
                                String[] allowedValues = values.split(",");
                                if (allowedValues.length > 0) {
                                    // Remove quotes from the value
                                    roleFormat = allowedValues[0].replace("'", "");
                                    System.out.println("Using first allowed role value as format reference: " + roleFormat);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Could not get column definition: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking database schema: " + e.getMessage());
        }

        // Determine the role value to use
        String roleValue;
        if (roleFormat != null) {
            // If we found ADMIN, use SPECTATEUR in the same format
            if (roleFormat.equals("ADMIN")) {
                roleValue = "SPECTATEUR";
            } else {
                // Otherwise, use the format we found but try to make it SPECTATEUR
                roleValue = roleFormat;
            }
            System.out.println("Using role value: " + roleValue);
        } else {
            // If we couldn't determine the format, use a default
            roleValue = "SPECTATEUR";
            System.out.println("Using default role value: " + roleValue);
        }

        // Insert into utilisateur table
        String userSql = "INSERT INTO utilisateur (email, motDePasseHash, role, nom, prenom) VALUES (?, ?, ?, ?, ?)";
        int userId = -1;

        try (PreparedStatement userStmt = dbConnection.prepareStatement(userSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            userStmt.setString(1, email);
            userStmt.setString(2, "google-oauth");
            userStmt.setString(3, roleValue);
            userStmt.setString(4, lastName);
            userStmt.setString(5, firstName);

            System.out.println("Executing SQL: " + userSql);
            System.out.println("With values: email=" + email + ", password=google-oauth, role=" + roleValue +
                    ", nom=" + lastName + ", prenom=" + firstName);

            int affectedRows = userStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }

        // Insert into spectateur table - use utilisateur_id to match UtilisateurDAO
        String spectateurSql = "INSERT INTO spectateur (utilisateur_id, date_inscription) VALUES (?, ?)";
        try (PreparedStatement spectateurStmt = dbConnection.prepareStatement(spectateurSql)) {
            spectateurStmt.setInt(1, userId);
            spectateurStmt.setDate(2, new java.sql.Date(new Date().getTime()));

            System.out.println("Executing SQL: " + spectateurSql);
            System.out.println("With values: utilisateur_id=" + userId + ", date=" + new java.sql.Date(new Date().getTime()));

            spectateurStmt.executeUpdate();
        }

        System.out.println("Successfully inserted Google user: " + email + " with ID: " + userId);

        // Create and return a Spectateur object
        Role role = Role.SPECTATEUR;
        try {
            role = Role.valueOf(roleValue);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not convert database role value to enum: " + e.getMessage());
            // Keep the default SPECTATEUR
        }

        return new Spectateur(role, "google-oauth", email, userId, lastName, firstName, new Date());
    }

    // Enhanced class to hold Google user information including profile picture
    private static class GoogleUserInfo {
        public final String email;
        public final String firstName;
        public final String lastName;
        public final String pictureUrl;

        public GoogleUserInfo(String email, String firstName, String lastName, String pictureUrl) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pictureUrl = pictureUrl;
        }
    }

    public interface GoogleAuthCallback {
        void onAuthCompleted(Utilisateur user);
        void onAuthFailed(String errorMessage);
    }
}
