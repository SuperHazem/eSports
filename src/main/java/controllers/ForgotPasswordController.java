package controllers;

import utils.EmailSender;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import utils.SceneController;

import java.io.IOException;
import java.security.SecureRandom;

public class ForgotPasswordController {
    @FXML 
    private TextField email;

    static String userEmail = "";
    private String verificationCode;
    public static String code = generateSecureSixDigitCode();

    @FXML
    void backtologinpage(MouseEvent event) throws IOException {
        SceneController.loadPage("/AuthenticationView.fxml");
    }

    @FXML
    void passrecovery(MouseEvent event) {
        try {
            userEmail = email.getText().trim();

            if (userEmail.isEmpty()) {
                showAlert("Input Error", "Please enter your email address");
                return;
            }

            EmailSender.sendEmail(
                    userEmail,
                    "Password Reset Request - Verification Code",
                    "Your verification code is: " + code + 
                            "\n\nThis code will expire in 15 minutes."
            );

            System.out.println("Verification code sent to: " + userEmail);
            SceneController.loadPage("/ResetPasswordView.fxml");

        } catch (RuntimeException e) {
            showAlert("Email Error", "Failed to send verification email: " + e.getMessage());
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to load password reset screen");
        }
    }

    static String generateSecureSixDigitCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}