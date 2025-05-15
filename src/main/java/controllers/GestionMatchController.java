package controllers;

import dao.EquipeDAO;
import dao.MatchDAO;
import enums.StatutMatch;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Equipe;
import models.Match;
import utils.WindowManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GestionMatchController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> filtreStatutComboBox;
    @FXML private TextField equipeFilterField;
    @FXML private TextField tournoiFilterField;
    @FXML private TableView<Match> matchTable;
    @FXML private TableColumn<Match, String> dateColumn;
    @FXML private TableColumn<Match, String> equipesColumn;
    @FXML private TableColumn<Match, String> tournoiColumn;
    @FXML private TableColumn<Match, String> areneColumn;
    @FXML private TableColumn<Match, String> scoreColumn;
    @FXML private TableColumn<Match, String> vainqueurColumn;
    @FXML private TableColumn<Match, String> dureeColumn;
    @FXML private TableColumn<Match, String> nomJeuColumn;
    @FXML private TableColumn<Match, String> statutColumn;
    @FXML private TableColumn<Match, Void> actionsColumn;
    @FXML private Label statusLabel;

    private MatchDAO matchDAO;
    private List<Match> allMatches;
    private FilteredList<Match> filteredMatches;
    private Stage previousStage;
    
    public void setPreviousStage(Stage stage) {
        this.previousStage = stage;
    }

    // Current filter predicates
    private Predicate<Match> datePredicate = match -> true;
    private Predicate<Match> statutPredicate = match -> true;
    private Predicate<Match> equipePredicate = match -> true;
    private Predicate<Match> tournoiPredicate = match -> true;

    @FXML
    public void initialize() {
        try {
            matchDAO = new MatchDAO();

            // Initialize date picker
            datePicker.setValue(LocalDate.now());

            // Setup status filter combo box
            setupFilterComboBox();

            // Configure table columns
            configureTableColumns();

            // Setup actions column
            setupActionsColumn();

            // Load all matches
            loadAllMatches();

            // Set initial status
            updateStatusLabel();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFilterComboBox() {
        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("Tous les statuts");
        for (StatutMatch statut : StatutMatch.values()) {
            options.add(statut.toString());
        }
        filtreStatutComboBox.setItems(options);
        filtreStatutComboBox.setValue("Tous les statuts");
    }

    private void configureTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(dateFormat.format(cellData.getValue().getDateMatch()));
        });

        // Teams column
        equipesColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            return new SimpleStringProperty(match.getEquipe1Nom() + " vs " + match.getEquipe2Nom());
        });

        // Tournament column
        tournoiColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTournoiNom()));

        // Arena column
        areneColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAreneNom()));

        // Score column
        scoreColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            return new SimpleStringProperty(match.getScoreEquipe1() + " - " + match.getScoreEquipe2());
        });

        // Winner column
        vainqueurColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            if (match.getVainqueur() != null) {
                String vainqueurNom = match.getVainqueur().equals(match.getIdEquipe1()) ?
                        match.getEquipe1Nom() : match.getEquipe2Nom();
                return new SimpleStringProperty(vainqueurNom);
            }
            return new SimpleStringProperty("Non déterminé");
        });

        // Duration column
        dureeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDuree() != null ?
                        cellData.getValue().getDuree() + " min" : "N/A"));

        // Game name column
        nomJeuColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNomJeu() != null ?
                        cellData.getValue().getNomJeu() : "N/A"));

        // Status column
        statutColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatutMatch().toString()));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final Button terminerBtn = new Button("Terminer");
            private final HBox pane = new HBox(5, editBtn, deleteBtn, terminerBtn);
    
            {
                editBtn.getStyleClass().add("btn-primary");
                editBtn.getStyleClass().add("btn-sm");
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.getStyleClass().add("btn-sm");
                terminerBtn.getStyleClass().add("btn-success");
                terminerBtn.getStyleClass().add("btn-sm");
                
                editBtn.setOnAction(event -> {
                    Match match = getTableView().getItems().get(getIndex());
                    editMatch(match);
                });
                
                deleteBtn.setOnAction(event -> {
                    Match match = getTableView().getItems().get(getIndex());
                    // Check if match is EN_COURS before deletion
                    if (match.getStatutMatch() == StatutMatch.EN_COURS) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Impossible de supprimer un match en cours. Veuillez d'abord le terminer.");
                        return;
                    }
                    deleteMatch(match);
                });
                
                terminerBtn.setOnAction(event -> {
                    Match match = getTableView().getItems().get(getIndex());
                    terminerMatch(match);
                });
            }
    
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Match match = getTableView().getItems().get(getIndex());
                    // Only show terminer button for EN_COURS matches
                    terminerBtn.setVisible(match.getStatutMatch() == StatutMatch.EN_COURS);
                    setGraphic(pane);
                }
            }
        });
    }
    private void terminerMatch(Match match) {
        if (match.getStatutMatch() != StatutMatch.EN_COURS) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Seuls les matchs en cours peuvent être terminés.");
            return;
        }
        
        try {
            // Ouvrir une boîte de dialogue pour sélectionner le vainqueur
            Dialog<Equipe> dialog = new Dialog<>();
            dialog.setTitle("Terminer le match");
            dialog.setHeaderText("Sélectionnez le vainqueur du match");
            
            // Configurer les boutons
            ButtonType confirmerButtonType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmerButtonType, ButtonType.CANCEL);
            
            // Créer la mise en page
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Créer le ComboBox pour sélectionner le vainqueur
            ComboBox<Equipe> vainqueurComboBox = new ComboBox<>();
            
            // Récupérer les équipes du match
            EquipeDAO equipeDAO = new EquipeDAO();
            Equipe equipe1 = equipeDAO.lire(match.getIdEquipe1());
            Equipe equipe2 = equipeDAO.lire(match.getIdEquipe2());
            
            // Ajouter les équipes et l'option "Aucun vainqueur" au ComboBox
            ObservableList<Equipe> options = FXCollections.observableArrayList();
            options.add(null); // Option "Aucun vainqueur"
            options.add(equipe1);
            options.add(equipe2);
            vainqueurComboBox.setItems(options);
            
            // Configurer l'affichage des équipes dans le ComboBox
            vainqueurComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Equipe item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucun vainqueur");
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            vainqueurComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Equipe item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null) {
                        setText("Aucun vainqueur");
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
            // Sélectionner par défaut "Aucun vainqueur"
            vainqueurComboBox.setValue(null);
            
            // Ajouter les composants à la grille
            grid.add(new Label("Vainqueur:"), 0, 0);
            grid.add(vainqueurComboBox, 1, 0);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convertir le résultat du bouton en équipe sélectionnée
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmerButtonType) {
                    return vainqueurComboBox.getValue();
                }
                return null;
            });
            
            // Afficher la boîte de dialogue et traiter le résultat
            Optional<Equipe> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                
                // With:
                Timestamp matchDate = match.getDateMatch();
                Timestamp now = new Timestamp(System.currentTimeMillis());
                long diffInMillies = Math.abs(now.getTime() - matchDate.getTime());
                int durationInMinutes = (int) (diffInMillies / (60 * 1000));
                
                // Update match status and duration
                match.setStatutMatch(StatutMatch.TERMINE);
                match.setDuree(durationInMinutes);
                
                // Set the winner
                Equipe vainqueur = result.get();
                if (vainqueur != null) {
                    match.setVainqueur(vainqueur.getId());
                } else {
                    match.setVainqueur(null);
                }
                
                matchDAO.modifier(match);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le match a été terminé avec succès.");
                loadAllMatches();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la terminaison du match.");
            e.printStackTrace();
        }
    }
    private void loadAllMatches() {
        allMatches = matchDAO.lireTous();
        filteredMatches = new FilteredList<>(FXCollections.observableArrayList(allMatches));
        matchTable.setItems(filteredMatches);
        updateStatusLabel();
    }

    @FXML
    private void handleRechercher() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            datePredicate = match -> true;
        } else {
            // Old code with Date
            // Date sqlDate = Date.valueOf(selectedDate);
            // datePredicate = match -> {
            //     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //     return sdf.format(match.getDateMatch()).equals(sdf.format(sqlDate));
            // };
            
            // New code with Timestamp
            LocalDate localDate = selectedDate;
            datePredicate = match -> {
                LocalDate matchDate = match.getDateMatch().toLocalDateTime().toLocalDate();
                return matchDate.equals(localDate);
            };
        }

        applyFilters();
    }

    @FXML
    private void handleFiltrer() {
        String selectedStatut = filtreStatutComboBox.getValue();
        if (selectedStatut.equals("Tous les statuts")) {
            statutPredicate = match -> true;
        } else {
            StatutMatch statut = StatutMatch.valueOf(selectedStatut);
            statutPredicate = match -> match.getStatutMatch() == statut;
        }

        applyFilters();
    }

    @FXML
    private void handleEquipeFilter() {
        String equipeFilter = equipeFilterField.getText().toLowerCase().trim();
        if (equipeFilter.isEmpty()) {
            equipePredicate = match -> true;
        } else {
            equipePredicate = match ->
                    match.getEquipe1Nom().toLowerCase().contains(equipeFilter) ||
                            match.getEquipe2Nom().toLowerCase().contains(equipeFilter);
        }

        applyFilters();
    }

    @FXML
    private void handleTournoiFilter() {
        String tournoiFilter = tournoiFilterField.getText().toLowerCase().trim();
        if (tournoiFilter.isEmpty()) {
            tournoiPredicate = match -> true;
        } else {
            tournoiPredicate = match ->
                    match.getTournoiNom().toLowerCase().contains(tournoiFilter);
        }

        applyFilters();
    }

    private void applyFilters() {
        filteredMatches.setPredicate(datePredicate.and(statutPredicate).and(equipePredicate).and(tournoiPredicate));
        updateStatusLabel();
    }

    @FXML
    private void handleReset() {
        // Reset all filters
        datePicker.setValue(null);
        filtreStatutComboBox.setValue("Tous les statuts");
        equipeFilterField.clear();
        tournoiFilterField.clear();

        // Reset predicates
        datePredicate = match -> true;
        statutPredicate = match -> true;
        equipePredicate = match -> true;
        tournoiPredicate = match -> true;

        // Apply reset filters
        applyFilters();

        statusLabel.setText("Filtres réinitialisés. Affichage de tous les matchs.");
    }

    @FXML
    private void handleRefresh() {
        loadAllMatches();
        statusLabel.setText("Données actualisées.");
    }



    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les matchs en CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("matchs_export.csv");

        File file = fileChooser.showSaveDialog(matchTable.getScene().getWindow());
        if (file != null) {
            exportToCSV(file, filteredMatches);
        }
    }

    private void exportToCSV(File file, List<Match> matches) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write CSV header
            writer.println("Date,Équipe 1,Équipe 2,Score,Tournoi,Arène,Statut,Durée,Jeu,Vainqueur");

            // Write match data
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            for (Match match : matches) {
                StringBuilder line = new StringBuilder();
                line.append(dateFormat.format(match.getDateMatch())).append(",");
                line.append(escapeCSV(match.getEquipe1Nom())).append(",");
                line.append(escapeCSV(match.getEquipe2Nom())).append(",");
                line.append(match.getScoreEquipe1()).append("-").append(match.getScoreEquipe2()).append(",");
                line.append(escapeCSV(match.getTournoiNom())).append(",");
                line.append(escapeCSV(match.getAreneNom())).append(",");
                line.append(match.getStatutMatch()).append(",");
                line.append(match.getDuree() != null ? match.getDuree() + " min" : "N/A").append(",");
                line.append(escapeCSV(match.getNomJeu() != null ? match.getNomJeu() : "N/A")).append(",");

                // Determine winner
                String vainqueur = "Non déterminé";
                if (match.getVainqueur() != null) {
                    vainqueur = match.getVainqueur().equals(match.getIdEquipe1()) ?
                            match.getEquipe1Nom() : match.getEquipe2Nom();
                }
                line.append(escapeCSV(vainqueur));

                writer.println(line);
            }

            statusLabel.setText("Export CSV réussi: " + file.getAbsolutePath());

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export",
                    "Impossible d'exporter les données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }



    private void editMatch(Match match) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMatch.fxml"));
            Parent root = loader.load();

            ModifierMatchController controller = loader.getController();
            controller.setMatch(match);
            controller.setOnMatchUpdated(this::handleRefresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier le Match");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la modification du match: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteMatch(Match match) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le match");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce match ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                matchDAO.supprimer(match.getMatchId());
                handleRefresh();
                statusLabel.setText("Match supprimé avec succès.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                        "Impossible de supprimer le match: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateStatusLabel() {
        int totalMatches = allMatches.size();
        int displayedMatches = filteredMatches.size();

        if (totalMatches == displayedMatches) {
            statusLabel.setText("Affichage de tous les matchs (" + totalMatches + ")");
        } else {
            statusLabel.setText("Affichage de " + displayedMatches + " sur " + totalMatches + " matchs");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void handleRetourner() {
        WindowManager.getInstance().goBack();
    }



    @FXML
    private void handleAjouterMatch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMatch.fxml"));
            Parent root = loader.load();

            AjouterMatchController controller = loader.getController();
            controller.setOnMatchAdded(this::handleRefresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un Match");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre d'ajout de match: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
