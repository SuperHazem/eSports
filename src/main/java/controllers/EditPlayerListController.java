// EditPlayerListController.java
package controllers;

import dao.EquipeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import models.Equipe;
import models.Joueur;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditPlayerListController {

    @FXML private ListView<String> availablePlayersList;
    @FXML private ListView<String> selectedPlayersList;
    @FXML private Button addPlayerButton;
    @FXML private Button removePlayerButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Equipe currentTeam;
    private GestionEquipeController parentController;
    private EquipeDAO equipeDAO;

    // Sample data for available players (in a real app, this would come from the database)
    private List<Joueur> allPlayers = new ArrayList<>();
    private ObservableList<Integer> selectedPlayerIds = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            equipeDAO = new EquipeDAO();

            // Initialize sample player data
            initializeSamplePlayers();

            // Configure button states based on selection
            availablePlayersList.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> addPlayerButton.setDisable(newVal == null));

            selectedPlayersList.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> removePlayerButton.setDisable(newVal == null));

            // Initially disable buttons
            addPlayerButton.setDisable(true);
            removePlayerButton.setDisable(true);

        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    private void initializeSamplePlayers() {
        // In a real app, you would fetch this from the database
        for (int i = 1; i <= 20; i++) {
            allPlayers.add(new Joueur(i, "Player" + i, "Rank" + (i % 5 + 1), 0.5 + (i % 10) * 0.05));
        }
    }

    public void setTeam(Equipe team) {
        this.currentTeam = team;
        if (team != null) {
            selectedPlayerIds.addAll(team.getListeJoueurs());
            updatePlayerLists();
        }
    }

    public void setParentController(GestionEquipeController controller) {
        this.parentController = controller;
    }

    private void updatePlayerLists() {
        // Clear existing items
        availablePlayersList.getItems().clear();
        selectedPlayersList.getItems().clear();

        // Populate available players (excluding selected ones)
        List<String> availablePlayers = allPlayers.stream()
                .filter(player -> !selectedPlayerIds.contains(player.getId()))
                .map(player -> player.getId() + " - " + player.getPseudoJeu())
                .collect(Collectors.toList());
        availablePlayersList.getItems().addAll(availablePlayers);

        // Populate selected players
        List<String> selectedPlayers = allPlayers.stream()
                .filter(player -> selectedPlayerIds.contains(player.getId()))
                .map(player -> player.getId() + " - " + player.getPseudoJeu())
                .collect(Collectors.toList());
        selectedPlayersList.getItems().addAll(selectedPlayers);
    }

    @FXML
    private void handleAddPlayer() {
        String selectedItem = availablePlayersList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Extract player ID from the selected item (format: "ID - Name")
            int playerId = Integer.parseInt(selectedItem.split(" - ")[0]);

            // Add to selected players
            selectedPlayerIds.add(playerId);

            // Update lists
            updatePlayerLists();
        }
    }

    @FXML
    private void handleRemovePlayer() {
        String selectedItem = selectedPlayersList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Extract player ID from the selected item (format: "ID - Name")
            int playerId = Integer.parseInt(selectedItem.split(" - ")[0]);

            // Remove from selected players
            selectedPlayerIds.remove(Integer.valueOf(playerId));

            // Update lists
            updatePlayerLists();
        }
    }

    @FXML
    private void handleCancel() {
        closePopup();
    }

    @FXML
    private void handleSave() {
        try {
            // Update the team's player list
            currentTeam.setListeJoueurs(new ArrayList<>(selectedPlayerIds));

            // Recalculate win rate
            double winRate = equipeDAO.calculerWinRateEquipe(currentTeam.getListeJoueurs());
            currentTeam.setWinRate(winRate);

            // Update in database
            equipeDAO.modifier(currentTeam);

            // Refresh the team list in the parent controller
            if (parentController != null) {
                parentController.refreshTeamList();
            }

            showInfo("Success", "Player list updated successfully!");
            closePopup();

        } catch (SQLException e) {
            showError("Database Error", "Failed to save player list: " + e.getMessage());
        }
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