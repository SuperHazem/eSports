package controllers;

import dao.UtilisateurDAO;
import enums.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

    @FXML private TextField searchNameField;
    @FXML private ComboBox<String> roleFilterComboBox;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final ObservableList<Utilisateur> utilisateurData = FXCollections.observableArrayList();
    private FilteredList<Utilisateur> filteredData;

    public UtilisateurController() throws SQLException {
        // Constructor logic (if needed)
    }

    @FXML
    public void initialize() {
        try {
            // Initialize table columns
            setupTableColumns();
            
            // Initialize role filter combo box
            setupRoleFilter();
            
            // Setup search functionality
            setupSearch();
            
            // Load all users into the table
            loadUtilisateurs();
            
            // Apply initial filtering
            applyFiltering();
            
        } catch (NullPointerException e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Certains éléments de l'interface n'ont pas été initialisés correctement.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation du contrôleur.");
        }
    }
    
    private void setupTableColumns() {
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
    }
    
    private void setupRoleFilter() {
        if (roleFilterComboBox != null) {
            roleFilterComboBox.getItems().addAll("Tous", "ADMIN", "COACH", "JOUEUR", "SPECTATEUR");
            roleFilterComboBox.setValue("Tous");
            
            roleFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    applyFiltering();
                }
            });
        }
    }
    
    private void setupSearch() {
        // Initialize filtered list
        filteredData = new FilteredList<>(utilisateurData, p -> true);
        
        // Add listener to search field
        if (searchNameField != null) {
            searchNameField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFiltering();
            });
        }
    }
    
    private void applyFiltering() {
        String searchText = searchNameField.getText().toLowerCase();
        String roleFilter = roleFilterComboBox.getValue();
        
        filteredData.setPredicate(utilisateur -> {
            // If search field is empty and role filter is "Tous", show all users
            if ((searchText == null || searchText.isEmpty()) && 
                (roleFilter == null || roleFilter.equals("Tous"))) {
                return true;
            }
            
            // Filter by role if not "Tous"
            if (roleFilter != null && !roleFilter.equals("Tous") && 
                (utilisateur.getRole() == null || !utilisateur.getRole().toString().equals(roleFilter))) {
                return false;
            }
            
            // If search field is empty, show all users that match the role filter
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            // Compare search text with user properties
            String nomPrenom = (utilisateur.getNom() + " " + utilisateur.getPrenom()).toLowerCase();
            String email = utilisateur.getEmail().toLowerCase();
            
            return nomPrenom.contains(searchText) || email.contains(searchText);
        });
        
        // Wrap the FilteredList in a SortedList
        SortedList<Utilisateur> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(utilisateurTable.comparatorProperty());
        
        // Update the TableView
        utilisateurTable.setItems(sortedData);
    }

    @FXML
    public void loadUtilisateurs() {
        List<Utilisateur> utilisateurs = utilisateurDAO.lireTous();
        utilisateurData.clear();
        utilisateurData.addAll(utilisateurs);
    }

    @FXML
    public void rechercherUtilisateur() {
        applyFiltering();
    }

    @FXML
    public void afficherTousUtilisateurs() {
        searchNameField.clear();
        roleFilterComboBox.setValue("Tous");
        applyFiltering();
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
                    applyFiltering(); // Reapply filters
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
                // Refresh the table
                loadUtilisateurs();
                applyFiltering();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre de modification.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}