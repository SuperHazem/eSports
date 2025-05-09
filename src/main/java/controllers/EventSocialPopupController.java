package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.EventSocial;
import utils.validators.EventSocialValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventSocialPopupController {

    @FXML private TextField nomField;
    @FXML private DatePicker dateField;
    @FXML private ComboBox<String> gouvernoratComboBox;
    @FXML private ComboBox<String> villeComboBox;
    @FXML private TextArea descriptionField;
    @FXML private TextField capaciteField;
    @FXML private Label titleLabel;

    private EventSocial event;
    private boolean isEditMode = false;

    public void initialize() {
        // Map des gouvernorats et leurs villes
        Map<String, List<String>> gouvernoratsVilles = new HashMap<>();
        
        // Gouvernorat de l'Ariana
        gouvernoratsVilles.put("Ariana", Arrays.asList(
            "Ariana Ville",
            "Ettadhamen",
            "Mnihla",
            "Raoued",
            "Sidi Thabet",
            "La Soukra",
            "Kalaat El Andalous",
            "Borj El Amri"
        ));
        
        // Gouvernorat de Tunis
        gouvernoratsVilles.put("Tunis", Arrays.asList(
            "Tunis Ville",
            "Bab El Bhar",
            "Bab Souika",
            "Carthage",
            "La Goulette",
            "Le Bardo",
            "La Marsa",
            "Sidi Bou Said",
            "Sidi Hassine"
        ));
        
        // Gouvernorat de Ben Arous
        gouvernoratsVilles.put("Ben Arous", Arrays.asList(
            "Ben Arous Ville",
            "El Mourouj",
            "Hammam Lif",
            "Hammam Chott",
            "Mégrine",
            "Mohamedia",
            "Mornag",
            "Radès"
        ));
        
        // Gouvernorat de Manouba
        gouvernoratsVilles.put("Manouba", Arrays.asList(
            "Manouba Ville",
            "Borj El Amri",
            "Den Den",
            "Douar Hicher",
            "El Battan",
            "Jdaida",
            "Mornaguia",
            "Oued Ellil",
            "Tebourba"
        ));
        
        // Gouvernorat de Nabeul
        gouvernoratsVilles.put("Nabeul", Arrays.asList(
            "Nabeul Ville",
            "Béni Khiar",
            "Béni Mtir",
            "Bou Argoub",
            "Dar Chaabane",
            "El Haouaria",
            "El Mida",
            "Grombalia",
            "Hammamet",
            "Kélibia",
            "Korba",
            "Menzel Bouzelfa",
            "Menzel Temime",
            "Soliman",
            "Takelsa"
        ));

        // Ajouter les gouvernorats au ComboBox
        gouvernoratComboBox.setItems(FXCollections.observableArrayList(gouvernoratsVilles.keySet()));
        
        // Écouter les changements de sélection du gouvernorat
        gouvernoratComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                villeComboBox.setItems(FXCollections.observableArrayList(gouvernoratsVilles.get(newVal)));
                villeComboBox.setValue(null); // Réinitialiser la sélection de la ville
            } else {
                villeComboBox.setItems(FXCollections.observableArrayList());
            }
        });
    }

    public void setEvent(EventSocial event) {
        this.event = event;
        this.isEditMode = true;
        populateFields();
        titleLabel.setText("Modifier Événement");
    }

    private void populateFields() {
        if (event != null) {
            nomField.setText(event.getNom());
            dateField.setValue(event.getDate());
            
            // Pour le lieu, on doit séparer le gouvernorat et la ville
            String lieu = event.getLieu();
            if (lieu != null && lieu.contains(" - ")) {
                String[] parts = lieu.split(" - ");
                gouvernoratComboBox.setValue(parts[0]);
                villeComboBox.setValue(parts[1]);
            }
            
            descriptionField.setText(event.getDescription());
            capaciteField.setText(String.valueOf(event.getCapacite()));
        }
    }

    @FXML
    public void enregistrer() {
        String nom = nomField.getText().trim();
        LocalDate date = dateField.getValue();
        String gouvernorat = gouvernoratComboBox.getValue();
        String ville = villeComboBox.getValue();
        String description = descriptionField.getText().trim();
        String capaciteStr = capaciteField.getText().trim();

        // Créer un objet temporaire pour la validation
        EventSocial tempEvent = new EventSocial();
        tempEvent.setNom(nom);
        tempEvent.setDate(date);
        tempEvent.setLieu(gouvernorat != null && ville != null ? gouvernorat + " - " + ville : null);
        tempEvent.setDescription(description);
        
        try {
            int capacite = Integer.parseInt(capaciteStr);
            tempEvent.setCapacite(capacite);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La capacité doit être un nombre valide.");
            return;
        }

        // Valider l'événement
        List<String> errors = EventSocialValidator.validateEvent(tempEvent);
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        // Si la validation est réussie, procéder à l'enregistrement
        if (isEditMode) {
            event.setNom(nom);
            event.setDate(date);
            event.setLieu(gouvernorat + " - " + ville);
            event.setDescription(description);
            event.setCapacite(tempEvent.getCapacite());
        } else {
            event = new EventSocial(nom, date, gouvernorat + " - " + ville, description, tempEvent.getCapacite());
        }

        closeStage();
    }

    private void showValidationErrors(List<String> errors) {
        StringBuilder message = new StringBuilder("Veuillez corriger les erreurs suivantes :\n\n");
        for (String error : errors) {
            message.append("• ").append(error).append("\n");
        }
        showAlert("Erreurs de validation", message.toString());
    }

    @FXML
    public void annuler() {
        event = null;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public EventSocial getEvent() {
        return event;
    }
} 