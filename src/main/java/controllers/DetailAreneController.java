package controllers;

import dao.MatchDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Arene;

import java.sql.SQLException;

public class DetailAreneController {

    @FXML private Label nameLabel;
    @FXML private Label locationLabel;
    @FXML private Label capacityLabel;
    @FXML private Label statusLabel;

    private MatchDAO matchDAO;

    @FXML
    public void initialize() {
        try {
            matchDAO = new MatchDAO();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données.");
            e.printStackTrace();
        }
    }

    public void setArene(Arene arene) {
        nameLabel.setText(arene.getName());
        locationLabel.setText(arene.getLocation());
        capacityLabel.setText(String.valueOf(arene.getCapacity()));
        boolean isAvailable = matchDAO.isArenaAvailable(arene.getAreneId());
        statusLabel.setText(isAvailable ? "Disponible" : "Occupée");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
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