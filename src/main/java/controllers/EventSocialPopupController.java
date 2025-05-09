package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.EventSocial;

import java.time.LocalDate;

public class EventSocialPopupController {

    @FXML private TextField nomField;
    @FXML private DatePicker dateField;
    @FXML private TextField lieuField;
    @FXML private TextArea descriptionField;
    @FXML private TextField capaciteField;
    @FXML private Label titleLabel;

    private EventSocial event;
    private boolean isEditMode = false;

    public void initialize() {
        // Initialize any necessary components
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
            lieuField.setText(event.getLieu());
            descriptionField.setText(event.getDescription());
            capaciteField.setText(String.valueOf(event.getCapacite()));
        }
    }

    @FXML
    public void enregistrer() {
        String nom = nomField.getText().trim();
        LocalDate date = dateField.getValue();
        String lieu = lieuField.getText().trim();
        String description = descriptionField.getText().trim();
        String capaciteStr = capaciteField.getText().trim();

        if (nom.isEmpty() || date == null || lieu.isEmpty() || description.isEmpty() || capaciteStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            int capacite = Integer.parseInt(capaciteStr);

            if (isEditMode) {
                event.setNom(nom);
                event.setDate(date);
                event.setLieu(lieu);
                event.setDescription(description);
                event.setCapacite(capacite);
            } else {
                event = new EventSocial(nom, date, lieu, description, capacite);
            }

            closeStage();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La capacité doit être un nombre valide.");
        }
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