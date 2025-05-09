package controllers;

import dao.UtilisateurDAO;
import enums.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.*;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class UtilisateurController {

    @FXML private TableView<Utilisateur> utilisateurTable;
    @FXML private TableColumn<Utilisateur, String> nomPrenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, Role> roleColumn;
    @FXML private TableColumn<Utilisateur, String> roleSpecificColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsColumn;

    @FXML private TextField searchNameField; // Changed from searchIdField to searchNameField

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ObservableList<Utilisateur> utilisateurData = FXCollections.observableArrayList();

    public UtilisateurController() throws SQLException {
        // Constructor logic (if needed)
    }

    @FXML
    public void initialize() {
        try {
            // Initialize table columns
            // Removed idColumn initialization

            // Merge nom and prenom into a single column
            nomPrenomColumn.setCellValueFactory(cellData -> {
                Utilisateur utilisateur = cellData.getValue();
                String nom = utilisateur.getNom();
                String prenom = utilisateur.getPrenom();
                return new SimpleStringProperty((nom != null ? nom : "") + " " + (prenom != null ? prenom : ""));
            });

            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

            // Add role-specific data column
            roleSpecificColumn.setCellValueFactory(cellData -> {
                Utilisateur utilisateur = cellData.getValue();
                Role role = utilisateur.getRole();

                if (role == null) {
                    return new SimpleStringProperty("");
                }

                switch (role) {
                    case COACH:
                        Coach coach = (Coach) utilisateur;
                        return new SimpleStringProperty("Stratégie: " + coach.getStrategie());

                    case JOUEUR:
                        Joueur joueur = (Joueur) utilisateur;
                        return new SimpleStringProperty("Pseudo: " + joueur.getPseudoJeu() +
                                ", Rank: " + joueur.getRank() +
                                ", Win Rate: " + joueur.getWinRate() + "%");

                    case SPECTATEUR:
                        Spectateur spectateur = (Spectateur) utilisateur;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        return new SimpleStringProperty("Inscrit le: " +
                                (spectateur.getDateInscription() != null ?
                                        dateFormat.format(spectateur.getDateInscription()) : "N/A"));

                    case ADMIN:
                    default:
                        return new SimpleStringProperty("Administrateur");
                }
            });

            // Add action buttons to the "Actions" column
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button modifierButton = new Button("Modifier");
                private final Button supprimerButton = new Button("Supprimer");

                {
                    // Apply CSS styles to the buttons
                    modifierButton.getStyleClass().add("button-modifier");
                    supprimerButton.getStyleClass().add("button-supprimer");

                    // Set action handlers for the buttons
                    modifierButton.setOnAction(event -> {
                        Utilisateur selectedUser = getTableView().getItems().get(getIndex());
                        modifierUtilisateur(selectedUser);
                    });

                    supprimerButton.setOnAction(event -> {
                        Utilisateur selectedUser = getTableView().getItems().get(getIndex());
                        supprimerUtilisateur(selectedUser);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(modifierButton, supprimerButton);
                        hbox.setSpacing(10); // Add spacing between buttons
                        setGraphic(hbox);
                    }
                }
            });

            // Load all users into the table
            loadUtilisateurs();
            utilisateurTable.setItems(utilisateurData);

        } catch (NullPointerException e) {
            // Handle cases where FXML elements are not properly initialized
            e.printStackTrace();
            showError("Erreur d'initialisation", "Certains éléments de l'interface n'ont pas été initialisés correctement.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation du contrôleur.");
        }
    }

    @FXML
    public void ajouterUtilisateur() {
        openPopup(null); // Open popup in "add" mode
    }

    public void modifierUtilisateur(Utilisateur selectedUser) {
        if (selectedUser == null) {
            showError("Erreur de sélection", "Aucun utilisateur sélectionné pour modification.");
            return;
        }
        openPopup(selectedUser); // Open popup in "edit" mode
    }

    public void supprimerUtilisateur(Utilisateur selectedUser) {
        if (selectedUser == null) {
            showError("Erreur de sélection", "Aucun utilisateur sélectionné pour suppression.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression d'utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    utilisateurDAO.supprimer(selectedUser.getId());
                    loadUtilisateurs(); // Refresh the table
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur de suppression", "Impossible de supprimer l'utilisateur.");
                }
            }
        });
    }

    private void openPopup(Utilisateur utilisateur) {
        try {
            // Load the FXML file for the pop-up
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UtilisateurPopup.fxml"));
            VBox popupContent = loader.load();

            // Get the controller and set the user if in edit mode
            UtilisateurPopupController popupController = loader.getController();
            if (utilisateur != null) {
                popupController.setUtilisateur(utilisateur);
            }

            // Create a new stage for the pop-up
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            popupStage.setTitle(utilisateur == null ? "Ajouter Utilisateur" : "Modifier Utilisateur");
            popupStage.setScene(new Scene(popupContent));

            // Show the pop-up and wait for it to close
            popupStage.showAndWait();

            // Retrieve the updated or new user
            Utilisateur updatedUser = popupController.getUtilisateur();
            if (updatedUser != null) {
                if (utilisateur == null) {
                    // Add new user
                    utilisateurDAO.ajouter(updatedUser);
                } else {
                    // Update existing user
                    utilisateurDAO.modifier(updatedUser);
                }
                loadUtilisateurs(); // Refresh the table
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la fenêtre pop-up.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue", "Une erreur inattendue est survenue lors de l'ouverture de la fenêtre pop-up.");
        }
    }

    @FXML
    public void rechercherUtilisateur() {
        try {
            // Get the search term from the search field
            String searchTerm = searchNameField.getText();
            if (searchTerm == null || searchTerm.isEmpty()) {
                showError("Erreur de recherche", "Veuillez entrer un nom ou prénom à rechercher.");
                return;
            }

            // Search users by name or first name
            List<Utilisateur> utilisateurs = utilisateurDAO.rechercherParNomPrenom(searchTerm);

            if (!utilisateurs.isEmpty()) {
                // Display the searched users
                utilisateurData.clear();
                utilisateurData.addAll(utilisateurs);
            } else {
                showError("Aucun résultat", "Aucun utilisateur trouvé avec ce nom ou prénom.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de recherche", "Une erreur est survenue lors de la recherche.");
        }
    }

    @FXML
    public void afficherTousUtilisateurs() {
        try {
            // Reload all users into the table
            loadUtilisateurs();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger tous les utilisateurs.");
        }
    }

    private void loadUtilisateurs() {
        utilisateurData.clear(); // Clear existing data
        utilisateurData.addAll(utilisateurDAO.lireTous()); // Load all users from the database
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}