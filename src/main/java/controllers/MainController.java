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
import javafx.event.ActionEvent;

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
    @FXML
    private Button eventSocialBtn;
    @FXML
    private Button publicationBtn;

    private Map<String, Parent> cachedViews = new HashMap<>();
    private Button currentActiveButton;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Style the logo
        styleLogoImage();

        // Set the initial active button
        currentActiveButton = sponsorBtn;
        currentActiveButton.getStyleClass().add("active-nav-button");

        // Load the default view (Utilisateurs)
        try {
            loadView("Sponsor");
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
            glow.setColor(Color.CYAN);
            glow.setRadius(10);
            glow.setSpread(0.5);
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
            userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            userRoleLabel.setText(user.getRole().toString());
        }
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String viewName = clickedButton.getId().replace("Btn", "").toLowerCase();

        // Update active button styling
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-nav-button");
        }
        clickedButton.getStyleClass().add("active-nav-button");
        currentActiveButton = clickedButton;

        try {
            loadView(viewName);
        } catch (IOException e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String viewName) throws IOException {
        // Check if view is already cached
        if (cachedViews.containsKey(viewName)) {
            contentArea.getChildren().setAll(cachedViews.get(viewName));
            return;
        }

        // Load the view
        String viewPath = "/" + viewName.substring(0, 1).toUpperCase() + viewName.substring(1) + "View.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
        Parent view = loader.load();

        // Cache the view
        cachedViews.put(viewName, view);

        // Set the view in the content area
        contentArea.getChildren().setAll(view);
    }
}


