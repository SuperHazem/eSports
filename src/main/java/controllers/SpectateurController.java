package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class SpectateurController implements Initializable {

    @FXML
    private BorderPane spectateurBorderPane;
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
    private ImageView userAvatarImage;

    @FXML
    private Button calendarBtn;
    @FXML
    private Button liveScoreBtn;
    @FXML
    private Button ticketsBtn;
    @FXML
    private Button reclamationsBtn;

    private Map<String, Parent> cachedViews = new HashMap<>();
    private Button currentActiveButton;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        styleLogoImage();
        styleUserAvatar();

        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            setCurrentUser(session.getCurrentUser());
        }

        // Set initial active button and load default view
        currentActiveButton = calendarBtn; // Default view changed to calendar
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().add("active-nav-button");
            try {
                loadView("calendaTab"); // Default view name changed to calendaTab
            } catch (IOException e) {
                System.err.println("Error loading default view for Spectateur: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
             System.err.println("Default button (calendarBtn) is null.");
        }
    }

    private void styleLogoImage() {
        if (logoImageView != null) {
            // Attempt to load the image if not already set by FXML
            if (logoImageView.getImage() == null) {
                 try {
                    Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
                    logoImageView.setImage(logo);
                } catch (Exception e) {
                    System.err.println("Error loading logo image for Spectateur sidebar: " + e.getMessage());
                }
            }
            if (logoImageView.getImage() != null) {
                double radius = Math.min(logoImageView.getFitWidth(), logoImageView.getFitHeight()) / 2;
                if (radius <= 0) radius = 50; // Default radius if fitWidth/Height are 0
                Circle clip = new Circle(radius, radius, radius);
                logoImageView.setClip(clip);

                DropShadow glow = new DropShadow();
                glow.setColor(Color.rgb(0, 247, 255, 0.7));
                glow.setRadius(15);
                logoImageView.setEffect(glow);

                logoImageView.setPreserveRatio(true);
                logoImageView.setSmooth(true);
                logoImageView.setCache(true);
            }
        }
    }

    private void styleUserAvatar() {
        if (userAvatarImage != null) {
            double radius = Math.min(userAvatarImage.getFitWidth(), userAvatarImage.getFitHeight()) / 2;
            if (radius <= 0) radius = 20; // Default radius
            Circle clip = new Circle(radius, radius, radius);
            userAvatarImage.setClip(clip);
            userAvatarImage.setPreserveRatio(true);
            userAvatarImage.setSmooth(true);
            userAvatarImage.setCache(true);
        }
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userRoleLabel.setText(user.getRole().toString());

            boolean isGoogleUser = user.getMotDePasseHash() != null && user.getMotDePasseHash().equals("google-oauth");

            if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                try {
                    File imageFile = new File(user.getProfilePicturePath());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        userAvatarImage.setImage(image);
                    } else {
                        generateProfileIcon(user);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image in Spectateur sidebar: " + e.getMessage());
                    generateProfileIcon(user);
                }
            } else {
                generateProfileIcon(user);
            }
        }
    }

    private void generateProfileIcon(Utilisateur user) {
        try {
            String initials = String.valueOf(user.getPrenom().charAt(0)) +
                             (user.getNom() != null && !user.getNom().isEmpty() ?
                              String.valueOf(user.getNom().charAt(0)) : "");
            String fullName = user.getPrenom() + user.getNom();
            int hash = fullName.hashCode();
            Color avatarColor = Color.hsb((hash % 360), 0.8, 0.9);
            Image generatedIcon = IconGenerator.createTextIcon(initials.toUpperCase(), avatarColor);
            userAvatarImage.setImage(generatedIcon);
        } catch (Exception e) {
            System.err.println("Error generating profile icon for Spectateur: " + e.getMessage());
        }
    }

    @FXML
    private void handleNavigation(ActionEvent event) throws IOException {
        Button clickedButton = (Button) event.getSource();

        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-nav-button");
        }
        clickedButton.getStyleClass().add("active-nav-button");
        currentActiveButton = clickedButton;

        String viewName = "";
        if (clickedButton == calendarBtn) {
            viewName = "calendaTab"; // Corrected to match FXML file name
        } else if (clickedButton == liveScoreBtn) {
            viewName = "LiveScoreTab"; // Corrected to match FXML file name
        } else if (clickedButton == ticketsBtn) {
            viewName = "ticketView";
        } else if (clickedButton == reclamationsBtn) {
            viewName = "ReclamationView";
        }

        if (!viewName.isEmpty()) {
            loadView(viewName);
        }
    }

    private void loadView(String viewName) throws IOException {
        if (cachedViews.containsKey(viewName)) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cachedViews.get(viewName));
            return;
        }

        String fxmlPath = "";
        // Adjust paths based on actual FXML file locations and naming conventions
        if (viewName.equalsIgnoreCase("calendaTab")) {
            fxmlPath = "/user/CalendarTab.fxml"; // Path from UserInterfaceController
        } else if (viewName.equalsIgnoreCase("LiveScoreTab")) {
            fxmlPath = "/user/LiveScoreTab.fxml"; // Path from UserInterfaceController
        } else if (viewName.equalsIgnoreCase("ticketView")) {
            fxmlPath = "/TicketView.fxml";
        } else if (viewName.equalsIgnoreCase("ReclamationView")) {
            fxmlPath = "/ReclamationView.fxml";
        } else if (viewName.equalsIgnoreCase("profile")) {
            fxmlPath = "/profile.fxml";
        } else {
            // Fallback for general views if specific paths are not matched
            fxmlPath = "/views/" + viewName + ".fxml";
        }
        
        System.out.println("SpectateurController: Attempting to load FXML from: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();

        if (view == null) {
            throw new IOException("Could not find FXML file for view: " + viewName + " at path: " + fxmlPath);
        }

        cachedViews.put(viewName, view);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    @FXML
    void openProfileView() {
        try {
            loadView("profile");
            if (currentActiveButton != null) {
                currentActiveButton.getStyleClass().remove("active-nav-button");
            }
            // No button to set as active for profile view in this layout
        } catch (IOException e) {
            System.err.println("Error loading profile view from Spectateur layout: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load profile");
            alert.setContentText("An error occurred while trying to load your profile.");
            alert.showAndWait();
        }
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be returned to the login screen.");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            UserSession.getInstance().cleanUserSession();
            // Assuming SceneController.loadPage can handle this path
            SceneController.loadPage("/AuthenticationView.fxml"); 
        }
    }
}