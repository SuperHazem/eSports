package controllers;

import dao.AreneDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Arene;
import utils.StaticDataLoader;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AjouterAreneController {

    @FXML private ComboBox<String> countryComboBox;
    @FXML private ComboBox<String> arenaNameComboBox;
    @FXML private TextField capacityField;
    @FXML private Button cancelButton;

    private Map<String, List<String>> arenaMap;
    private AreneDAO areneDAO;
    private Runnable onAreneAdded;

    public void setOnAreneAdded(Runnable callback) {
        this.onAreneAdded = callback;
    }

    @FXML
    public void initialize() {
        try {
            areneDAO = new AreneDAO();
            arenaMap = StaticDataLoader.loadCountryArenaMap();

            if (arenaMap != null && !arenaMap.isEmpty()) {
                countryComboBox.setItems(FXCollections.observableArrayList(arenaMap.keySet()));
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec du chargement des données d'arène.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Impossible de se connecter à la base de données.");
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCountryChange() {
        String selectedCountry = countryComboBox.getValue();
        if (selectedCountry != null && arenaMap.containsKey(selectedCountry)) {
            arenaNameComboBox.setItems(FXCollections.observableArrayList(arenaMap.get(selectedCountry)));
        } else {
            arenaNameComboBox.getItems().clear();
        }
    }

    @FXML
    private void handleAjouterArene() {
        try {
            String name = arenaNameComboBox.getValue();
            String location = countryComboBox.getValue();
            String capacityText = capacityField.getText().trim();

            if (name == null || name.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Données manquantes", "Veuillez sélectionner une arène.");
                return;
            }
            if (location == null || location.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Données manquantes", "Veuillez sélectionner un pays.");
                return;
            }
            if (capacityText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Données manquantes", "Veuillez entrer une capacité.");
                return;
            }

            int capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                showAlert(Alert.AlertType.WARNING, "Entrée invalide", "La capacité doit être positive.");
                return;
            }

            Arene newArene = new Arene(0, name, capacity, location);
            areneDAO.ajouter(newArene);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Arène ajoutée avec succès !");
            if (onAreneAdded != null) {
                onAreneAdded.run();
            }
            closeStage();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Entrée invalide", "La capacité doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'arène : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}