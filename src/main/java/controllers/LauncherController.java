package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import utils.WindowManager;

public class LauncherController {

    @FXML
    private void handleOpenArene(ActionEvent event) {
        openWindow("/GestionArenes.fxml", "Gestion des Arènes");
    }

    @FXML
    private void handleOpenMatch(ActionEvent event) {
        openWindow("/GestionMatch.fxml", "Gestion des Matchs");
    }
    
    @FXML
    private void handleOpenSmartMatchmaking(ActionEvent event) {
        openWindow("/SmartMatchmaking.fxml", "Smart Matchmaking");
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            // ✅ FIX: Now it remembers this window so "Retourner" will work from inside
            WindowManager.getInstance().openWindow(fxmlPath, title, true);
        } catch (Exception e) {
            System.err.println("Error opening window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRetourner() {
        WindowManager.getInstance().goBack();
    }

}