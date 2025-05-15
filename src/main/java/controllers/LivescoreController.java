package controllers;

import enums.StatutMatch;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Match;
import services.LivescoreService;
import utils.LivescoreStyleUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LivescoreController {

    @FXML private VBox rootContainer;
    @FXML private Label titleLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Button refreshButton;
    @FXML private TableView<Match> matchesTable;
    @FXML private TableColumn<Match, String> gameColumn;
    @FXML private TableColumn<Match, String> teamsColumn;
    @FXML private TableColumn<Match, String> scoreColumn;
    @FXML private TableColumn<Match, String> tournamentColumn;
    @FXML private TableColumn<Match, String> statusColumn;

    private LivescoreService livescoreService;

    @FXML
    public void initialize() {
        try {
            // Initialize the livescore service
            livescoreService = new LivescoreService();

            // Configure table columns
            configureTableColumns();

            // Load and apply CSS stylesheet
            String cssPath = getClass().getResource("/styles/styles.css").toExternalForm();
            matchesTable.getStylesheets().add(cssPath);
            matchesTable.getStyleClass().add("livescore-table");

            // Apply styling to the table
            LivescoreStyleUtil.applyLivescoreTableStyles(matchesTable);

            // Set up listeners for updates and errors
            livescoreService.setMatchUpdateListener(this::updateLivescores);
            livescoreService.setErrorListener(this::handleError);

            // Start automatic updates every 30 seconds
            livescoreService.startLivescoreUpdates(30);


        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to initialize livescore: " + e.getMessage());
        }
    }

    private void configureTableColumns() {
        // Game column
        gameColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            if (match == null) return javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown");

            String gameName = match.getNomJeu();
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> gameName);
        });

        // Teams column
        teamsColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            if (match == null) return javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown vs Unknown");

            String team1 = match.getEquipe1Nom();
            String team2 = match.getEquipe2Nom();
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> team1 + " vs " + team2);
        });

        // Score column
        scoreColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            if (match == null) return javafx.beans.binding.Bindings.createStringBinding(() -> "0 - 0");

            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> match.getScoreEquipe1() + " - " + match.getScoreEquipe2());
        });
        scoreColumn.setCellFactory(column -> {
            return new TableCell<Match, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (getTableRow() != null && getTableRow().getItem() instanceof Match) {
                            Match match = (Match) getTableRow().getItem();
                            setStyle(LivescoreStyleUtil.getScoreStyle(match));
                        }
                    }
                }
            };
        });

        // Tournament column
        tournamentColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            if (match == null) return javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown");

            String tournamentName = match.getTournoiNom();
            return javafx.beans.binding.Bindings.createStringBinding(() -> tournamentName);
        });

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            if (match == null) return javafx.beans.binding.Bindings.createStringBinding(() -> "Unknown");

            StatutMatch status = match.getStatutMatch();
            if (status == null) status = StatutMatch.EN_ATTENTE;

            String statusText;
            switch (status) {
                case EN_COURS:
                    statusText = "LIVE";
                    break;
                case TERMINE:
                    statusText = "Terminé";
                    break;
                case EN_ATTENTE:
                    statusText = "À venir";
                    break;
                default:
                    statusText = "Unknown";
                    break;
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> statusText);
        });
        statusColumn.setCellFactory(column -> {
            return new TableCell<Match, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    getStyleClass().removeAll("status-live", "status-finished", "status-upcoming");

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        if (getTableRow() != null && getTableRow().getItem() instanceof Match) {
                            Match match = (Match) getTableRow().getItem();
                            StatutMatch status = match.getStatutMatch();
                            if (status == null) status = StatutMatch.EN_ATTENTE;

                            switch (status) {
                                case EN_COURS:
                                    getStyleClass().add("status-live");
                                    break;
                                case TERMINE:
                                    getStyleClass().add("status-finished");
                                    break;
                                case EN_ATTENTE:
                                    getStyleClass().add("status-upcoming");
                                    break;
                            }
                        }
                    }
                }
            };
        });
    }

    @FXML
    private void handleRefreshButton() {
        refreshButton.setDisable(true);
        livescoreService.fetchLivescoresAsync();
        // Re-enable the button after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> refreshButton.setDisable(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateLivescores(List<Match> matches) {
        matchesTable.setItems(FXCollections.observableArrayList(matches));

        // Update the last updated label
        if (livescoreService.getLastUpdated() != null) {
            Instant instant = Instant.parse(livescoreService.getLastUpdated());
            String formattedTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(instant);
            lastUpdatedLabel.setText("Dernière mise à jour: " + formattedTime);
        }

        refreshButton.setDisable(false);
    }

    private void handleError(Throwable error) {
        showAlert(Alert.AlertType.ERROR, "Error",
                "Failed to fetch livescores: " + error.getMessage());
        refreshButton.setDisable(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}