package controllers;

import dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.*;
import enums.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class UtilisateurPopupController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private Label titleLabel;

    // Role-specific containers
    @FXML private VBox roleSpecificContainer;
    @FXML private VBox coachFields;
    @FXML private VBox joueurFields;
    @FXML private VBox spectateurFields;

    // Coach-specific fields
    @FXML private TextField strategieField;

    // Joueur-specific fields
    @FXML private TextField pseudoJeuField;
    @FXML private TextField rankField;
    @FXML private TextField winRateField;

    // Spectateur-specific fields
    @FXML private DatePicker dateInscriptionPicker;

    private Utilisateur utilisateur; // To hold the user being edited
    private boolean isEditMode = false; // Indicates whether in "add" or "edit" mode
    private Role originalRole; // Store the original role when editing

    public void initialize() {
        // Populate the role combo box
        roleComboBox.getItems().addAll("ADMIN", "COACH", "JOUEUR", "SPECTATEUR");

        // Add listener to show/hide role-specific fields
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateRoleSpecificFields(newVal);
            }
        });
    }

    private void updateRoleSpecificFields(String role) {
        // Hide all role-specific fields first
        coachFields.setVisible(false);
        coachFields.setManaged(false);
        joueurFields.setVisible(false);
        joueurFields.setManaged(false);
        spectateurFields.setVisible(false);
        spectateurFields.setManaged(false);

        // Show fields based on selected role
        switch (role) {
            case "COACH":
                coachFields.setVisible(true);
                coachFields.setManaged(true);
                break;

            case "JOUEUR":
                joueurFields.setVisible(true);
                joueurFields.setManaged(true);
                break;

            case "SPECTATEUR":
                spectateurFields.setVisible(true);
                spectateurFields.setManaged(true);
                break;

            case "ADMIN":
            default:
                // No specific fields for admin
                break;
        }
    }

    // Set the user object for editing
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        this.isEditMode = true;
        this.originalRole = utilisateur.getRole(); // Store the original role

        // Pre-fill fields if in edit mode
        emailField.setText(utilisateur.getEmail());
        passwordField.setText(utilisateur.getMotDePasseHash());
        roleComboBox.setValue(utilisateur.getRole().toString());
        nomField.setText(utilisateur.getNom());
        prenomField.setText(utilisateur.getPrenom());

        // Pre-fill role-specific fields
        Role role = utilisateur.getRole();
        switch (role) {
            case COACH:
                if (utilisateur instanceof Coach) {
                    Coach coach = (Coach) utilisateur;
                    strategieField.setText(coach.getStrategie());
                }
                break;

            case JOUEUR:
                if (utilisateur instanceof Joueur) {
                    Joueur joueur = (Joueur) utilisateur;
                    pseudoJeuField.setText(joueur.getPseudoJeu());
                    rankField.setText(joueur.getRank());
                    winRateField.setText(String.valueOf(joueur.getWinRate()));
                }
                break;

            case SPECTATEUR:
                if (utilisateur instanceof Spectateur) {
                    Spectateur spectateur = (Spectateur) utilisateur;
                    if (spectateur.getDateInscription() != null) {
                        LocalDate localDate = spectateur.getDateInscription().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        dateInscriptionPicker.setValue(localDate);
                    }
                }
                break;

            case ADMIN:
            default:
                // No specific fields for admin
                break;
        }

        // Update title for edit mode
        titleLabel.setText("Modifier Utilisateur");
    }

    @FXML
    public void enregistrer() {
        String email = emailField.getText().trim();
        String motDePasse = passwordField.getText().trim();
        String roleString = roleComboBox.getValue();
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();

        if (email.isEmpty() || motDePasse.isEmpty() || roleString == null || nom.isEmpty() || prenom.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            Role role = Role.valueOf(roleString.toUpperCase().trim());

            // Check if role has changed in edit mode
            boolean roleChanged = isEditMode && originalRole != role;

            // If we're in edit mode and the role has changed, we need to create a new object
            // instead of trying to cast the existing one
            if (isEditMode && roleChanged) {
                // Get the ID from the existing user
                int id = utilisateur.getId();

                // Create a new user with the appropriate type
                switch (role) {
                    case ADMIN:
                        utilisateur = new Admin(role, motDePasse, email, id, nom, prenom);
                        break;

                    case COACH:
                        String strategie = strategieField.getText().trim();
                        if (strategie.isEmpty()) {
                            showAlert("Erreur", "Veuillez remplir le champ Stratégie.");
                            return;
                        }
                        utilisateur = new Coach(role, motDePasse, email, id, nom, prenom, strategie);
                        break;

                    case JOUEUR:
                        String pseudoJeu = pseudoJeuField.getText().trim();
                        String rank = rankField.getText().trim();
                        String winRateStr = winRateField.getText().trim();

                        if (pseudoJeu.isEmpty() || rank.isEmpty() || winRateStr.isEmpty()) {
                            showAlert("Erreur", "Veuillez remplir tous les champs spécifiques au joueur.");
                            return;
                        }

                        double winRate;
                        try {
                            winRate = Double.parseDouble(winRateStr);
                        } catch (NumberFormatException e) {
                            showAlert("Erreur", "Le Win Rate doit être un nombre.");
                            return;
                        }

                        utilisateur = new Joueur(role, motDePasse, email, id, nom, prenom, pseudoJeu, winRate, rank);
                        break;

                    case SPECTATEUR:
                        LocalDate dateInscription = dateInscriptionPicker.getValue();
                        if (dateInscription == null) {
                            showAlert("Erreur", "Veuillez sélectionner une date d'inscription.");
                            return;
                        }

                        Date date = Date.from(dateInscription.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        utilisateur = new Spectateur(role, motDePasse, email, id, nom, prenom, date);
                        break;
                }
            } else {
                // Create a new user or update existing user without changing role
                if (isEditMode) {
                    // Update common fields
                    utilisateur.setEmail(email);
                    utilisateur.setMotDePasseHash(motDePasse);
                    utilisateur.setNom(nom);
                    utilisateur.setPrenom(prenom);

                    // Update role-specific fields
                    switch (role) {
                        case COACH:
                            if (utilisateur instanceof Coach) {
                                String strategie = strategieField.getText().trim();
                                if (strategie.isEmpty()) {
                                    showAlert("Erreur", "Veuillez remplir le champ Stratégie.");
                                    return;
                                }
                                ((Coach) utilisateur).setStrategie(strategie);
                            }
                            break;

                        case JOUEUR:
                            if (utilisateur instanceof Joueur) {
                                String pseudoJeu = pseudoJeuField.getText().trim();
                                String rank = rankField.getText().trim();
                                String winRateStr = winRateField.getText().trim();

                                if (pseudoJeu.isEmpty() || rank.isEmpty() || winRateStr.isEmpty()) {
                                    showAlert("Erreur", "Veuillez remplir tous les champs spécifiques au joueur.");
                                    return;
                                }

                                double winRate;
                                try {
                                    winRate = Double.parseDouble(winRateStr);
                                } catch (NumberFormatException e) {
                                    showAlert("Erreur", "Le Win Rate doit être un nombre.");
                                    return;
                                }

                                Joueur joueur = (Joueur) utilisateur;
                                joueur.setPseudoJeu(pseudoJeu);
                                joueur.setRank(rank);
                                joueur.setWinRate(winRate);
                            }
                            break;

                        case SPECTATEUR:
                            if (utilisateur instanceof Spectateur) {
                                LocalDate dateInscription = dateInscriptionPicker.getValue();
                                if (dateInscription == null) {
                                    showAlert("Erreur", "Veuillez sélectionner une date d'inscription.");
                                    return;
                                }

                                Date date = Date.from(dateInscription.atStartOfDay(ZoneId.systemDefault()).toInstant());
                                ((Spectateur) utilisateur).setDateInscription(date);
                            }
                            break;

                        case ADMIN:
                        default:
                            // No specific fields for admin
                            break;
                    }
                } else {
                    // Create a new user
                    switch (role) {
                        case ADMIN:
                            utilisateur = new Admin(role, motDePasse, email, 0, nom, prenom);
                            break;

                        case COACH:
                            String strategie = strategieField.getText().trim();
                            if (strategie.isEmpty()) {
                                showAlert("Erreur", "Veuillez remplir le champ Stratégie.");
                                return;
                            }
                            utilisateur = new Coach(role, motDePasse, email, 0, nom, prenom, strategie);
                            break;

                        case JOUEUR:
                            String pseudoJeu = pseudoJeuField.getText().trim();
                            String rank = rankField.getText().trim();
                            String winRateStr = winRateField.getText().trim();

                            if (pseudoJeu.isEmpty() || rank.isEmpty() || winRateStr.isEmpty()) {
                                showAlert("Erreur", "Veuillez remplir tous les champs spécifiques au joueur.");
                                return;
                            }

                            double winRate;
                            try {
                                winRate = Double.parseDouble(winRateStr);
                            } catch (NumberFormatException e) {
                                showAlert("Erreur", "Le Win Rate doit être un nombre.");
                                return;
                            }

                            utilisateur = new Joueur(role, motDePasse, email, 0, nom, prenom, pseudoJeu, winRate, rank);
                            break;

                        case SPECTATEUR:
                            LocalDate dateInscription = dateInscriptionPicker.getValue();
                            if (dateInscription == null) {
                                showAlert("Erreur", "Veuillez sélectionner une date d'inscription.");
                                return;
                            }

                            Date date = Date.from(dateInscription.atStartOfDay(ZoneId.systemDefault()).toInstant());
                            utilisateur = new Spectateur(role, motDePasse, email, 0, nom, prenom, date);
                            break;
                    }
                }
            }

            // Add this block to save the user to the database
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            if (isEditMode) {
                utilisateurDAO.modifier(utilisateur);
            } else {
                utilisateurDAO.ajouter(utilisateur);
            }

            // Close the pop-up
            closeStage();
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", "Rôle invalide: " + roleString);
        } catch (ClassCastException e) {
            showAlert("Erreur", "Erreur de type: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            showAlert("Erreur de base de données", "Impossible de sauvegarder l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void annuler() {
        closeStage();
    }

    private void closeStage() {
        // Get the stage from any node (e.g., the "Annuler" button)
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}