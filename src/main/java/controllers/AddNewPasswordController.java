package controllers;

import utils.InputValidation;
import utils.PasswordHasher;
import dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import utils.SceneController;

import java.io.IOException;
import java.sql.SQLException;

public class AddNewPasswordController {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    private PasswordField cnewpass;

    @FXML
    private PasswordField newpass;

    public AddNewPasswordController() throws SQLException {
    }

    @FXML
    void backtologinpage(MouseEvent event) throws IOException {
        SceneController.loadPage("/AuthenticationView.fxml");
    }

    @FXML
    void passrecovery(MouseEvent event) throws IOException {
        if (InputValidation.validateNewPassword(newpass.getText(), cnewpass.getText())) {
            // Hash the password before storing it
            String hashedPassword = PasswordHasher.hashPassword(newpass.getText());

            // Update the password in the database with the hashed version
            utilisateurDAO.updatePasswordByEmail(ForgotPasswordController.userEmail, hashedPassword);

            System.out.println(ForgotPasswordController.userEmail);
            System.out.println("Password recovered");

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Your password has been successfully updated.");
            alert.showAndWait();

            SceneController.loadPage("/AuthenticationView.fxml");
        } else {
            InputValidation.showAlert("Error", "Failed to update password. Passwords must match and be at least 8 characters long.", Alert.AlertType.ERROR);
        }
    }
}