package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import models.Utilisateur;
import utils.UserSession;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    // Profile Header
    @FXML private ImageView profileImageView;
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileEmailLabel;
    
    // Common Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextArea addressField;
    
    // Role-specific Fields Containers
    @FXML private VBox adminSpecificFields;
    @FXML private VBox coachSpecificFields;
    @FXML private VBox playerSpecificFields;
    @FXML private VBox spectatorSpecificFields;
    
    // Admin-specific Fields
    @FXML private ComboBox<String> adminAccessLevelCombo;
    @FXML private TextField adminDepartmentField;
    
    // Coach-specific Fields
    @FXML private TextField coachSpecialtyField;
    @FXML private TextField coachExperienceField;
    @FXML private ComboBox<String> coachTeamCombo;
    
    // Player-specific Fields
    @FXML private TextField playerNicknameField;
    @FXML private TextField playerMainGameField;
    @FXML private ComboBox<String> playerTeamCombo;
    
    // Spectator-specific Fields
    @FXML private TextField spectatorFavoriteGamesField;
    @FXML private ComboBox<String> spectatorSubscriptionCombo;
    
    // Security Tab Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Preferences Tab Fields
    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private CheckBox notificationsCheckbox;
    
    // Action Buttons
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    private Utilisateur currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from session
        UserSession session = UserSession.getInstance();
        currentUser = session.getCurrentUser();
        
        if (currentUser == null) {
            System.err.println("Error: No user found in session");
            return;
        }
        
        // Initialize combo boxes
        initializeComboBoxes();
        
        // Load user data
        loadUserData();
        
        // Show fields specific to user role
        showRoleSpecificFields();
    }
    
    private void initializeComboBoxes() {
        // Admin access levels
        adminAccessLevelCombo.setItems(FXCollections.observableArrayList(
                "Niveau 1 - Standard", 
                "Niveau 2 - Avancé", 
                "Niveau 3 - Complet"));
        
        // Teams for coaches and players
        coachTeamCombo.setItems(FXCollections.observableArrayList(
                "Team Alpha", "Team Beta", "Team Gamma", "Team Delta"));
        playerTeamCombo.setItems(FXCollections.observableArrayList(
                "Team Alpha", "Team Beta", "Team Gamma", "Team Delta"));
        
        // Subscription types for spectators
        spectatorSubscriptionCombo.setItems(FXCollections.observableArrayList(
                "Gratuit", "Premium", "VIP"));
        
        // Themes
        themeCombo.setItems(FXCollections.observableArrayList(
                "Sombre", "Clair", "Système"));
        themeCombo.setValue("Sombre");
        
        // Languages
        languageCombo.setItems(FXCollections.observableArrayList(
                "Français", "English", "Español"));
        languageCombo.setValue("Français");
    }
    
    private void loadUserData() {
        // Set header information
        profileNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        profileEmailLabel.setText(currentUser.getEmail());
        
        // Set role badge style and text
        String role = currentUser.getRole().toString();
        profileRoleLabel.setText(role);
        
        switch (role.toLowerCase()) {
            case "admin":
                profileRoleLabel.getStyleClass().add("role-admin");
                break;
            case "coach":
                profileRoleLabel.getStyleClass().add("role-coach");
                break;
            case "joueur":
                profileRoleLabel.getStyleClass().add("role-joueur");
                break;
            case "spectateur":
                profileRoleLabel.getStyleClass().add("role-spectateur");
                break;
        }
        
        // Set common fields
        firstNameField.setText(currentUser.getPrenom());
        lastNameField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getTelephone());
        
        // Set birth date if available
        if (currentUser.getDateNaissance() != null) {
            birthDatePicker.setValue(LocalDate.parse(currentUser.getDateNaissance().toString()));
        }
        
        // Set address if available
        if (currentUser.getAdresse() != null) {
            addressField.setText(currentUser.getAdresse());
        }
        
        // Role-specific data would be loaded here in a real application
        // This is just placeholder data for demonstration
        switch (role.toLowerCase()) {
            case "admin":
                adminAccessLevelCombo.setValue("Niveau 3 - Complet");
                adminDepartmentField.setText("Direction Générale");
                break;
            case "coach":
                coachSpecialtyField.setText("League of Legends");
                coachExperienceField.setText("5");
                coachTeamCombo.setValue("Team Alpha");
                break;
            case "joueur":
                playerNicknameField.setText("Pro_Gamer_" + currentUser.getPrenom());
                playerMainGameField.setText("Counter-Strike");
                playerTeamCombo.setValue("Team Beta");
                break;
            case "spectateur":
                spectatorFavoriteGamesField.setText("League of Legends, Valorant, CS:GO");
                spectatorSubscriptionCombo.setValue("Premium");
                break;
        }
    }
    
    private void showRoleSpecificFields() {
        // Hide all role-specific fields first
        adminSpecificFields.setVisible(false);
        adminSpecificFields.setManaged(false);
        coachSpecificFields.setVisible(false);
        coachSpecificFields.setManaged(false);
        playerSpecificFields.setVisible(false);
        playerSpecificFields.setManaged(false);
        spectatorSpecificFields.setVisible(false);
        spectatorSpecificFields.setManaged(false);
        
        // Show fields based on user role
        String role = currentUser.getRole().toString().toLowerCase();
        switch (role) {
            case "admin":
                adminSpecificFields.setVisible(true);
                adminSpecificFields.setManaged(true);
                break;
            case "coach":
                coachSpecificFields.setVisible(true);
                coachSpecificFields.setManaged(true);
                break;
            case "joueur":
                playerSpecificFields.setVisible(true);
                playerSpecificFields.setManaged(true);
                break;
            case "spectateur":
                spectatorSpecificFields.setVisible(true);
                spectatorSpecificFields.setManaged(true);
                break;
        }
    }
    
    @FXML
    private void handleSave() {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }
        
        // Update user data
        currentUser.setPrenom(firstNameField.getText());
        currentUser.setNom(lastNameField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setTelephone(phoneField.getText());
        
        if (birthDatePicker.getValue() != null) {
            currentUser.setDateNaissance(Date.valueOf(birthDatePicker.getValue()).toLocalDate());
        }
        
        currentUser.setAdresse(addressField.getText());
        
        // Handle password change if needed
        if (!currentPasswordField.getText().isEmpty()) {
            if (validatePasswordChange()) {
                // In a real app, you would hash the password and update it in the database
                System.out.println("Password would be updated here");
            }
        }
        
        // Save role-specific data
        saveRoleSpecificData();
        
        // In a real application, you would save to database here
        // For now, just show a success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profil Mis à Jour");
        alert.setHeaderText("Vos informations ont été mises à jour avec succès");
        alert.showAndWait();
        
        // Update the profile header
        profileNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        profileEmailLabel.setText(currentUser.getEmail());
    }
    
    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (firstNameField.getText().isEmpty()) {
            errorMessage.append("Le prénom ne peut pas être vide.\n");
        }
        
        if (lastNameField.getText().isEmpty()) {
            errorMessage.append("Le nom ne peut pas être vide.\n");
        }
        
        if (emailField.getText().isEmpty()) {
            errorMessage.append("L'email ne peut pas être vide.\n");
        } else if (!emailField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errorMessage.append("Format d'email invalide.\n");
        }
        
        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private boolean validatePasswordChange() {
        // In a real app, you would verify the current password against the stored hash
        if (currentPasswordField.getText().isEmpty()) {
            showError("Veuillez entrer votre mot de passe actuel.");
            return false;
        }
        
        if (newPasswordField.getText().isEmpty()) {
            showError("Le nouveau mot de passe ne peut pas être vide.");
            return false;
        }
        
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            return false;
        }
        
        // Check password strength
        if (newPasswordField.getText().length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères.");
            return false;
        }
        
        return true;
    }
    
    private void saveRoleSpecificData() {
        String role = currentUser.getRole().toString().toLowerCase();
        
        switch (role) {
            case "admin":
                // Save admin-specific data
                System.out.println("Admin access level: " + adminAccessLevelCombo.getValue());
                System.out.println("Admin department: " + adminDepartmentField.getText());
                break;
            case "coach":
                // Save coach-specific data
                System.out.println("Coach specialty: " + coachSpecialtyField.getText());
                System.out.println("Coach experience: " + coachExperienceField.getText());
                System.out.println("Coach team: " + coachTeamCombo.getValue());
                break;
            case "joueur":
                // Save player-specific data
                System.out.println("Player nickname: " + playerNicknameField.getText());
                System.out.println("Player main game: " + playerMainGameField.getText());
                System.out.println("Player team: " + playerTeamCombo.getValue());
                break;
            case "spectateur":
                // Save spectator-specific data
                System.out.println("Spectator favorite games: " + spectatorFavoriteGamesField.getText());
                System.out.println("Spectator subscription: " + spectatorSubscriptionCombo.getValue());
                break;
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleCancel() {
        // Reload user data to discard changes
        loadUserData();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Modifications Annulées");
        alert.setHeaderText("Vos modifications ont été annulées");
        alert.showAndWait();
    }
}