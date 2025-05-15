package controllers;

import dao.UtilisateurDAO;
import enums.UserStatus;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import models.Utilisateur;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserAdminController implements Initializable {

    @FXML private TableView<Utilisateur> userTable;
    @FXML private TableColumn<Utilisateur, Integer> idColumn;
    @FXML private TableColumn<Utilisateur, String> nomColumn;
    @FXML private TableColumn<Utilisateur, String> prenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> statusColumn;
    @FXML private TableColumn<Utilisateur, String> suspensionFinColumn;
    @FXML private TableColumn<Utilisateur, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    
    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> utilisateurs;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialiser le DAO
            utilisateurDAO = new UtilisateurDAO();
            
            // Configurer les colonnes de la table
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().toString()));
            
            // Configurer la colonne de statut
            statusColumn.setCellValueFactory(cellData -> {
                UserStatus status = cellData.getValue().getStatus();
                String statusText = "";
                
                switch (status) {
                    case ACTIF:
                        statusText = "Actif";
                        break;
                    case SUSPENDU:
                        statusText = "Suspendu";
                        break;
                    case BANNI:
                        statusText = "Banni";
                        break;
                }
                
                return new SimpleStringProperty(statusText);
            });
            
            // Configurer la colonne de date de fin de suspension
            suspensionFinColumn.setCellValueFactory(cellData -> {
                LocalDate dateFin = cellData.getValue().getSuspensionFin();
                if (dateFin == null) {
                    return new SimpleStringProperty("");
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    return new SimpleStringProperty(dateFin.format(formatter));
                }
            });
            
            // Configurer la colonne d'actions
            actionsColumn.setCellFactory(createActionsColumnCallback());
            
            // Charger les utilisateurs
            chargerUtilisateurs();
            
            // Configurer les événements
            searchButton.setOnAction(event -> rechercherUtilisateurs());
            refreshButton.setOnAction(event -> chargerUtilisateurs());
            
        } catch (SQLException e) {
            afficherErreur("Erreur d'initialisation", "Impossible de se connecter à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Charge tous les utilisateurs dans la table
     */
    private void chargerUtilisateurs() {
        try {
            List<Utilisateur> listeUtilisateurs = utilisateurDAO.lireTous();
            utilisateurs = FXCollections.observableArrayList(listeUtilisateurs);
            userTable.setItems(utilisateurs);
        } catch (Exception e) {
            afficherErreur("Erreur de chargement", "Impossible de charger les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Recherche des utilisateurs par nom ou prénom
     */
    private void rechercherUtilisateurs() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            chargerUtilisateurs();
            return;
        }
        
        try {
            List<Utilisateur> listeUtilisateurs = utilisateurDAO.rechercherParNomPrenom(searchTerm);
            utilisateurs = FXCollections.observableArrayList(listeUtilisateurs);
            userTable.setItems(utilisateurs);
        } catch (Exception e) {
            afficherErreur("Erreur de recherche", "Impossible de rechercher les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crée la factory pour la colonne d'actions
     */
    private Callback<TableColumn<Utilisateur, Void>, TableCell<Utilisateur, Void>> createActionsColumnCallback() {
        return new Callback<>() {
            @Override
            public TableCell<Utilisateur, Void> call(final TableColumn<Utilisateur, Void> param) {
                return new TableCell<>() {
                    private final Button banButton = new Button("Bannir");
                    private final Button unbanButton = new Button("Débannir");
                    private final Button timeoutButton = new Button("Suspendre");
                    private final Button removeTimeoutButton = new Button("Lever suspension");
                    private final VBox buttonsBox = new VBox(5, banButton, unbanButton, timeoutButton, removeTimeoutButton);
                    
                    {
                        // Configurer les styles des boutons
                        banButton.getStyleClass().add("danger-button");
                        unbanButton.getStyleClass().add("success-button");
                        timeoutButton.getStyleClass().add("warning-button");
                        removeTimeoutButton.getStyleClass().add("info-button");
                        
                        // Configurer les actions des boutons
                        banButton.setOnAction(event -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            bannirUtilisateur(utilisateur);
                        });
                        
                        unbanButton.setOnAction(event -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            debannirUtilisateur(utilisateur);
                        });
                        
                        timeoutButton.setOnAction(event -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            suspendreUtilisateur(utilisateur);
                        });
                        
                        removeTimeoutButton.setOnAction(event -> {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            leverSuspension(utilisateur);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                            
                            // Ajuster la visibilité des boutons en fonction du statut de l'utilisateur
                            banButton.setVisible(utilisateur.getStatus() != UserStatus.BANNI);
                            unbanButton.setVisible(utilisateur.getStatus() == UserStatus.BANNI);
                            timeoutButton.setVisible(utilisateur.getStatus() != UserStatus.SUSPENDU);
                            removeTimeoutButton.setVisible(utilisateur.getStatus() == UserStatus.SUSPENDU);
                            
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Bannir un utilisateur de façon permanente
     */
    private void bannirUtilisateur(Utilisateur utilisateur) {
        // Demander confirmation et raison du bannissement
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bannir l'utilisateur");
        dialog.setHeaderText("Vous êtes sur le point de bannir l'utilisateur " + utilisateur.getPrenom() + " " + utilisateur.getNom());
        dialog.setContentText("Veuillez indiquer la raison du bannissement:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String raison = result.get().trim();
            if (raison.isEmpty()) {
                raison = "Non spécifiée";
            }
            
            try {
                boolean success = utilisateurDAO.bannirUtilisateur(utilisateur.getId(), raison);
                if (success) {
                    afficherInformation("Utilisateur banni", "L'utilisateur a été banni avec succès.");
                    chargerUtilisateurs(); // Rafraîchir la liste
                } else {
                    afficherErreur("Erreur", "Impossible de bannir l'utilisateur.");
                }
            } catch (Exception e) {
                afficherErreur("Erreur", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Débannir un utilisateur précédemment banni
     */
    private void debannirUtilisateur(Utilisateur utilisateur) {
        // Demander confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Débannir l'utilisateur");
        alert.setHeaderText("Vous êtes sur le point de débannir l'utilisateur " + utilisateur.getPrenom() + " " + utilisateur.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir débannir cet utilisateur?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = utilisateurDAO.debannirUtilisateur(utilisateur.getId());
                if (success) {
                    afficherInformation("Utilisateur débanni", "L'utilisateur a été débanni avec succès.");
                    chargerUtilisateurs(); // Rafraîchir la liste
                } else {
                    afficherErreur("Erreur", "Impossible de débannir l'utilisateur.");
                }
            } catch (Exception e) {
                afficherErreur("Erreur", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Suspendre temporairement un utilisateur (time-out)
     */
    private void suspendreUtilisateur(Utilisateur utilisateur) {
        // Créer un dialogue personnalisé pour la suspension
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Suspendre l'utilisateur");
        dialog.setHeaderText("Vous êtes sur le point de suspendre temporairement l'utilisateur " + 
                           utilisateur.getPrenom() + " " + utilisateur.getNom());
        
        // Créer les champs du formulaire
        Label raisonLabel = new Label("Raison de la suspension:");
        TextArea raisonField = new TextArea();
        raisonField.setPrefRowCount(3);
        
        Label dureeLabel = new Label("Durée de la suspension:");
        ComboBox<String> dureeCombo = new ComboBox<>();
        dureeCombo.getItems().addAll(
            "24 heures",
            "48 heures",
            "3 jours",
            "7 jours",
            "14 jours",
            "30 jours"
        );
        dureeCombo.setValue("24 heures");
        
        // Créer la mise en page
        VBox content = new VBox(10);
        content.getChildren().addAll(raisonLabel, raisonField, dureeLabel, dureeCombo);
        dialog.getDialogPane().setContent(content);
        
        // Ajouter les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Afficher le dialogue et traiter le résultat
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String raison = raisonField.getText().trim();
            if (raison.isEmpty()) {
                raison = "Non spécifiée";
            }
            
            // Convertir la durée sélectionnée en jours
            int dureeJours;
            switch (dureeCombo.getValue()) {
                case "24 heures":
                    dureeJours = 1;
                    break;
                case "48 heures":
                    dureeJours = 2;
                    break;
                case "3 jours":
                    dureeJours = 3;
                    break;
                case "7 jours":
                    dureeJours = 7;
                    break;
                case "14 jours":
                    dureeJours = 14;
                    break;
                case "30 jours":
                    dureeJours = 30;
                    break;
                default:
                    dureeJours = 1;
            }
            
            try {
                boolean success = utilisateurDAO.suspendreUtilisateur(utilisateur.getId(), raison, dureeJours);
                if (success) {
                    afficherInformation("Utilisateur suspendu", 
                                      "L'utilisateur a été suspendu pour " + dureeCombo.getValue() + ".");
                    chargerUtilisateurs(); // Rafraîchir la liste
                } else {
                    afficherErreur("Erreur", "Impossible de suspendre l'utilisateur.");
                }
            } catch (Exception e) {
                afficherErreur("Erreur", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Lever la suspension d'un utilisateur avant la date de fin prévue
     */
    private void leverSuspension(Utilisateur utilisateur) {
        // Demander confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Lever la suspension");
        alert.setHeaderText("Vous êtes sur le point de lever la suspension de l'utilisateur " + 
                          utilisateur.getPrenom() + " " + utilisateur.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir lever la suspension de cet utilisateur?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = utilisateurDAO.leverSuspension(utilisateur.getId());
                if (success) {
                    afficherInformation("Suspension levée", "La suspension de l'utilisateur a été levée avec succès.");
                    chargerUtilisateurs(); // Rafraîchir la liste
                } else {
                    afficherErreur("Erreur", "Impossible de lever la suspension de l'utilisateur.");
                }
            } catch (Exception e) {
                afficherErreur("Erreur", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une boîte de dialogue d'information
     */
    private void afficherInformation(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}