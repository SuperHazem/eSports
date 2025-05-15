package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import utils.WindowManager;

public class UserInterfaceController {

    @FXML
    private AnchorPane calendarTabContainer, liveScoreTabContainer;

    @FXML
    public void initialize() {
        try {
            // Load the FXML files as Parent objects instead of specific layout types
            Parent calendar = FXMLLoader.load(getClass().getResource("/user/CalendarTab.fxml"));
            Parent liveScore = FXMLLoader.load(getClass().getResource("/user/LiveScoreTab.fxml"));

            // Add the loaded layouts to the containers
            calendarTabContainer.getChildren().add(calendar);
            liveScoreTabContainer.getChildren().add(liveScore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetourner() {
        WindowManager.getInstance().goBack();
    }
    
    @FXML
    private void handleOpenSmartMatchmaking() {
        try {
            WindowManager.getInstance().openWindow("/SmartMatchmaking.fxml", "Smart Matchmaking", false);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de Smart Matchmaking: " + e.getMessage());
            e.printStackTrace();
        }
    }
}