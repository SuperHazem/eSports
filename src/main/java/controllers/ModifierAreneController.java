package controllers;

import dao.AreneDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Arene;
import utils.WindowManager;

import java.sql.SQLException;

public class ModifierAreneController {

    @FXML private Label idLabel;
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField capacityField;

    private Arene arene;
    private AreneDAO areneDAO;
    private Runnable onAreneUpdated;

    @FXML
    public void initialize() {
        try {
            areneDAO = new AreneDAO();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données.");
            e.printStackTrace();
        }
    }

    public void setArene(Arene arene) {
        this.arene = arene;
        idLabel.setText(String.valueOf(arene.getAreneId()));
        nameField.setText(arene.getName());
        locationField.setText(arene.getLocation());
        capacityField.setText(String.valueOf(arene.getCapacity()));
    }

    public void setOnAreneUpdated(Runnable callback) {
        this.onAreneUpdated = callback;
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();
            String capacityText = capacityField.getText().trim();

            if (name.isEmpty() || location.isEmpty() || capacityText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            int capacity = Integer.parseInt(capacityText);
            if (capacity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "La capacité doit être positive.");
                return;
            }

            arene.setName(name);
            arene.setLocation(location);
            arene.setCapacity(capacity);

            areneDAO.modifier(arene);

            if (onAreneUpdated != null) {
                onAreneUpdated.run();
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'arène a été modifiée avec succès.");
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "La capacité doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification de l'arène.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        WindowManager.getInstance().closeCurrentWindow();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}