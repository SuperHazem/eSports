package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Publication;
import models.Commentaire;
import dao.PublicationDAO;
import dao.CommentaireDAO;
import dao.PublicationDAOImpl;
import dao.CommentaireDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.SimpleStringProperty;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

public class PublicationController {
    private static final int CURRENT_USER_ID = 2;
    private final PublicationDAO publicationDAO = new PublicationDAOImpl();
    private final CommentaireDAO commentaireDAO = new CommentaireDAOImpl();
    private Publication publicationSelectionnee;
    private ObservableList<Publication> publications;
    private FilteredList<Publication> publicationsFiltered;

    @FXML private TableView<Publication> publicationTable;
    @FXML private TableColumn<Publication, String> titreCol;
    @FXML private TableColumn<Publication, String> contenuCol;
    @FXML private TableColumn<Publication, String> dateCol;
    @FXML private TableColumn<Publication, Void> actionsCol;

    @FXML private TableView<Commentaire> commentaireTable;
    @FXML private TableColumn<Commentaire, String> commentaireContenuCol;
    @FXML private TableColumn<Commentaire, Integer> commentaireNoteCol;
    @FXML private TableColumn<Commentaire, String> commentaireDateCol;
    @FXML private TableColumn<Commentaire, Void> commentaireActionsCol;
    
    @FXML private TextField nouveauCommentaireField;
    @FXML private Spinner<Integer> noteSpinner;
    @FXML private TextField searchField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;

    @FXML
    public void initialize() {
        configurerColonnesPublication();
        configurerColonnesCommentaire();
        configurerRecherche();
        chargerPublications();
    }

