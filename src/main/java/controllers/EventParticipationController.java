package controllers;

import dao.EventSocialDAO;
import dao.EventSocialDAOImpl;
import dao.ParticipationEventDAO;
import dao.ParticipationEventDAOImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.EventSocial;
import models.ParticipationEvent;
import models.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EventParticipationController {

    @FXML private TableView<EventSocial> eventTable;
    @FXML private TableColumn<EventSocial, String> nomColumn;
    @FXML private TableColumn<EventSocial, LocalDate> dateColumn;
    @FXML private TableColumn<EventSocial, String> lieuColumn;
    @FXML private TableColumn<EventSocial, String> descriptionColumn;
    @FXML private TableColumn<EventSocial, String> capaciteColumn;
    @FXML private TableColumn<EventSocial, String> participantsColumn;
    @FXML private TableColumn<EventSocial, Void> actionsColumn;

    @FXML private TextField searchNameField;
    @FXML private DatePicker searchDateField;

    private final EventSocialDAO eventDAO;
    private final ParticipationEventDAO participationDAO;
    private final ObservableList<EventSocial> eventData = FXCollections.observableArrayList();
    private Utilisateur currentUser;

    public EventParticipationController() throws SQLException {
        this.eventDAO = new EventSocialDAOImpl();
        this.participationDAO = new ParticipationEventDAOImpl();
        
        // Create a test user for development
        this.currentUser = new Utilisateur();
        this.currentUser.setId(1);
        this.currentUser.setNom("Test");
        this.currentUser.setPrenom("User");
        this.currentUser.setEmail("test@test.com");
    }

    public void setCurrentUser(Utilisateur user) {
        if (user != null) {
            this.currentUser = user;
        }
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        
        // Custom cell factory for participants column
        participantsColumn.setCellValueFactory(cellData -> {
            EventSocial event = cellData.getValue();
            int currentParticipants = participationDAO.nombreParticipants(event);
            return new SimpleStringProperty(currentParticipants + " / " + event.getCapacite());
        });

        // Add action buttons column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button participerButton = new Button("Participer");
            private final Button annulerButton = new Button("Annuler");

            {
                participerButton.getStyleClass().add("button-participate");
                annulerButton.getStyleClass().add("button-cancel");

                participerButton.setOnAction(event -> {
                    EventSocial selectedEvent = getTableView().getItems().get(getIndex());
                    participerEvent(selectedEvent);
                });

                annulerButton.setOnAction(event -> {
                    EventSocial selectedEvent = getTableView().getItems().get(getIndex());
                    annulerParticipation(selectedEvent);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EventSocial event = getTableView().getItems().get(getIndex());
                    boolean isParticipating = participationDAO.existeParticipation(currentUser, event);
                    
                    HBox hbox = new HBox();
                    if (isParticipating) {
                        hbox.getChildren().add(annulerButton);
                    } else {
                        hbox.getChildren().add(participerButton);
                    }
                    hbox.setSpacing(10);
                    setGraphic(hbox);
                }
            }
        });

        // Load all events into the table
        loadEvents();
        eventTable.setItems(eventData);
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

    private void participerEvent(EventSocial event) {
        if (currentUser == null) {
            showError("Erreur", "Vous devez être connecté pour participer à un événement.");
            return;
        }

        try {
            // Check if user is already participating
            if (participationDAO.existeParticipation(currentUser, event)) {
                showError("Erreur", "Vous participez déjà à cet événement.");
                return;
            }

            // Check if event is full
            int currentParticipants = participationDAO.nombreParticipants(event);
            if (currentParticipants >= event.getCapacite()) {
                showError("Erreur", "Désolé, cet événement est complet.");
                return;
            }

            // Create and save participation
            ParticipationEvent participation = new ParticipationEvent(currentUser, event);
            participationDAO.ajouter(participation);

            // Refresh the table
            loadEvents();

            showSuccess("Succès", "Votre participation a été enregistrée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'enregistrement de votre participation.");
        }
    }

    private void annulerParticipation(EventSocial event) {
        if (currentUser == null) {
            showError("Erreur", "Vous devez être connecté pour annuler votre participation.");
            return;
        }

        try {
            // Find the participation
            List<ParticipationEvent> participations = participationDAO.lireParUtilisateur(currentUser);
            ParticipationEvent participation = participations.stream()
                    .filter(p -> p.getEvent().getId().equals(event.getId()))
                    .findFirst()
                    .orElse(null);

            if (participation == null) {
                showError("Erreur", "Vous ne participez pas à cet événement.");
                return;
            }

            // Delete the participation
            participationDAO.supprimer(participation.getId());

            // Refresh the table
            loadEvents();

            showSuccess("Succès", "Votre participation a été annulée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'annulation de votre participation.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 