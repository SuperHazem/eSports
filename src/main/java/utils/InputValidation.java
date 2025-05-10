package utils;

import javafx.scene.control.Alert;

public class InputValidation {

    public static boolean validateNewPassword(String confirmPassword, String newPassword) {
        // Check if passwords match
        if (!confirmPassword.equals(newPassword)) {
            showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
            return false;
        }

        // Check password length
        if (newPassword.length() < 8) {
            showAlert("Error", "Password must be at least 8 characters long", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    public static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}