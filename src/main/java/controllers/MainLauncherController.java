package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.WindowManager;

public class MainLauncherController {

    @FXML
    private void handleOpenAdmin(ActionEvent event) {
        openWindow("/Launcher.fxml", "Admin Interface");
    }

    @FXML
    private void handleOpenUser(ActionEvent event) {
        openWindow("/user/UserInterface.fxml", "User Interface");
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            // Open the new window using WindowManager
            WindowManager.getInstance().openWindow(fxmlPath, title, false);

        } catch (Exception e) {
            System.err.println("Error opening window: " + e.getMessage());
            e.printStackTrace();
        }
    }
}