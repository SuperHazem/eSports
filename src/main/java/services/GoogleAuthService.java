package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final NetHttpTransport httpTransport;
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService() throws GeneralSecurityException, IOException {
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
        Task<Utilisateur> task = new Task<>() {
            @Override
            protected Utilisateur call() throws Exception {
                // Load client secrets
                InputStream in = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
                if (in == null) {
                    throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
                }
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

                // Build flow and trigger user authorization request
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline")
                        .build();

                LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
                Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

                // Get user info from Google
                GoogleIdToken idToken = verifier.verify(credential.getAccessToken());
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    String email = payload.getEmail();
                    String name = (String) payload.get("name");
                    
                    // Split name into first and last name (if possible)
                    String firstName = name;
                    String lastName = "";
                    if (name.contains(" ")) {
                        String[] parts = name.split(" ", 2);
                        firstName = parts[0];
                        lastName = parts[1];
                    }
                    
                    // Create a new user with SPECTATEUR role by default
                    // You can change this logic based on your requirements
                    return new Spectateur(Role.SPECTATEUR, "google-oauth", email, 0, lastName, firstName, new Date());
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            Utilisateur user = task.getValue();
            Platform.runLater(() -> callback.onAuthCompleted(user));
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            Platform.runLater(() -> callback.onAuthFailed(exception.getMessage()));
        });

        new Thread(task).start();
    }

    public interface GoogleAuthCallback {
        void onAuthCompleted(Utilisateur user);
        void onAuthFailed(String errorMessage);
    }
}