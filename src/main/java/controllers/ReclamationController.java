package controllers;

import dao.ReclamationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Reclamation;
import enums.Statut;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReclamationController implements Initializable {

    @FXML
    private TableView<Reclamation> tableReclamations;

    @FXML
    private TableColumn<Reclamation, String> colObjet;

    @FXML
    private TableColumn<Reclamation, String> colDescription;

    @FXML
    private TableColumn<Reclamation, String> colDate;

    @FXML
    private TableColumn<Reclamation, String> colTicket;

    @FXML
    private TableColumn<Reclamation, Statut> colStatut;

    @FXML
    private TableColumn<Reclamation, Void> colActions;

    @FXML
    private ComboBox<String> comboStatut;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button btnNouvelleReclamation;

    @FXML
    private Button btnRechercher;

    @FXML
    private Button btnReinitialiser;

    private ReclamationDAO reclamationDAO;
    private ObservableList<Reclamation> reclamationsList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            reclamationDAO = new ReclamationDAO();

            // S'assurer que la colonne statut existe
            reclamationDAO.ajouterColonneStatutSiNecessaire();

            // Configuration des colonnes pour afficher correctement l'objet et la description
            colObjet.setCellValueFactory(new PropertyValueFactory<>("objet"));
            colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

            // Pour les dates, on utilise un format personnalisé
            colDate.setCellValueFactory(cellData -> {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = sdf.format(cellData.getValue().getDate());
                return javafx.beans.binding.Bindings.createStringBinding(() -> dateStr);
            });

            colTicket.setCellValueFactory(cellData -> {
                String ticketId = cellData.getValue().getTicket() != null ?
                        "T-" + cellData.getValue().getTicket().getId() : "";
                return javafx.beans.binding.Bindings.createStringBinding(() -> ticketId);
            });

            // Utiliser getStatutLibelle pour afficher le libellé du statut
            colStatut.setCellValueFactory(cellData ->
                    javafx.beans.binding.Bindings.createObjectBinding(
                            () -> cellData.getValue().getStatut()
                    )
            );

            // Personnaliser l'affichage du statut avec des couleurs
            colStatut.setCellFactory(column -> new TableCell<Reclamation, Statut>() {
                @Override
                protected void updateItem(Statut item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("statut-en-cours", "statut-resolu", "statut-rejete");
                    } else {
                        setText(item.getLibelle());
                        getStyleClass().removeAll("statut-en-cours", "statut-resolu", "statut-rejete");

                        switch (item) {
                            case EN_COURS:
                                getStyleClass().add("statut-en-cours");
                                break;
                            case RESOLU:
                                getStyleClass().add("statut-resolu");
                                break;
                            case REJETE:
                                getStyleClass().add("statut-rejete");
                                break;
                        }
                    }
                }
            });

            // Configurer la colonne d'actions
            configurerColonneActions();

            // Initialiser le ComboBox des statuts
            List<String> statutsLibelles = Stream.concat(
                    Stream.of("Tous"),
                    Stream.of(Statut.values()).map(Statut::getLibelle)
            ).collect(Collectors.toList());

            comboStatut.setItems(FXCollections.observableArrayList(statutsLibelles));
            comboStatut.getSelectionModel().selectFirst();

            // Charger les données
            chargerReclamations();

        } catch (SQLException e) {
            afficherErreur("Erreur de connexion à la base de données", e.getMessage());
        }
    }

    private void configurerColonneActions() {
        colActions.setCellFactory(param -> new TableCell<Reclamation, Void>() {
            private final Button detailsBtn = new Button("DÉTAILS");
            private final Button modifierBtn = new Button("MODIFIER"); // Nouveau bouton MODIFIER
            private final Button repondreBtn = new Button("RÉPONDRE");
            private final Button supprimerBtn = new Button("SUPPRIMER");

            {
                detailsBtn.getStyleClass().add("details-button");
                modifierBtn.getStyleClass().add("modifier-button"); // Ajouter une classe CSS pour le style
                repondreBtn.getStyleClass().add("save-button");
                supprimerBtn.getStyleClass().add("delete-button");

                detailsBtn.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    afficherDetailsReclamation(reclamation);
                });

                modifierBtn.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    ouvrirModificationReclamation(reclamation);
                });

                repondreBtn.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    ouvrirReponseReclamation(reclamation);
                });
                supprimerBtn.setOnAction(event -> {
                    Reclamation reclamation = getTableView().getItems().get(getIndex());
                    confirmerSuppression(reclamation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    // Créer un conteneur pour les boutons
                    HBox buttonsBox = new HBox(5);
                    buttonsBox.getChildren().addAll(detailsBtn, modifierBtn, repondreBtn, supprimerBtn);
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void chargerReclamations() {
        List<Reclamation> reclamations = reclamationDAO.lireTous();
        reclamationsList = FXCollections.observableArrayList(reclamations);
        tableReclamations.setItems(reclamationsList);
    }

    @FXML
    private void rechercherReclamations(ActionEvent event) {
        String statutStr = comboStatut.getValue();
        Statut statut = null;

        if (statutStr != null && !statutStr.equals("Tous")) {
            statut = Statut.fromString(statutStr);
        }

        Date date = null;
        if (datePicker.getValue() != null) {
            date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        List<Reclamation> reclamations = reclamationDAO.rechercherParStatutEtDate(statut, date);
        reclamationsList = FXCollections.observableArrayList(reclamations);
        tableReclamations.setItems(reclamationsList);
    }

    @FXML
    private void reinitialiserRecherche(ActionEvent event) {
        comboStatut.getSelectionModel().selectFirst();
        datePicker.setValue(null);
        chargerReclamations();
    }

    @FXML
    private void ouvrirNouvelleReclamation(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/ReclamationPopup.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier ReclamationPopup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ReclamationPopupController controller = loader.getController();
            controller.setMode("ajouter");
            controller.setReclamationController(this);

            // Créer la scène et ajouter le CSS programmatiquement
            Scene scene = new Scene(root);

            // Ajouter le CSS programmatiquement au lieu de le définir dans le FXML
            URL cssUrl = getClass().getResource("/styles/reclamation-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS chargé avec succès: " + cssUrl.toExternalForm());
            } else {
                System.err.println("ERREUR: Impossible de trouver le fichier CSS: /styles/reclamation-style.css");
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvelle Réclamation");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de nouvelle réclamation: " + e.getMessage());
        }
    }

    // Nouvelle méthode pour ouvrir la fenêtre de modification d'une réclamation
    private void ouvrirModificationReclamation(Reclamation reclamation) {
        try {
            URL fxmlUrl = getClass().getResource("/ReclamationPopup.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier ReclamationPopup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ReclamationPopupController controller = loader.getController();
            controller.setMode("modifier");
            controller.setReclamation(reclamation);
            controller.setReclamationController(this);

            // Créer la scène et ajouter le CSS programmatiquement
            Scene scene = new Scene(root);

            // Ajouter le CSS programmatiquement
            URL cssUrl = getClass().getResource("/styles/reclamation-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS chargé avec succès: " + cssUrl.toExternalForm());
            } else {
                System.err.println("ERREUR: Impossible de trouver le fichier CSS: /styles/reclamation-style.css");
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier la Réclamation");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    private void afficherDetailsReclamation(Reclamation reclamation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la réclamation");
        alert.setHeaderText("Réclamation #" + reclamation.getId());

        StringBuilder content = new StringBuilder();
        content.append("Objet: ").append(reclamation.getObjet()).append("\n\n");
        content.append("Description: ").append(reclamation.getDescription()).append("\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        content.append("Date: ").append(sdf.format(reclamation.getDate())).append("\n\n");

        content.append("Ticket: ").append(reclamation.getTicket() != null ? "T-" + reclamation.getTicket().getId() : "Non spécifié").append("\n\n");
        content.append("Statut: ").append(reclamation.getStatut().getLibelle());

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void ouvrirReponseReclamation(Reclamation reclamation) {
        // Cette méthode sera implémentée plus tard pour ouvrir l'interface de réponse
        afficherInfo("Information", "La fonctionnalité de réponse sera implémentée prochainement.");
    }

    public void rafraichirTableau() {
        chargerReclamations();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void confirmerSuppression(Reclamation reclamation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la réclamation #" + reclamation.getId());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ? Cette action est irréversible.");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Supprimer la réclamation de la base de données
                    reclamationDAO.supprimer(reclamation.getId());

                    // Rafraîchir le tableau
                    rafraichirTableau();

                    // Afficher un message de confirmation
                    afficherInfo("Suppression réussie", "La réclamation a été supprimée avec succès.");
                } catch (Exception e) {
                    afficherErreur("Erreur de suppression", "Une erreur s'est produite lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void supprimerReclamation() {
        Reclamation reclamationSelectionnee = tableReclamations.getSelectionModel().getSelectedItem();
        if (reclamationSelectionnee != null) {
            confirmerSuppression(reclamationSelectionnee);
        } else {
            afficherAvertissement("Aucune réclamation sélectionnée", "Veuillez sélectionner une réclamation à supprimer.");
        }
    }

    private void afficherAvertissement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
