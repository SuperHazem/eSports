package controllers;

import dao.*;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import models.*;
import utils.UserSession;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SpectatorEventController {
    @FXML private TableView<EventSocial> eventTable;
    @FXML private TableColumn<EventSocial, String> nomColumn;
    @FXML private TableColumn<EventSocial, LocalDate> dateColumn;
    @FXML private TableColumn<EventSocial, String> lieuColumn;
    @FXML private TableColumn<EventSocial, String> descriptionColumn;
    @FXML private TableColumn<EventSocial, String> participantsColumn;
    @FXML private TableColumn<EventSocial, Void> actionsColumn;
    @FXML private TextField searchNameField;
    @FXML private DatePicker searchDateField;
    @FXML private StackPane notifyArea;
    @FXML private Label notifyLabel;

    private EventSocialDAO eventDAO;
    private ParticipationEventDAO pDAO;
    private ObservableList<EventSocial> eventData = FXCollections.observableArrayList();
    private Utilisateur currentUser;

    public SpectatorEventController() {
        eventDAO = new EventSocialDAOImpl();
        pDAO = new ParticipationEventDAOImpl();
    }

    @FXML
    public void initialize() {
        // Get current user from session
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            currentUser = session.getCurrentUser();
        }

        // Initialize table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Custom cell factory for participants column
        participantsColumn.setCellValueFactory(cellData -> {
            EventSocial event = cellData.getValue();
            int currentParticipants = pDAO.nombreParticipants(event);
            return new SimpleStringProperty(currentParticipants + "/" + event.getCapacite());
        });

        // Add action buttons column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button participerButton = new Button("Participer");

            {
                participerButton.getStyleClass().add("btn-participate");

                participerButton.setOnAction(event -> {
                    EventSocial selectedEvent = getTableView().getItems().get(getIndex());
                    joinEvent(selectedEvent);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EventSocial event = getTableView().getItems().get(getIndex());
                    boolean isParticipating = currentUser != null &&
                            pDAO.existeParticipation(currentUser, event);

                    if (isParticipating) {
                        Label participatingLabel = new Label("Inscrit");
                        participatingLabel.getStyleClass().add("joined");
                        setGraphic(participatingLabel);
                    } else {
                        setGraphic(participerButton);
                    }
                }
            }
        });

        // Load all events into the table
        loadEvents();
        eventTable.setItems(eventData);
    }

    private void loadEvents() {
        try {
            eventData.clear();
            eventData.addAll(eventDAO.lireTous());
        } catch (Exception e) {
            System.err.println("Error loading events: " + e.getMessage());
            e.printStackTrace();
            showNotification("Impossible de charger les événements.", false);
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
                showNotification("Veuillez entrer un nom ou une date pour la recherche.", false);
                return;
            }

            if (!events.isEmpty()) {
                eventData.clear();
                eventData.addAll(events);
            } else {
                showNotification("Aucun événement trouvé avec ces critères.", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Une erreur est survenue lors de la recherche.", false);
        }
    }

    @FXML
    public void afficherTousEvents() {
        searchNameField.clear();
        searchDateField.setValue(null);
        loadEvents();
    }

    private void joinEvent(EventSocial event) {
        if (currentUser == null) {
            showNotification("Vous devez être connecté pour participer à un événement.", false);
            return;
        }

        try {
            // Check if user is already participating
            if (pDAO.existeParticipation(currentUser, event)) {
                showNotification("Vous participez déjà à cet événement.", false);
                return;
            }

            // Check if event is full
            int currentParticipants = pDAO.nombreParticipants(event);
            if (currentParticipants >= event.getCapacite()) {
                showNotification("Désolé, cet événement est complet.", false);
                return;
            }

            // Create and save participation
            ParticipationEvent participation = new ParticipationEvent(currentUser, event);
            pDAO.ajouter(participation);

            // Refresh the table to update participation status
            eventTable.refresh();

            showNotification("Votre participation à l'événement \"" + event.getNom() + "\" a été enregistrée avec succès!", true);
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Une erreur est survenue lors de l'enregistrement de votre participation.", false);
        }
    }

    private void showNotification(String message, boolean isSuccess) {
        notifyLabel.setText(message);

        if (isSuccess) {
            notifyLabel.setTextFill(Color.WHITE);
            notifyArea.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 5;");
        } else {
            notifyLabel.setTextFill(Color.WHITE);
            notifyArea.setStyle("-fx-background-color: #F44336; -fx-background-radius: 5;");
        }

        // Make notification visible
        notifyArea.setVisible(true);
        notifyArea.setManaged(true);
        notifyArea.setOpacity(1);

        // Create fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(5), notifyArea);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            notifyArea.setVisible(false);
            notifyArea.setManaged(false);
            notifyArea.setOpacity(1.0); // Reset opacity for next use
        });

        fadeOut.play();
    }
}