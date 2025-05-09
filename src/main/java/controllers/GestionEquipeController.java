// GestionEquipeController.java
package controllers;

import dao.EquipeDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import models.Equipe;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GestionEquipeController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button showAllButton;
    @FXML private TableView<Equipe> teamTable;
    @FXML private TableColumn<Equipe, Integer> idColumn;
    @FXML private TableColumn<Equipe, String> nameColumn;
    @FXML private TableColumn<Equipe, String> coachColumn;
    @FXML private TableColumn<Equipe, String> playersColumn;
    @FXML private TableColumn<Equipe, Double> winRateColumn;
    @FXML private TableColumn<Equipe, Void> actionsColumn;
    @FXML private Button addTeamButton;

    private EquipeDAO equipeDAO;
    private ObservableList<Equipe> teamList = FXCollections.observableArrayList();

    // GestionEquipeController.java - corrected initialize method

    @FXML
    public void initialize() {
        try {
            // Initialize DAO
            equipeDAO = new EquipeDAO();

            // Configure table columns
            idColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));

            nameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNom()));

            coachColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getCoachId())));

            playersColumn.setCellValueFactory(cellData -> {
                List<Integer> playerIds = cellData.getValue().getListeJoueurs();
                String playersList = playerIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                return new SimpleStringProperty(playersList);
            });

            winRateColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getWinRate()));

            // Use progress bar for win rate
            winRateColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

            // Configure actions column with buttons
            actionsColumn.setCellFactory(createActionsColumnCellFactory());

            // Load all teams
            loadAllTeams();

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to connect to database", e.getMessage());
        }
    }

    private Callback<TableColumn<Equipe, Void>, TableCell<Equipe, Void>> createActionsColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Equipe, Void> call(TableColumn<Equipe, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");
                    private final Button editPlayersBtn = new Button("Éditer Liste Joueurs");
                    private final HBox pane = new HBox(5, editBtn, editPlayersBtn, deleteBtn);

                    {
                        // Configure button styles
                        editBtn.getStyleClass().add("edit-button");
                        deleteBtn.getStyleClass().add("delete-button");
                        editPlayersBtn.getStyleClass().add("player-list-button");

                        // Configure button actions
                        editBtn.setOnAction(event -> {
                            Equipe equipe = getTableView().getItems().get(getIndex());
                            handleEditTeam(equipe);
                        });

                        deleteBtn.setOnAction(event -> {
                            Equipe equipe = getTableView().getItems().get(getIndex());
                            handleDeleteTeam(equipe);
                        });

                        editPlayersBtn.setOnAction(event -> {
                            Equipe equipe = getTableView().getItems().get(getIndex());
                            handleEditPlayerList(equipe);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showErrorAlert("Search Error", "Search field is empty", "Please enter a team name to search.");
            return;
        }

        // Filter teams by name (case-insensitive)
        ObservableList<Equipe> filteredList = teamList.filtered(
                equipe -> equipe.getNom().toLowerCase().contains(searchTerm.toLowerCase())
        );

        teamTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showInfoAlert("Search Result", "No teams found",
                    "No teams matching '" + searchTerm + "' were found.");
        }
    }

    @FXML
    private void handleShowAll() {
        loadAllTeams();
    }

    @FXML
    private void handleAddTeam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddEditTeamPopup.fxml"));
            Parent root = loader.load();

            AddEditTeamController controller = loader.getController();
            controller.setMode(AddEditTeamController.Mode.ADD);
            controller.setParentController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            showErrorAlert("UI Error", "Failed to open add team dialog", e.getMessage());
        }
    }

    private void handleEditTeam(Equipe equipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddEditTeamPopup.fxml"));
            Parent root = loader.load();

            AddEditTeamController controller = loader.getController();
            controller.setMode(AddEditTeamController.Mode.EDIT);
            controller.setTeam(equipe);
            controller.setParentController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            showErrorAlert("UI Error", "Failed to open edit team dialog", e.getMessage());
        }
    }

    private void handleDeleteTeam(Equipe equipe) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Team");
        confirmDialog.setContentText("Are you sure you want to delete the team '" + equipe.getNom() + "'?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                equipeDAO.supprimer(equipe.getId());
                teamList.remove(equipe);
                showInfoAlert("Success", "Team Deleted",
                        "The team '" + equipe.getNom() + "' has been deleted successfully.");
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete team", e.getMessage());
            }
        }
    }

    private void handleEditPlayerList(Equipe equipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EditPlayerListPopup.fxml"));
            Parent root = loader.load();

            EditPlayerListController controller = loader.getController();
            controller.setTeam(equipe);
            controller.setParentController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            showErrorAlert("UI Error", "Failed to open edit player list dialog", e.getMessage());
        }
    }

    public void loadAllTeams() {
        try {
            // In a real application, you would fetch all teams from the database
            // For now, we'll create some sample data
            teamList.clear();

            // Sample data (in a real app, this would come from the database)
            List<Integer> players1 = List.of(1, 2, 3, 4, 5);
            List<Integer> players2 = List.of(6, 7, 8, 9, 10);
            List<Integer> players3 = List.of(11, 12, 13, 14, 15);

            teamList.add(new Equipe(1, "Team Alpha", 101, players1, 0.75));
            teamList.add(new Equipe(2, "Team Beta", 102, players2, 0.62));
            teamList.add(new Equipe(3, "Team Gamma", 103, players3, 0.88));

            teamTable.setItems(teamList);

        } catch (Exception e) {
            showErrorAlert("Data Error", "Failed to load teams", e.getMessage());
        }
    }

    public void refreshTeamList() {
        loadAllTeams();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}