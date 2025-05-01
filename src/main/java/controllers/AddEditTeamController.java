// AddEditTeamController.java
package controllers;

import dao.EquipeDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Equipe;

import java.sql.SQLException;
import java.util.ArrayList;

public class AddEditTeamController {

    public enum Mode {
        ADD,
        EDIT
    }

    @FXML private Label popupTitle;
    @FXML private TextField teamNameField;
    @FXML private TextField coachIdField;
    @FXML private Button cancelButton;
    @FXML private Button calculateWinRateButton;
    @FXML private Button saveButton;

    private Mode currentMode = Mode.ADD;
    private Equipe currentTeam;
    private GestionEquipeController parentController;
    private EquipeDAO equipeDAO;

    @FXML
    public void initialize() {
        try {
            equipeDAO = new EquipeDAO();
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        updateUI();
    }

    public void setTeam(Equipe team) {
        this.currentTeam = team;
        populateFields();
    }

    public void setParentController(GestionEquipeController controller) {
        this.parentController = controller;
    }

    private void updateUI() {
        if (currentMode == Mode.ADD) {
            popupTitle.setText("Ajouter Équipe");
            saveButton.setText("Enregistrer");
        } else {
            popupTitle.setText("Modifier Équipe");
            saveButton.setText("Mettre à jour");
        }
    }

    private void populateFields() {
        if (currentTeam != null) {
            teamNameField.setText(currentTeam.getNom());
            coachIdField.setText(String.valueOf(currentTeam.getCoachId()));
        }
    }

    @FXML
    private void handleCancel() {
        closePopup();
    }

    @FXML
    private void handleCalculateWinRate() {
        try {
            // Get player list from the current team
            if (currentTeam != null && !currentTeam.getListeJoueurs().isEmpty()) {
                double winRate = equipeDAO.calculerWinRateEquipe(currentTeam.getListeJoueurs());
                showInfo("Win Rate Calculation",
                        "Calculated Win Rate: " + String.format("%.2f%%", winRate * 100));
            } else {
                showError("Calculation Error", "No players in the team to calculate win rate.");
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to calculate win rate: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            String teamName = teamNameField.getText().trim();
            int coachId = Integer.parseInt(coachIdField.getText().trim());

            if (currentMode == Mode.ADD) {
                // Create a new team with empty player list
                Equipe newTeam = new Equipe(0, teamName, coachId, new ArrayList<>(), 0.0);
                equipeDAO.ajouter(newTeam);
                showInfo("Success", "Team added successfully!");
            } else {
                // Update existing team
                currentTeam.setNom(teamName);
                currentTeam.setCoachId(coachId);
                equipeDAO.modifier(currentTeam);
                showInfo("Success", "Team updated successfully!");
            }

            // Refresh the team list in the parent controller
            if (parentController != null) {
                parentController.refreshTeamList();
            }

            closePopup();

        } catch (NumberFormatException e) {
            showError("Input Error", "Coach ID must be a valid number.");
        } catch (SQLException e) {
            showError("Database Error", "Failed to save team: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Validation Error", e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (teamNameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Team name cannot be empty.");
            teamNameField.requestFocus();
            return false;
        }

        if (coachIdField.getText().trim().isEmpty()) {
            showError("Validation Error", "Coach ID cannot be empty.");
            coachIdField.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(coachIdField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Validation Error", "Coach ID must be a valid number.");
            coachIdField.requestFocus();
            return false;
        }

        return true;
    }

    private void closePopup() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}