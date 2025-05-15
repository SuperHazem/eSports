package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import models.Utilisateur;
import utils.IconGenerator;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;

public class ProfileController implements Initializable {

    // Profile Header
    @FXML private ImageView profileImageView;
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Button uploadPhotoButton;
    
    /**
     * Generate a profile icon for users without a profile picture
     * This is especially useful for Google-authenticated users
     */
    private void generateProfileIcon(Utilisateur user) {
        try {
            // Use the IconGenerator utility to create an avatar based on user's initials
            String initials = String.valueOf(user.getPrenom().charAt(0)) + 
                             (user.getNom() != null && !user.getNom().isEmpty() ? 
                              String.valueOf(user.getNom().charAt(0)) : "");
            
            // Generate a color based on the user's name for consistency
            String fullName = user.getPrenom() + user.getNom();
            int hash = fullName.hashCode();
            // Generate a bright, saturated color (avoid dark colors for visibility)
            Color avatarColor = Color.hsb(
                (hash % 360), // Hue: 0-359 degrees
                0.8,          // Saturation: 80%
                0.9           // Brightness: 90%
            );
            
            // Create the image and set it to the avatar
            Image generatedIcon = IconGenerator.createTextIcon(initials.toUpperCase(), avatarColor);
            profileImageView.setImage(generatedIcon);
            
            System.out.println("Profile view - Generated profile icon for user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Error generating profile icon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
        
        // Check if this is a Google-authenticated user
        boolean isGoogleUser = currentUser.getMotDePasseHash() != null && currentUser.getMotDePasseHash().equals("google-oauth");
        
        System.out.println("Profile view - User authentication type check: " + currentUser.getEmail() + ", isGoogleUser=" + isGoogleUser);
        
        // Load profile picture if available
        if (currentUser.getProfilePicturePath() != null && !currentUser.getProfilePicturePath().isEmpty()) {
            try {
                File imageFile = new File(currentUser.getProfilePicturePath());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profileImageView.setImage(image);
                    System.out.println("Profile view - Loaded profile picture from: " + currentUser.getProfilePicturePath());
                } else if (isGoogleUser) {
                    // For Google users, generate an icon if the profile picture file doesn't exist
                    System.out.println("Profile view - Profile picture file doesn't exist for Google user, generating icon");
                    generateProfileIcon(currentUser);
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
                if (isGoogleUser) {
                    // For Google users, generate an icon if there's an error loading the profile picture
                    System.out.println("Profile view - Error loading profile image for Google user, generating icon");
                    generateProfileIcon(currentUser);
                }
            }
        } else if (isGoogleUser) {
            // For Google users without a profile picture path, generate an icon
            System.out.println("Profile view - No profile picture path for Google user, generating icon");
            generateProfileIcon(currentUser);
        } else {
            // For non-Google users without a profile picture, also generate an icon
            System.out.println("Profile view - No profile picture for regular user, generating icon");
            generateProfileIcon(currentUser);
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
                // Hash the password and update it in the database
                // In a real app, you would use a proper hashing algorithm
                String hashedPassword = newPasswordField.getText(); // Replace with actual hashing
                currentUser.setMotDePasseHash(hashedPassword);
            }
        }
        
        // Save role-specific data
        saveRoleSpecificData();
        
        // Save to database using UtilisateurDAO
        try {
            dao.UtilisateurDAO utilisateurDAO = new dao.UtilisateurDAO();
            utilisateurDAO.modifier(currentUser);
            
            // Update the session user
            UserSession.getInstance().setCurrentUser(currentUser);
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profil Mis à Jour");
            alert.setHeaderText("Vos informations ont été mises à jour avec succès");
            alert.showAndWait();
            
            // Update the profile header
            profileNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            profileEmailLabel.setText(currentUser.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la mise à jour du profil: " + e.getMessage());
        }
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
    
    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Create directory for profile pictures if it doesn't exist
                String uploadDir = "src/main/resources/images/profiles/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // Generate unique filename to avoid conflicts
                String fileName = UUID.randomUUID().toString() + "-" + selectedFile.getName();
                Path targetPath = uploadPath.resolve(fileName);
                
                // Copy the file to the target location
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update the user's profile picture path
                String profilePicturePath = targetPath.toString();
                currentUser.setProfilePicturePath(profilePicturePath);
                
                // Update the image view
                Image image = new Image(targetPath.toUri().toString());
                profileImageView.setImage(image);
                
                // Save the changes to the database
                try {
                    dao.UtilisateurDAO utilisateurDAO = new dao.UtilisateurDAO();
                    utilisateurDAO.modifier(currentUser);
                    
                    // Update the session user
                    UserSession.getInstance().setCurrentUser(currentUser);
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Photo de Profil");
                    alert.setHeaderText("Votre photo de profil a été mise à jour avec succès");
                    alert.showAndWait();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur lors de la mise à jour de la photo de profil: " + e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur lors du téléchargement de l'image: " + e.getMessage());
            }
        }
    }
}