package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import models.Utilisateur;
import utils.IconGenerator;
import utils.SceneController;
import utils.UserSession;

import java.io.File;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private VBox sidebar;
    @FXML
    private StackPane contentArea;
    @FXML
    private ImageView logoImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private javafx.scene.layout.HBox userProfileSection;
    @FXML
    private ImageView userAvatarImage;

    @FXML
    private Button tournoiBtn;
    @FXML
    private Button equipeBtn;
    @FXML
    private Button utilisateurBtn;
    @FXML
    private Button recompenseBtn;
    @FXML
    private Button sponsorBtn;
    @FXML
    private Button userAdminBtn; // Bouton pour la gestion des utilisateurs (bannissement, suspension)
    @FXML
    private Button adminReponseBtn;
    @FXML
    private Button adminViewBtn;
    @FXML
    private Button smartMatchmakingBtn;
    @FXML
    private Button gestionMatchBtn;
    @FXML
    private Button gestionAreneBtn;

    private Map<String, Parent> cachedViews = new HashMap<>();
    private Button currentActiveButton;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Style the logo
        styleLogoImage();
        
        // Style the user avatar
        styleUserAvatar();

        // Set the initial active button
        currentActiveButton = utilisateurBtn;
        
        // Load user data from session if available
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            setCurrentUser(session.getCurrentUser());
        }

        // Load the default view (Utilisateurs)
        try {
            loadView("utilisateur");
        } catch (IOException e) {
            System.err.println("Error loading default view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply styling to make the logo circular with glow effects
     */
    private void styleLogoImage() {
        if (logoImageView != null && logoImageView.getImage() != null) {
            // Create a circle clip for the logo
            double radius = Math.min(logoImageView.getFitWidth(), logoImageView.getFitHeight()) / 2;
            Circle clip = new Circle(radius, radius, radius);
            logoImageView.setClip(clip);

            // Add a glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(0, 247, 255, 0.7));
            glow.setRadius(15);
            logoImageView.setEffect(glow);

            // Center the logo
            logoImageView.setPreserveRatio(true);
            logoImageView.setSmooth(true);
            logoImageView.setCache(true);
        }
    }
    
    /**
     * Apply styling to make the user avatar circular
     */
    private void styleUserAvatar() {
        if (userAvatarImage != null) {
            // Create a circle clip for the avatar
            double radius = Math.min(userAvatarImage.getFitWidth(), userAvatarImage.getFitHeight()) / 2;
            Circle clip = new Circle(radius, radius, radius);
            userAvatarImage.setClip(clip);
            
            // Set image properties
            userAvatarImage.setPreserveRatio(true);
            userAvatarImage.setSmooth(true);
            userAvatarImage.setCache(true);
        }
    }

    /**
     * Set the current user and update UI accordingly
     */
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userRoleLabel.setText(user.getRole().toString());
            
            // Check if this is a Google-authenticated user
            boolean isGoogleUser = user.getMotDePasseHash() != null && user.getMotDePasseHash().equals("google-oauth");
            
            System.out.println("User authentication type check: " + user.getEmail() + ", isGoogleUser=" + isGoogleUser + ", hash=" + user.getMotDePasseHash());
            
            // Load profile picture if available
            if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                try {
                    File imageFile = new File(user.getProfilePicturePath());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        userAvatarImage.setImage(image);
                        System.out.println("Loaded profile picture from: " + user.getProfilePicturePath());
                    } else if (isGoogleUser) {
                        // For Google users, try to generate an icon if the profile picture file doesn't exist
                        System.out.println("Profile picture file doesn't exist for Google user, generating icon");
                        generateProfileIcon(user);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image in sidebar: " + e.getMessage());
                    if (isGoogleUser) {
                        // For Google users, generate an icon if there's an error loading the profile picture
                        System.out.println("Error loading profile image for Google user, generating icon");
                        generateProfileIcon(user);
                    }
                }
            } else if (isGoogleUser) {
                // For Google users without a profile picture path, generate an icon
                System.out.println("No profile picture path for Google user, generating icon");
                generateProfileIcon(user);
            } else {
                // For non-Google users without a profile picture, also generate an icon
                System.out.println("No profile picture for regular user, generating icon");
                generateProfileIcon(user);
            }
        }
    }
    
    /**
     * Generate a profile icon for users without a profile picture
     * This is especially useful for Google-authenticated users
     */
    private void generateProfileIcon(Utilisateur user) {
        try {
            // Use the IconGenerator utility to create an avatar based on user's initials
            String initials = String.valueOf(user.getPrenom().charAt(0)) + 
                             (user.getNom() != null && !user.getNom().isEmpty() ? 
                              String.valueOf(user.getNom().charAt(0)) : "");
            
            // Generate a color based on the user's name for consistency
            String fullName = user.getPrenom() + user.getNom();
            int hash = fullName.hashCode();
            // Generate a bright, saturated color (avoid dark colors for visibility)
            Color avatarColor = Color.hsb(
                (hash % 360), // Hue: 0-359 degrees
                0.8,          // Saturation: 80%
                0.9           // Brightness: 90%
            );
            
            // Create the image and set it to the avatar
            Image generatedIcon = IconGenerator.createTextIcon(initials.toUpperCase(), avatarColor);
            userAvatarImage.setImage(generatedIcon);
            
            System.out.println("Generated profile icon for Google user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Error generating profile icon: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavigation() throws IOException {
        Button clickedButton = (Button) mainBorderPane.getScene().getFocusOwner();

        // Update active button styling
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-nav-button");
        }
        clickedButton.getStyleClass().add("active-nav-button");
        currentActiveButton = clickedButton;

        // Determine which view to load
        String viewName = "";

        if (clickedButton == tournoiBtn) {
            viewName = "tournoi";
        } else if (clickedButton == equipeBtn) {
            viewName = "equipe";
        } else if (clickedButton == utilisateurBtn) {
            viewName = "utilisateur";
        } else if (clickedButton == userAdminBtn) {
            viewName = "userAdmin";
        } else if (clickedButton == recompenseBtn) {
            viewName = "recompense";
        } else if (clickedButton == sponsorBtn) {
            viewName = "sponsor";
        } else if (clickedButton == adminReponseBtn) {
            viewName = "AdminReponseView";
        } else if (clickedButton == adminViewBtn) {
            viewName = "AdminView";
        } else if (clickedButton == smartMatchmakingBtn) {
            viewName = "SmartMatchmaking";
        } else if (clickedButton == gestionMatchBtn) {
            viewName = "GestionMatch";
        } else if (clickedButton == gestionAreneBtn) {
            viewName = "GestionArene";
        }

        loadView(viewName);
    }

    private void loadView(String viewName) throws IOException {
        System.out.println("Loading view: " + viewName);

        // Check if the view is already cached
        if (cachedViews.containsKey(viewName)) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cachedViews.get(viewName));
            return;
        }

        // Try multiple possible paths for the FXML file
        String[] possiblePaths = {
                "/views/" + viewName + "View.fxml",
                "/fxml/" + viewName + "View.fxml",
                "/" + viewName + "View.fxml",
                "/views/" + viewName + ".fxml",
                "/fxml/" + viewName + ".fxml",
                "/" + viewName + ".fxml",
                "/views/AdminReponseView.fxml",
                "/views/AdminView.fxml",
                "/views/SmartMatchmaking.fxml",
                "/views/GestionMatch.fxml",
                "/views/GestionArene.fxml"
        };

        FXMLLoader loader = null;
        Parent view = null;

        for (String path : possiblePaths) {
            try {
                System.out.println("Trying to load FXML from: " + path);
                loader = new FXMLLoader(getClass().getResource(path));
                view = loader.load();
                System.out.println("Successfully loaded FXML from: " + path);


                break;
            } catch (Exception e) {
                // Continue to next path
                System.out.println("Failed to load from " + path + ": " + e.getMessage());
            }
        }

        if (view == null) {
            String fxmlPath = null; // Declare fxmlPath here
            // Fallback for specific view names if the generic paths didn't work
            if (viewName.equalsIgnoreCase("AdminReponseView")) {
                fxmlPath = "/views/AdminReponseView.fxml";
            } else if (viewName.equalsIgnoreCase("AdminView")) {
                fxmlPath = "/views/AdminView.fxml";
            } else if (viewName.equalsIgnoreCase("SmartMatchmaking")) {
                fxmlPath = "/views/SmartMatchmaking.fxml";
            } else if (viewName.equalsIgnoreCase("GestionMatch")) {
                fxmlPath = "/views/GestionMatch.fxml";
            } else if (viewName.equalsIgnoreCase("GestionArene")) {
                fxmlPath = "/views/GestionArene.fxml";
            } else {
                 throw new IOException("Could not find FXML file for view: " + viewName + " after trying multiple paths.");
            }
            try {
                System.out.println("Trying to load FXML from specific path: " + fxmlPath);
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
                view = loader.load();
                System.out.println("Successfully loaded FXML from specific path: " + fxmlPath);
            } catch (Exception e) {
                 throw new IOException("Could not find FXML file for view: " + viewName + " even with specific path " + fxmlPath + ". Error: " + e.getMessage());
            }
        }

        if (view == null) { // Double check after specific path attempt
            throw new IOException("Could not find FXML file for view: " + viewName);
        }

        // Cache the view for future use
        cachedViews.put(viewName, view);

        // Display the view
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    @FXML
    void openProfileView() {
        try {
            // Load the profile view
            loadView("profile");
            
            // Update the active button styling
            if (currentActiveButton != null) {
                currentActiveButton.getStyleClass().remove("active-nav-button");
            }
            // No button to set as active since profile is not in the main navigation
        } catch (IOException e) {
            System.err.println("Error loading profile view: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load profile");
            alert.setContentText("An error occurred while trying to load your profile. Please try again later.");
            alert.showAndWait();
        }
    }
    
    @FXML
    void logout(ActionEvent event) throws IOException {
        // Create confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("Any unsaved changes will be lost.");

        // Customize buttons
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            // Get current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Clean user session
            UserSession session = UserSession.getInstance();
            session.cleanUserSession();

            // Load login screen
            SceneController.loadPage("/AuthenticationView.fxml");

            // Alternative approach if SceneController doesn't work:
            /*
            Parent root = FXMLLoader.load(getClass().getResource("/AuthenticationView.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.show();

            // Close current stage
            currentStage.close();
            */
        }
    }
}