    private void configurerRecherche() {
        // Initialize the filtered list
        publications = FXCollections.observableArrayList();
        publicationsFiltered = new FilteredList<>(publications);
        publicationTable.setItems(publicationsFiltered);

        // Add listeners for search field and date pickers
        searchField.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        dateDebutPicker.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
        dateFinPicker.valueProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());
    }

    @FXML
    private void rechercherPublications() {
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserRecherche() {
        searchField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        publicationsFiltered.setPredicate(publication -> {
            boolean matchSearchText = true;
            boolean matchDateRange = true;

            // Text search
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().toLowerCase();
                matchSearchText = publication.getTitre().toLowerCase().contains(searchText) ||
                                publication.getContenu().toLowerCase().contains(searchText);
            }

            // Date range
            if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
                LocalDate dateDebut = dateDebutPicker.getValue();
                LocalDate dateFin = dateFinPicker.getValue();
                LocalDate publicationDate = publication.getDatePublication()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                
                matchDateRange = !publicationDate.isBefore(dateDebut) && !publicationDate.isAfter(dateFin);
            }

            return matchSearchText && matchDateRange;
        });
    }

    private void configurerColonnesPublication() {
        titreCol.setCellValueFactory(cellData -> cellData.getValue().titreProperty());
        contenuCol.setCellValueFactory(cellData -> cellData.getValue().contenuProperty());
        dateCol.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDatePublication();
            if (date != null) {
                return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date));
            }
            return new SimpleStringProperty("");
        });
        
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Supprimer");
            private final HBox boutons = new HBox(10, modifierBtn, supprimerBtn);

            {
                modifierBtn.getStyleClass().add("button-modifier");
                supprimerBtn.getStyleClass().add("button-supprimer");
                boutons.setStyle("-fx-alignment: CENTER;");
                
                modifierBtn.setOnAction(event -> {
                    Publication publication = getTableView().getItems().get(getIndex());
                    if (publication.getAuteur() == CURRENT_USER_ID) {
                        modifierPublication(publication);
                    } else {
                        afficherAlerte("Action non autorisée", "Vous ne pouvez modifier que vos propres publications.");
                    }
                });

                supprimerBtn.setOnAction(event -> {
                    Publication publication = getTableView().getItems().get(getIndex());
                    if (publication.getAuteur() == CURRENT_USER_ID) {
                        supprimerPublication(publication);
                    } else {
                        afficherAlerte("Action non autorisée", "Vous ne pouvez supprimer que vos propres publications.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Publication publication = getTableView().getItems().get(getIndex());
                    if (publication.getAuteur() == CURRENT_USER_ID) {
                        setGraphic(boutons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        publicationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                publicationSelectionnee = newSelection;
                chargerCommentaires(newSelection.getId());
            }
        });
    }

    private void configurerColonnesCommentaire() {
        commentaireContenuCol.setCellValueFactory(cellData -> cellData.getValue().contenuProperty());
        commentaireNoteCol.setCellValueFactory(cellData -> cellData.getValue().noteProperty().asObject());
        commentaireDateCol.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDate();
            if (date != null) {
                return new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date));
            }
            return new SimpleStringProperty("");
        });
        
        commentaireActionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Supprimer");
            private final HBox boutons = new HBox(10, modifierBtn, supprimerBtn);

            {
                modifierBtn.getStyleClass().add("button-modifier");
                supprimerBtn.getStyleClass().add("button-supprimer");
                boutons.setStyle("-fx-alignment: CENTER;");
                
                modifierBtn.setOnAction(event -> {
                    Commentaire commentaire = getTableView().getItems().get(getIndex());
                    if (commentaire.getAuteur() == CURRENT_USER_ID) {
                        modifierCommentaire(commentaire);
                    } else {
                        afficherAlerte("Action non autorisée", "Vous ne pouvez modifier que vos propres commentaires.");
                    }
                });

                supprimerBtn.setOnAction(event -> {
                    Commentaire commentaire = getTableView().getItems().get(getIndex());
                    if (commentaire.getAuteur() == CURRENT_USER_ID) {
                        supprimerCommentaire(commentaire);
                    } else {
                        afficherAlerte("Action non autorisée", "Vous ne pouvez supprimer que vos propres commentaires.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Commentaire commentaire = getTableView().getItems().get(getIndex());
                    if (commentaire.getAuteur() == CURRENT_USER_ID) {
                        setGraphic(boutons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    @FXML
    private void ajouterPublication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PublicationPopup.fxml"));
            Parent root = loader.load();
            PublicationPopupController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Publication");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerPublications();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la fenêtre de création de publication.");
        }
    }

    private void modifierPublication(Publication publication) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PublicationPopup.fxml"));
            Parent root = loader.load();
            PublicationPopupController controller = loader.getController();
            controller.setPublication(publication);

            Stage stage = new Stage();
            stage.setTitle("Modifier Publication");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerPublications();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la fenêtre de modification de publication.");
        }
    }

    private void supprimerPublication(Publication publication) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la publication");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette publication ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                publicationDAO.supprimer(publication.getId());
                chargerPublications();
            } catch (Exception e) {
                afficherAlerte("Erreur", "Impossible de supprimer la publication: " + e.getMessage());
            }
        }
    }

    @FXML
    private void ajouterCommentaire() {
        if (publicationSelectionnee == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner une publication pour commenter.");
            return;
        }

        String contenu = nouveauCommentaireField.getText().trim();
        if (contenu.isEmpty()) {
            afficherAlerte("Erreur", "Le commentaire ne peut pas être vide.");
            return;
        }

        try {
            Commentaire commentaire = new Commentaire(contenu, noteSpinner.getValue(), publicationSelectionnee);
            commentaireDAO.ajouter(commentaire);
            nouveauCommentaireField.clear();
            noteSpinner.getValueFactory().setValue(3);
            chargerCommentaires(publicationSelectionnee.getId());
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible d'ajouter le commentaire: " + e.getMessage());
        }
    }

    private void modifierCommentaire(Commentaire commentaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommentairePopup.fxml"));
            Parent root = loader.load();
            CommentairePopupController controller = loader.getController();
            controller.setCommentaire(commentaire);

            Stage stage = new Stage();
            stage.setTitle("Modifier Commentaire");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerCommentaires(publicationSelectionnee.getId());
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la fenêtre de modification de commentaire.");
        }
    }

    private void supprimerCommentaire(Commentaire commentaire) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le commentaire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                commentaireDAO.supprimer(commentaire.getId());
                chargerCommentaires(publicationSelectionnee.getId());
            } catch (Exception e) {
                afficherAlerte("Erreur", "Impossible de supprimer le commentaire: " + e.getMessage());
            }
        }
    }

    private void chargerPublications() {
        try {
            List<Publication> listePublications = publicationDAO.lireTous();
            publications.setAll(listePublications);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de charger les publications: " + e.getMessage());
        }
    }

    private void chargerCommentaires(Integer publicationId) {
        try {
            List<Commentaire> commentaires = commentaireDAO.lireParPublication(publicationId);
            commentaireTable.setItems(FXCollections.observableArrayList(commentaires));
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de charger les commentaires: " + e.getMessage());
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 