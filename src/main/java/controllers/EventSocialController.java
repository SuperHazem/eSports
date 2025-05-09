package controllers;

import dao.EventSocialDAO;
import dao.EventSocialDAOImpl;
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
import models.EventSocial;
import utils.validators.EventSocialValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EventSocialController {

    @FXML private TableView<EventSocial> eventTable;
    @FXML private TableColumn<EventSocial, String> nomColumn;
    @FXML private TableColumn<EventSocial, LocalDate> dateColumn;
    @FXML private TableColumn<EventSocial, String> lieuColumn;
    @FXML private TableColumn<EventSocial, String> descriptionColumn;
    @FXML private TableColumn<EventSocial, Integer> capaciteColumn;
    @FXML private TableColumn<EventSocial, Void> actionsColumn;

    @FXML private TextField searchNameField;
    @FXML private DatePicker searchDateField;

    private final EventSocialDAO eventDAO;
    private final ObservableList<EventSocial> eventData = FXCollections.observableArrayList();

    public EventSocialController() throws SQLException {
        this.eventDAO = new EventSocialDAOImpl();
    }

    @FXML
    public void initialize() {
        try {
            // Initialize table columns
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));

            // Add action buttons column
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button modifierButton = new Button("Modifier");
                private final Button supprimerButton = new Button("Supprimer");

                {
                    modifierButton.getStyleClass().add("button-modifier");
                    supprimerButton.getStyleClass().add("button-supprimer");

                    modifierButton.setOnAction(event -> {
                        EventSocial selectedEvent = getTableView().getItems().get(getIndex());
                        modifierEvent(selectedEvent);
                    });

                    supprimerButton.setOnAction(event -> {
                        EventSocial selectedEvent = getTableView().getItems().get(getIndex());
                        supprimerEvent(selectedEvent);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(modifierButton, supprimerButton);
                        hbox.setSpacing(10);
                        setGraphic(hbox);
                    }
                }
            });

            // Load all events into the table
            loadEvents();
            eventTable.setItems(eventData);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation du contrôleur.");
        }
    }

    @FXML
    public void ajouterEvent() {
        openPopup(null);
    }

    public void modifierEvent(EventSocial selectedEvent) {
        if (selectedEvent == null) {
            showError("Erreur de sélection", "Aucun événement sélectionné pour modification.");
            return;
        }
        openPopup(selectedEvent);
    }

    public void supprimerEvent(EventSocial selectedEvent) {
        if (selectedEvent == null) {
            showError("Erreur de sélection", "Aucun événement sélectionné pour suppression.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression d'événement");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    eventDAO.supprimer(selectedEvent.getId());
                    loadEvents();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur de suppression", "Impossible de supprimer l'événement.");
                }
            }
        });
    }

    private void openPopup(EventSocial event) {
        try {
            // Get the resource URL
            java.net.URL resource = getClass().getResource("/EventSocialPopup.fxml");
            if (resource == null) {
                throw new IOException("Cannot find EventSocialPopup.fxml");
            }

            // Create and configure the loader
            FXMLLoader loader = new FXMLLoader(resource);
            VBox popupContent = loader.load();

            // Get the controller and set up the event
            EventSocialPopupController popupController = loader.getController();
            if (event != null) {
                popupController.setEvent(event);
            }

            // Create and configure the popup stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle(event == null ? "Ajouter Événement" : "Modifier Événement");
            
            // Create scene with the loaded content
            Scene scene = new Scene(popupContent);
            popupStage.setScene(scene);

            // Show the popup and wait for it to close
            popupStage.showAndWait();

            // Handle the result
            EventSocial updatedEvent = popupController.getEvent();
            if (updatedEvent != null) {
                if (event == null) {
                    eventDAO.ajouter(updatedEvent);
                } else {
                    eventDAO.modifier(updatedEvent);
                }
                loadEvents();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la fenêtre pop-up: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    @FXML
    public void rechercherEvent() {
        try {
            String searchName = searchNameField.getText().trim();
            LocalDate searchDate = searchDateField.getValue();

            List<EventSocial> events;
            if (!searchName.isEmpty() && searchDate != null) {
                events = eventDAO.lireParNom(searchName).stream()
                        .filter(e -> e.getDate().equals(searchDate))
                        .toList();
            } else if (!searchName.isEmpty()) {
                events = eventDAO.lireParNom(searchName);
            } else if (searchDate != null) {
                events = eventDAO.lireParDate(searchDate);
            } else {
                showError("Erreur de recherche", "Veuillez entrer un nom ou une date.");
                return;
            }

            if (!events.isEmpty()) {
                eventData.clear();
                eventData.addAll(events);
            } else {
                showError("Aucun résultat", "Aucun événement trouvé avec ces critères.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de recherche", "Une erreur est survenue lors de la recherche.");
        }
    }

    @FXML
    public void afficherTousEvents() {
        try {
            loadEvents();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger tous les événements.");
        }
    }

    private void loadEvents() {
        try {
            eventData.clear();
            eventData.addAll(eventDAO.lireTous());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger les événements.");
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