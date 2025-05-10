package controllers;

import models.Utilisateur;
import utils.InputValidation;
import dao.UtilisateurDAO;
import utils.EmailSender;
import utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import utils.SceneController;

import java.io.IOException;
import java.sql.SQLException;

public class VerifyEmail {
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // Static fields to store verification data
    public static String sentCode;
    public static Utilisateur pendingUser;
    public static String verificationMode = "REGISTER"; // REGISTER or RESET_PASSWORD

    @FXML
    private TextField code;

    @FXML
    private Label instructionLabel;

    @FXML
    private Label emailLabel;

    public VerifyEmail() throws SQLException {
    }

    @FXML
    public void initialize() {
        // Set the email label if we have a pending user
        if (pendingUser != null) {
            emailLabel.setText("Code sent to: " + pendingUser.getEmail());
        } else {
            // Handle case where page is loaded without a pending user
            emailLabel.setText("No email address available");
            InputValidation.showAlert("Error",
                    "No pending registration found. Please start the registration process again.",
                    Alert.AlertType.ERROR);
        }

        // Set appropriate instruction based on verification mode
        if ("RESET_PASSWORD".equals(verificationMode)) {
            instructionLabel.setText("Please enter the verification code to reset your password");
        } else {
            instructionLabel.setText("Please enter the verification code to complete registration");
        }
    }

    @FXML
    void backToRegisterPage(MouseEvent event) throws IOException {
        if ("RESET_PASSWORD".equals(verificationMode)) {
            SceneController.loadPage("/ForgotPasswordView.fxml");
        } else {
            SceneController.loadPage("/AuthenticationView.fxml");
        }
    }

    @FXML
    void resendCode(MouseEvent event) {
        if (pendingUser != null) {
            sendVerificationCode(pendingUser.getEmail());
            InputValidation.showAlert("Code Sent",
                    "A new verification code has been sent to your email.",
                    Alert.AlertType.INFORMATION);
        } else {
            InputValidation.showAlert("Error",
                    "Unable to resend code. Please go back and try again.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    void verifyAndCreateAccount(MouseEvent event) throws SQLException, IOException {
        if (code.getText().isEmpty()) {
            InputValidation.showAlert("Error",
                    "Please enter the verification code sent to your email.",
                    Alert.AlertType.ERROR);
            return;
        }

        if (sentCode != null && sentCode.equals(code.getText())) {
            if ("RESET_PASSWORD".equals(verificationMode)) {
                // For password reset flow
                SceneController.loadPage("/AddNewPasswordView.fxml");
            } else {
                // For registration flow
                if (pendingUser != null) {
                    // Hash the password before storing
                    String hashedPassword = utils.PasswordHasher.hashPassword(pendingUser.getMotDePasseHash());
                    pendingUser.setMotDePasseHash(hashedPassword);

                    // Insert the new user into the database
                    utilisateurDAO.ajouter(pendingUser);

                    // Set the user in the session
                    UserSession.getInstance().setUser(pendingUser);

                    System.out.println("User registered successfully");
                    System.out.println("User ID: " + UserSession.getInstance().getUser().getId());

                    // Show success message
                    InputValidation.showAlert("Success", "Registration completed successfully!", Alert.AlertType.INFORMATION);

                    // Navigate to the main page
                    SceneController.loadPage("/MainLayout.fxml");
                } else {
                    InputValidation.showAlert("Error", "User data not found. Please try registering again.", Alert.AlertType.ERROR);
                }
            }
        } else {
            InputValidation.showAlert("Invalid Code",
                    "The verification code is invalid. Please ensure the code is correct and try again.",
                    Alert.AlertType.ERROR);
        }
    }

    // Method to send verification code
    public static void sendVerificationCode(String email) {
        // Generate a random 6-digit code
        sentCode = String.format("%06d", (int)(Math.random() * 1000000));

        // Send the code via email
        String subject = "Email Verification";
        String body = "Your verification code is: " + sentCode + "\n\n" +
                "Please enter this code in the application to complete your " +
                (verificationMode.equals("RESET_PASSWORD") ? "password reset." : "registration.");

        EmailSender.sendEmail(email, subject, body);
        System.out.println("Verification code sent to: " + email);
    }

    // Static method to prepare for verification
    public static void prepareForVerification(Utilisateur user, String mode) {
        pendingUser = user;
        verificationMode = mode;
        sendVerificationCode(user.getEmail());
    }

    // Method to check if there's a pending verification
    public static boolean hasPendingVerification() {
        return pendingUser != null && sentCode != null;
    }

    // Method to clear verification data
    public static void clearVerificationData() {
        pendingUser = null;
        sentCode = null;
        verificationMode = "REGISTER";
    }
}