package controllers;

import dao.UtilisateurDAO;
import enums.Role;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.*;
import services.GoogleAuthService;
import utils.PasswordHasher;
import utils.SceneController;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AuthenticationController {

    @FXML private ImageView logoImage;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private TextField registerFirstName;
    @FXML private TextField registerLastName;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField registerConfirmPassword;
    @FXML private ComboBox<String> registerRole;
    @FXML private Button googleLoginButton;

    private UtilisateurDAO utilisateurDAO;
    private Utilisateur authenticatedUser;
    private GoogleAuthService googleAuthService;
    private Connection dbConnection;

    @FXML
    public void initialize() {
        try {
            // Initialize DAO
            utilisateurDAO = new UtilisateurDAO();

            // Get the database connection from the DAO
            try {
                // Get the connection from the DAO
                dbConnection = utilisateurDAO.getConnection();

                // Initialize Google Auth Service with the connection
                googleAuthService = new GoogleAuthService(dbConnection);

                // Enable Google login button if service initialized successfully
                if (googleLoginButton != null) {
                    googleLoginButton.setDisable(false);
                }
            } catch (Exception e) {
                System.err.println("Failed to initialize Google Auth Service: " + e.getMessage());
                e.printStackTrace();
                // Disable Google login button if service failed to initialize
                if (googleLoginButton != null) {
                    googleLoginButton.setDisable(true);
                    googleLoginButton.setTooltip(new Tooltip("Google authentication service unavailable"));
                }
            }

            // Load and style the logo
            loadAndStyleLogo();

            // Initialize the role combo box
            initializeRoleComboBox();

            // Set initial form visibility
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            registerForm.setVisible(false);
            registerForm.setManaged(false);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Une erreur est survenue lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method to load and style the logo image
     */
    private void loadAndStyleLogo() {
        try {
            // Load the logo image
            InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                // Set the image to the ImageView
                logoImage.setImage(new Image(logoStream));

                // Apply styling to the logo
                styleLogoImage();
            } else {
                System.err.println("Logo image resource not found, creating fallback logo");
                createFallbackLogo();
            }
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            e.printStackTrace();
            createFallbackLogo();
        }
    }

    /**
     * Apply styling to make the logo circular with glow effects
     */
    private void styleLogoImage() {
        if (logoImage != null && logoImage.getImage() != null) {
            // Create a circle clip for the logo
            double radius = Math.min(logoImage.getFitWidth(), logoImage.getFitHeight()) / 2;
            Circle clip = new Circle(radius, radius, radius);
            logoImage.setClip(clip);

            // Add a glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.rgb(0, 247, 255, 0.7));
            glow.setRadius(15);
            logoImage.setEffect(glow);

            // Center the logo
            logoImage.setPreserveRatio(true);
            logoImage.setSmooth(true);
            logoImage.setCache(true);
        }
    }

    /**
     * Create a fallback logo if the image file cannot be loaded
     */
    private void createFallbackLogo() {
        // Create a canvas for drawing a simple logo
        Canvas canvas = new Canvas(100, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Draw a circular background
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillOval(0, 0, 100, 100);

        // Draw a border
        gc.setStroke(Color.rgb(0, 247, 255));
        gc.setLineWidth(3);
        gc.strokeOval(3, 3, 94, 94);

        // Draw text or symbol
        gc.setFill(Color.rgb(0, 247, 255));
        gc.setFont(new Font("Arial Bold", 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("EA", 50, 50);

        // Convert canvas to image
        WritableImage image = new WritableImage(100, 100);
        canvas.snapshot(null, image);

        // Set the image to the ImageView
        logoImage.setImage(image);

        // Apply styling
        styleLogoImage();
    }

    @FXML
    public void initializeRoleComboBox() {
        if (registerRole != null) {
            registerRole.getItems().clear();
            registerRole.getItems().addAll("JOUEUR", "COACH", "SPECTATEUR");
            // Set SPECTATEUR as the default selected role
            registerRole.setValue("SPECTATEUR");
        }
    }

    @FXML
    public void showLoginForm() {
        if (loginForm != null && registerForm != null) {
            fadeTransition(registerForm, loginForm);
        }
    }

    @FXML
    public void showRegisterForm() {
        if (loginForm != null && registerForm != null) {
            fadeTransition(loginForm, registerForm);
            // Initialize the combo box when showing the register form
            Platform.runLater(this::initializeRoleComboBox);
        }
    }

    @FXML
    public void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Champs requis", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Query the database for all users
            List<Utilisateur> allUsers = utilisateurDAO.lireTous();

            // Find the user with matching email
            Utilisateur user = null;
            for (Utilisateur u : allUsers) {
                if (u.getEmail().equals(email)) {
                    user = u;
                    break;
                }
            }

            // Check if user exists
            if (user == null) {
                showError("Échec de connexion", "Email ou mot de passe incorrect.");
                return;
            }

            // For debugging - print the stored hash and the input password
            System.out.println("Stored hash: " + user.getMotDePasseHash());
            System.out.println("Input password: " + password);

            // TEMPORARY SOLUTION: Direct comparison for debugging
            // This will help us understand if the issue is with hashing or something else
            if (password.equals(user.getMotDePasseHash())) {
                // Authentication successful with direct comparison
                authenticatedUser = user;
                showInfo("Connexion réussie", "Bienvenue dans l'application eSports Arena Manager!");
                loadMainApplication();
                return;
            }

            // Try to verify with proper hashing
            boolean passwordMatches = false;
            try {
                passwordMatches = PasswordHasher.verifyPassword(password, user.getMotDePasseHash());
                System.out.println("Password verification result: " + passwordMatches);
            } catch (Exception e) {
                System.out.println("Error in password verification: " + e.getMessage());
                e.printStackTrace();
            }

            if (passwordMatches) {
                // Authentication successful
                authenticatedUser = user;
                showInfo("Connexion réussie", "Bienvenue dans l'application eSports Arena Manager!");
                loadMainApplication();
            } else {
                // Authentication failed
                showError("Échec de connexion", "Email ou mot de passe incorrect.");
            }

        } catch (Exception e) {
            showError("Erreur de connexion", "Une erreur est survenue lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");

        alert.showAndWait();
    }

    @FXML
    public void handleRegister() {
        try {
            String firstName = registerFirstName.getText().trim();
            String lastName = registerLastName.getText().trim();
            String email = registerEmail.getText().trim();
            String password = registerPassword.getText().trim();
            String confirmPassword = registerConfirmPassword.getText().trim();
            String roleString = registerRole.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty() || roleString == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'inscription",
                        "Veuillez remplir tous les champs.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'inscription",
                        "Les mots de passe ne correspondent pas.");
                return;
            }

            // Create user based on role
            Role role = Role.valueOf(roleString);
            Utilisateur newUser;

            switch (role) {
                case JOUEUR:
                    // For simplicity, set default values for Joueur-specific fields
                    newUser = new Joueur(role, password, email, 0, lastName, firstName,
                            "Player" + System.currentTimeMillis(), 0.0, "Débutant");
                    break;

                case COACH:
                    // For simplicity, set default values for Coach-specific fields
                    newUser = new Coach(role, password, email, 0, lastName, firstName,
                            "Stratégie par défaut");
                    break;

                case SPECTATEUR:
                    // For simplicity, use current date for Spectateur-specific fields
                    newUser = new Spectateur(role, password, email, 0, lastName, firstName,
                            new Date());
                    break;

                default:
                    showAlert(Alert.AlertType.ERROR, "Erreur d'inscription",
                            "Rôle non supporté: " + roleString);
                    return;
            }

            // Prepare for email verification
            VerifyEmail.prepareForVerification(newUser, "REGISTER");

            // Navigate to the verification page
            SceneController.loadPage("/VerifyEmailView.fxml");

            // Clear registration form
            registerFirstName.clear();
            registerLastName.clear();
            registerEmail.clear();
            registerPassword.clear();
            registerConfirmPassword.clear();
            registerRole.getSelectionModel().clearSelection();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'inscription",
                    "Une erreur inattendue est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForgotPassword() {
        try {
            // Navigate to the forgot password screen
            SceneController.loadPage("/ForgotPasswordView.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            showAlert(Alert.AlertType.ERROR,"Error", "Failed to load password recovery page: " + e.getMessage());
        }
    }

    private void fadeTransition(VBox fromNode, VBox toNode) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), fromNode);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            fromNode.setVisible(false);
            fromNode.setManaged(false);
            toNode.setVisible(true);
            toNode.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toNode);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadMainApplication() {
        try {
            // Load the main layout with sidebar
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainLayout.fxml"));
            Parent root = loader.load();

            // Get the controller and set the authenticated user
            MainController mainController = loader.getController();
            mainController.setCurrentUser(authenticatedUser);

            // Get current stage
            Stage stage = (Stage) loginEmail.getScene().getWindow();

            // Create new scene
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

            // Set the scene
            stage.setScene(scene);
            stage.setTitle("eSports Arena Manager");
            stage.setMaximized(true);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger l'application principale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/authentication.css").toExternalForm());
        dialogPane.getStyleClass().add("alert-dialog");

        alert.showAndWait();
    }

    // Add this method to handle Google login
    @FXML
    public void handleGoogleLogin() {
        if (googleAuthService == null) {
            showAlert(Alert.AlertType.ERROR, "Service non disponible",
                    "Le service d'authentification Google n'est pas disponible.");
            return;
        }

        // Show more informative message about account selection
        showAlert(Alert.AlertType.INFORMATION, "Connexion avec Google",
                "Une fenêtre de navigateur va s'ouvrir pour vous permettre de sélectionner votre compte Google. " +
                        "Veuillez choisir le compte que vous souhaitez utiliser pour vous connecter à l'application.");

        // Disable the Google login button to prevent multiple clicks
        if (googleLoginButton != null) {
            googleLoginButton.setDisable(true);
        }

        try {
            googleAuthService.startGoogleAuthFlow(new GoogleAuthService.GoogleAuthCallback() {
                @Override
                public void onAuthCompleted(Utilisateur user) {
                    Platform.runLater(() -> {
                        try {
                            // Re-enable the Google login button
                            if (googleLoginButton != null) {
                                googleLoginButton.setDisable(false);
                            }

                            if (user != null) {
                                // Debug the user object
                                System.out.println("Google auth completed. User: " + user.getEmail() +
                                        ", Role: " + user.getRole() +
                                        " (name: " + user.getRole().name() + ")");

                                // Set the authenticated user
                                authenticatedUser = user;

                                // Close any open dialogs
                                closeAlerts();

                                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie",
                                        "Vous êtes maintenant connecté avec le compte: " + user.getEmail());

                                // Load main application
                                loadMainApplication();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Échec de connexion",
                                        "Impossible de récupérer les informations utilisateur.");
                            }
                        } catch (Exception e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                                    "Une erreur est survenue lors de la connexion: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onAuthFailed(String errorMessage) {
                    Platform.runLater(() -> {
                        // Re-enable the Google login button
                        if (googleLoginButton != null) {
                            googleLoginButton.setDisable(false);
                        }

                        if (errorMessage == null) {
                            showAlert(Alert.AlertType.ERROR, "Échec de connexion",
                                    "Erreur d'authentification Google: Une erreur inconnue s'est produite");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Échec de connexion",
                                    "Erreur d'authentification Google: " + errorMessage);
                        }
                    });
                }
            });
        } catch (Exception e) {
            // Re-enable the Google login button
            if (googleLoginButton != null) {
                googleLoginButton.setDisable(false);
            }

            showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                    "Une erreur est survenue lors de la connexion Google: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to close any open alerts
    private void closeAlerts() {
        Stage currentStage = (Stage) loginEmail.getScene().getWindow();
        for (javafx.stage.Window window : new ArrayList<>(Stage.getWindows())) {
            if (window instanceof Stage && window.isShowing() && window != currentStage) {
                ((Stage) window).close();
            }
        }
    }
}
