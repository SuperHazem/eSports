package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import models.EventSocial;
import utils.validators.EventSocialValidator;

import java.time.LocalDate;
import java.util.List;

public class EventSocialPopupController {

    @FXML private TextField nomField;
    @FXML private DatePicker datePicker;
    @FXML private TextField lieuField;
    @FXML private TextArea descriptionField;
    @FXML private TextField capaciteField;
    @FXML private Button saveButton;
    @FXML private Label titleLabel;
    @FXML private VBox mapContainer;
    @FXML private WebView mapView;

    private EventSocial event;
    private boolean isEditMode;

    @FXML
    public void initialize() {
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Initialize map
        initializeMap();
    }

    private void initializeMap() {
        try {
            // Simple OpenStreetMap implementation
            String mapHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Location Selector</title>
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css"/>
                    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
                    <style>
                        #map { height: 300px; width: 100%; }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        var map = L.map('map').setView([36.8065, 10.1815], 13);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            attribution: '© OpenStreetMap contributors'
                        }).addTo(map);
                        
                        var marker = null;
                        map.on('click', function(e) {
                            if (marker) {
                                map.removeLayer(marker);
                            }
                            marker = L.marker(e.latlng).addTo(map);
                            // Send coordinates to Java
                            window.location.href = 'java:updateLocation(' + e.latlng.lat + ',' + e.latlng.lng + ')';
                        });
                    </script>
                </body>
                </html>
                """;
            mapView.getEngine().loadContent(mapHtml);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de carte", "Impossible de charger la carte. Veuillez entrer l'adresse manuellement.");
        }
    }

    @FXML
    private void openMap() {
        try {
            mapContainer.setVisible(true);
            mapContainer.setManaged(true);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la carte. Veuillez entrer l'adresse manuellement.");
        }
    }

    @FXML
    private void closeMap() {
        mapContainer.setVisible(false);
        mapContainer.setManaged(false);
    }

    public void updateLocation(double lat, double lng) {
        try {
            // Simple reverse geocoding using OpenStreetMap Nominatim
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lng);
            String address = "Lat: " + lat + ", Lng: " + lng; // Fallback address
            lieuField.setText(address);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de récupérer l'adresse. Veuillez l'entrer manuellement.");
        }
    }

    public void setEvent(EventSocial event) {
        this.event = event;
        this.isEditMode = event != null;
        
        if (isEditMode) {
            titleLabel.setText("Modifier Événement Social");
            nomField.setText(event.getNom());
            datePicker.setValue(event.getDate());
            lieuField.setText(event.getLieu());
            descriptionField.setText(event.getDescription());
            capaciteField.setText(String.valueOf(event.getCapacite()));
        } else {
            titleLabel.setText("Ajouter Événement Social");
        }
    }

    public EventSocial getEvent() {
        return event;
    }

    @FXML
    private void save() {
        try {
            String nom = nomField.getText();
            LocalDate date = datePicker.getValue();
            String lieu = lieuField.getText();
            String description = descriptionField.getText();
            int capacite = Integer.parseInt(capaciteField.getText());

            EventSocial newEvent = new EventSocial(nom, date, lieu, description, capacite);
            
            // Validate the event
            List<String> errors = EventSocialValidator.validateEvent(newEvent);
            if (!errors.isEmpty()) {
                showError("Erreur de validation", String.join("\n", errors));
                return;
            }

            if (isEditMode) {
                newEvent.setId(event.getId());
            }
            
            this.event = newEvent;
            closeStage();
        } catch (NumberFormatException ex) {
            showError("Erreur", "La capacité doit être un nombre valide");
        }
    }

    @FXML
    public void annuler() {
        event = null;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 