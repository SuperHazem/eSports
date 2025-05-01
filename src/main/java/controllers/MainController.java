package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.Utilisateur;
import utils.IconGenerator;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    private Button tournoiBtn;
    @FXML
    private Button equipeBtn;
    @FXML
    private Button utilisateurBtn;
    @FXML
    private Button recompenseBtn;
    @FXML
    private Button sponsorBtn;

    private Map<String, Parent> cachedViews = new HashMap<>();
    private Button currentActiveButton;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Style the logo
        styleLogoImage();

        // Set the initial active button
        currentActiveButton = utilisateurBtn;

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
     * Set the current user and update UI accordingly
     */
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            userRoleLabel.setText(user.getRole().toString());
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
        } else if (clickedButton == recompenseBtn) {
            viewName = "recompense";
        } else if (clickedButton == sponsorBtn) {
            viewName = "sponsor";
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
                "/" + viewName + ".fxml"
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
            throw new IOException("Could not find FXML file for view: " + viewName);
        }

        // Cache the view for future use
        cachedViews.put(viewName, view);

        // Display the view
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }
}


