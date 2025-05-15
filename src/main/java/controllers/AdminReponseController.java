package controllers;

import dao.ReclamationDAO;
import dao.ReponseDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Reponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminReponseController {
    @FXML
    private TableView<Reponse> reponsesTableView;

    @FXML
    private TableColumn<Reponse, Integer> idColumn;


    @FXML
    private TableColumn<Reponse, String> contenuColumn;

    @FXML
    private TableColumn<Reponse, String> adminColumn;

    @FXML
    private TableColumn<Reponse, Date> dateColumn;

    @FXML
    private TableColumn<Reponse, Void> actionsColumn;

    @FXML
    private TextField rechercheField;

    @FXML
    private Button rechercherButton;

    @FXML
    private Button nouvelleReponseButton;

    @FXML
    private DatePicker rechercheDatePicker;
    @FXML
    private TableColumn<Reponse, String> objetReclamationColumn;


    private ReponseDAO reponseDAO;
    private ObservableList<Reponse> reponsesList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public void initialize() {
        try {
            reponseDAO = new ReponseDAO();
            ReclamationDAO reclamationDAO = new ReclamationDAO(); // Ajoutez cette ligne

            // Configurer les colonnes
            // Suppression des colonnes ID et Admin comme demandé
            idColumn.setVisible(false);
            adminColumn.setVisible(false);

            contenuColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));
            objetReclamationColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(
                            cellData.getValue().getReclamation() != null
                                    ? cellData.getValue().getReclamation().getObjet()
                                    : "Aucun objet")
            );


            // Formater la date
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            dateColumn.setCellFactory(column -> new TableCell<Reponse, Date>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(dateFormat.format(item));
                    }
                }
            });

            // Limiter le contenu affiché
            contenuColumn.setCellFactory(column -> new TableCell<Reponse, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Limiter à 50 caractères
                        setText(item.length() > 50 ? item.substring(0, 47) + "..." : item);
                    }
                }
            });

            // Configurer la colonne d'actions
            configurerColonneActions();

            // Configurer les boutons
            rechercherButton.setOnAction(event -> rechercherReponseParDate());
            nouvelleReponseButton.setOnAction(event -> ouvrirPopupNouvelleReponse());

            // Charger toutes les réponses au démarrage
            chargerToutesReponses();

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    private void rechercherReponseParDate() {
        try {
            if (rechercheDatePicker.getValue() == null) {
                chargerToutesReponses();
                return;
            }

            Date date = Date.from(rechercheDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<Reponse> reponses = reponseDAO.rechercherParDate(date);
            reponsesList.clear();
            reponsesList.addAll(reponses);
            reponsesTableView.setItems(reponsesList);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'effectuer la recherche par date");
        }
    }

    private void configurerColonneActions() {
        actionsColumn.setCellFactory(param -> new TableCell<Reponse, Void>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Supprimer");

            {
                modifierBtn.setStyle("-fx-background-color: #00b8d9; -fx-text-fill: white;");
                supprimerBtn.setStyle("-fx-background-color: #00b8d9; -fx-text-fill: white;");

                modifierBtn.setOnAction(event -> {
                    Reponse reponse = getTableView().getItems().get(getIndex());
                    ouvrirPopupModificationReponse(reponse);
                });

                supprimerBtn.setOnAction(event -> {
                    Reponse reponse = getTableView().getItems().get(getIndex());
                    supprimerReponse(reponse);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    // Créer un conteneur pour les boutons d'action
                    HBox buttonsBox = new HBox(5);
                    buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void chargerToutesReponses() {
        try {
            List<Reponse> reponses = reponseDAO.lireTous();
            reponsesList.clear();
            reponsesList.addAll(reponses);
            reponsesTableView.setItems(reponsesList);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger les réponses");
        }
    }

    private void rechercherReponse() {
        try {
            String recherche = rechercheField.getText().trim();

            if (recherche.isEmpty()) {
                chargerToutesReponses();
                return;
            }

            List<Reponse> reponses = reponseDAO.rechercherParContenu(recherche);
            reponsesList.clear();
            reponsesList.addAll(reponses);
            reponsesTableView.setItems(reponsesList);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'effectuer la recherche");
        }
    }

    private void supprimerReponse(Reponse reponse) {
        try {
            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer la réponse");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Correction ici: Gérer l'exception SQLException
                        reponseDAO.supprimer(reponse.getId());
                        chargerToutesReponses();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        afficherErreur("Erreur", "Impossible de supprimer la réponse: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de supprimer la réponse");
        }
    }

    private void ouvrirPopupNouvelleReponse() {
        try {
            URL fxmlUrl = getClass().getResource("/AdminNouvelleReponseView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AdminNouvelleReponseView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminNouvelleReponseController controller = loader.getController();
            controller.setParentController(this);

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/admin-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Nouvelle réponse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de nouvelle réponse");
        }
    }

    private void ouvrirPopupModificationReponse(Reponse reponse) {
        try {
            URL fxmlUrl = getClass().getResource("/AdminNouvelleReponseView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AdminNouvelleReponseView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminNouvelleReponseController controller = loader.getController();
            controller.setParentController(this);
            controller.setReponse(reponse); // Mode modification

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/admin-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier la réponse");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de modification");
        }
    }

    public void rafraichirTableau() {
        chargerToutesReponses();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}