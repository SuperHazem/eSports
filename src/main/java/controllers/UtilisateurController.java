package controllers;

import dao.UtilisateurDAO;
import enums.Role;
import enums.UserStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
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
        // Ajouter une colonne pour le statut de l'utilisateur
        TableColumn<Utilisateur, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(cellData -> {
            Utilisateur utilisateur = cellData.getValue();
            String statusText = "";
            
            switch (utilisateur.getStatus()) {
                case ACTIF:
                    statusText = "Actif";
                    break;
                case SUSPENDU:
                    statusText = "Suspendu";
                    if (utilisateur.getSuspensionFin() != null) {
                        statusText += " (jusqu'au " + utilisateur.getSuspensionFin() + ")";
                    }
                    break;
                case BANNI:
                    statusText = "Banni";
                    break;
            }
            
            return new SimpleStringProperty(statusText);
        });
        statusColumn.setPrefWidth(150);
        statusColumn.getStyleClass().add("column");
        
        // Ajouter la colonne à la table
        utilisateurTable.getColumns().add(3, statusColumn); // Ajouter après la colonne de rôle
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
            private final Button bannirButton = new Button("Bannir");
            private final Button suspendreButton = new Button("Suspendre");
            private final Button activerButton = new Button("Activer");

            {
                // Apply CSS styles to the buttons
                modifierButton.getStyleClass().add("button-modifier");
                supprimerButton.getStyleClass().add("button-supprimer");
                bannirButton.getStyleClass().add("button-bannir");
                suspendreButton.getStyleClass().add("button-suspendre");
                activerButton.getStyleClass().add("button-activer");

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
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    HBox hbox = new HBox();
                    hbox.setSpacing(10); // Add spacing between buttons
                    
                    // Toujours afficher les boutons modifier et supprimer
                    hbox.getChildren().addAll(modifierButton, supprimerButton);
                    
                    // Afficher les boutons en fonction du statut de l'utilisateur
                    if (utilisateur.getStatus() == enums.UserStatus.ACTIF) {
                        // Pour un utilisateur actif, on peut le bannir ou le suspendre
                        bannirButton.setOnAction(event -> bannirUtilisateur(utilisateur));
                        suspendreButton.setOnAction(event -> suspendreUtilisateur(utilisateur));
                        hbox.getChildren().addAll(bannirButton, suspendreButton);
                    } else if (utilisateur.getStatus() == enums.UserStatus.BANNI) {
                        // Pour un utilisateur banni, on peut le réactiver
                        activerButton.setOnAction(event -> debannirUtilisateur(utilisateur));
                        hbox.getChildren().add(activerButton);
                    } else if (utilisateur.getStatus() == enums.UserStatus.SUSPENDU) {
                        // Pour un utilisateur suspendu, on peut le réactiver
                        activerButton.setOnAction(event -> leverSuspension(utilisateur));
                        hbox.getChildren().add(activerButton);
                    }
                    
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
    
    /**
     * Bannir un utilisateur de façon permanente
     */
    private void bannirUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            showError("Erreur de sélection", "Aucun utilisateur sélectionné pour bannissement.");
            return;
        }
        
        // Créer une boîte de dialogue pour saisir la raison du bannissement
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bannissement d'utilisateur");
        dialog.setHeaderText("Bannir l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom());
        dialog.setContentText("Raison du bannissement:");
        
        dialog.showAndWait().ifPresent(raison -> {
            if (raison.trim().isEmpty()) {
                showError("Erreur", "Vous devez spécifier une raison pour le bannissement.");
                return;
            }
            
            try {
                boolean success = utilisateurDAO.bannirUtilisateur(utilisateur.getId(), raison);
                if (success) {
                    loadUtilisateurs(); // Rafraîchir la table
                    applyFiltering(); // Réappliquer les filtres
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("L'utilisateur a été banni avec succès.");
                    alert.showAndWait();
                } else {
                    showError("Erreur", "Impossible de bannir l'utilisateur.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors du bannissement de l'utilisateur.");
            }
        });
    }
    
    /**
     * Débannir un utilisateur précédemment banni
     */
    private void debannirUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            showError("Erreur de sélection", "Aucun utilisateur sélectionné pour débannissement.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Débannir l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom());
        alert.setContentText("Êtes-vous sûr de vouloir débannir cet utilisateur ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = utilisateurDAO.debannirUtilisateur(utilisateur.getId());
                    if (success) {
                        loadUtilisateurs(); // Rafraîchir la table
                        applyFiltering(); // Réappliquer les filtres
                        
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Succès");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("L'utilisateur a été débanni avec succès.");
                        successAlert.showAndWait();
                    } else {
                        showError("Erreur", "Impossible de débannir l'utilisateur.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Une erreur est survenue lors du débannissement de l'utilisateur.");
                }
            }
        });
    }
    
    /**
     * Suspendre temporairement un utilisateur
     */
    private void suspendreUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            showError("Erreur de sélection", "Aucun utilisateur sélectionné pour suspension.");
            return;
        }
        
        // Créer une boîte de dialogue personnalisée pour la suspension
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Suspension temporaire");
        dialog.setHeaderText("Suspendre l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom());
        
        // Boutons
        ButtonType suspendreButtonType = new ButtonType("Suspendre", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(suspendreButtonType, ButtonType.CANCEL);
        
        // Créer la grille pour le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Champs du formulaire
        TextArea raisonField = new TextArea();
        raisonField.setPromptText("Raison de la suspension");
        raisonField.setPrefRowCount(3);
        
        ComboBox<String> dureeComboBox = new ComboBox<>();
        dureeComboBox.getItems().addAll(
            "24 heures", "48 heures", "3 jours", "7 jours", "14 jours", "30 jours"
        );
        dureeComboBox.setValue("24 heures");
        
        grid.add(new Label("Raison:"), 0, 0);
        grid.add(raisonField, 1, 0);
        grid.add(new Label("Durée:"), 0, 1);
        grid.add(dureeComboBox, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir le résultat en paire raison/durée
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == suspendreButtonType) {
                String raison = raisonField.getText();
                String dureeSel = dureeComboBox.getValue();
                
                // Convertir la durée sélectionnée en nombre de jours
                int jours;
                switch (dureeSel) {
                    case "24 heures": jours = 1; break;
                    case "48 heures": jours = 2; break;
                    case "3 jours": jours = 3; break;
                    case "7 jours": jours = 7; break;
                    case "14 jours": jours = 14; break;
                    case "30 jours": jours = 30; break;
                    default: jours = 1;
                }
                
                return new Pair<>(raison, jours);
            }
            return null;
        });
        
        // Afficher la boîte de dialogue et traiter le résultat
        dialog.showAndWait().ifPresent(result -> {
            String raison = result.getKey();
            int jours = result.getValue();
            
            if (raison.trim().isEmpty()) {
                showError("Erreur", "Vous devez spécifier une raison pour la suspension.");
                return;
            }
            
            try {
                // Appeler la méthode pour suspendre l'utilisateur
                boolean success = utilisateurDAO.suspendreUtilisateur(utilisateur.getId(), raison, jours);
                
                if (success) {
                    loadUtilisateurs(); // Rafraîchir la table
                    applyFiltering(); // Réappliquer les filtres
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText(null);
                    alert.setContentText("L'utilisateur a été suspendu avec succès pour " + jours + " jour(s).");
                    alert.showAndWait();
                } else {
                    showError("Erreur", "Impossible de suspendre l'utilisateur.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors de la suspension de l'utilisateur.");
            }
        });
    }
    
    /**
     * Lever une suspension avant sa date de fin prévue
     */
    private void leverSuspension(Utilisateur utilisateur) {
        if (utilisateur == null) {
            showError("Erreur de sélection", "Aucun utilisateur sélectionné pour lever la suspension.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Lever la suspension de l'utilisateur " + utilisateur.getNom() + " " + utilisateur.getPrenom());
        alert.setContentText("Êtes-vous sûr de vouloir lever la suspension de cet utilisateur avant sa date de fin prévue ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Appeler la méthode pour lever la suspension
                    boolean success = utilisateurDAO.leverSuspension(utilisateur.getId());
                    
                    if (success) {
                        loadUtilisateurs(); // Rafraîchir la table
                        applyFiltering(); // Réappliquer les filtres
                        
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Succès");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("La suspension de l'utilisateur a été levée avec succès.");
                        successAlert.showAndWait();
                    } else {
                        showError("Erreur", "Impossible de lever la suspension de l'utilisateur.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Une erreur est survenue lors de la levée de suspension de l'utilisateur.");
                }
            }
        });
    }
    

    

}