package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import utils.SceneController;

import java.io.IOException;

public class ResetPasswordController {

    @FXML
    private TextField code;

    @FXML
    void backtoresetpage(MouseEvent event) throws IOException {
        SceneController.loadPage("/ForgotPasswordView.fxml");
    }

    @FXML
    void newpasswordpage(MouseEvent event) throws IOException {
        System.out.println(ForgotPasswordController.code);
        if (code.getText().equals(ForgotPasswordController.code)) {
            System.out.println("New password");
            SceneController.loadPage("/AddNewPasswordView.fxml");
        } else {
            System.out.println("Incorrect code");
            showAlert("Verification Failed", "The code you entered is incorrect. Please try again.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}