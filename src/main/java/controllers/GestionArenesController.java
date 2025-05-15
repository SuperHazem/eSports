package controllers;

import dao.AreneDAO;
import dao.MatchDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Arene;
import utils.WindowManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GestionArenesController {

    @FXML private TextField nameFilterField;
    @FXML private TextField locationFilterField;
    @FXML private TextField capacityMinField;
    @FXML private TextField capacityMaxField;
    @FXML private TableView<Arene> arenaTable;
    @FXML private TableColumn<Arene, String> nameColumn;
    @FXML private TableColumn<Arene, String> locationColumn;
    @FXML private TableColumn<Arene, Integer> capacityColumn;
    @FXML private TableColumn<Arene, String> statusColumn;
    @FXML private TableColumn<Arene, Void> actionsColumn;
    @FXML private Label statusLabel;

    private AreneDAO areneDAO;
    private MatchDAO matchDAO;
    private List<Arene> allArenas;
    private FilteredList<Arene> filteredArenas;

    private Predicate<Arene> namePredicate = arene -> true;
    private Predicate<Arene> locationPredicate = arene -> true;
    private Predicate<Arene> capacityPredicate = arene -> true;

    @FXML
    public void initialize() {
        try {
            areneDAO = new AreneDAO();
            matchDAO = new MatchDAO();

            configureTableColumns();
            setupActionsColumn();
            loadAllArenas();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureTableColumns() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        locationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        capacityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCapacity()).asObject());
        statusColumn.setCellValueFactory(cellData -> {
            boolean isAvailable = matchDAO.isArenaAvailable(cellData.getValue().getAreneId());
            return new SimpleStringProperty(isAvailable ? "Disponible" : "Occupée");
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);
    
            {
                // Apply the same styling as in GestionMatchController
                editBtn.getStyleClass().add("btn-primary");
                editBtn.getStyleClass().add("btn-sm");
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.getStyleClass().add("btn-sm");
    
                editBtn.setOnAction(event -> {
                    Arene arene = getTableView().getItems().get(getIndex());
                    editArene(arene);
                });
    
                deleteBtn.setOnAction(event -> {
                    Arene arene = getTableView().getItems().get(getIndex());
                    deleteArene(arene);
                });
            }
    
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadAllArenas() {
        allArenas = areneDAO.lireTous();
        filteredArenas = new FilteredList<>(FXCollections.observableArrayList(allArenas));
        arenaTable.setItems(filteredArenas);
        updateStatusLabel();
    }

    @FXML
    private void handleNameFilter() {
        String nameFilter = nameFilterField.getText().toLowerCase().trim();
        if (nameFilter.isEmpty()) {
            namePredicate = arene -> true;
        } else {
            namePredicate = arene -> arene.getName().toLowerCase().contains(nameFilter);
        }
        applyFilters();
    }

    @FXML
    private void handleLocationFilter() {
        String locationFilter = locationFilterField.getText().toLowerCase().trim();
        if (locationFilter.isEmpty()) {
            locationPredicate = arene -> true;
        } else {
            locationPredicate = arene -> arene.getLocation().toLowerCase().contains(locationFilter);
        }
        applyFilters();
    }

    @FXML
    private void handleCapacityFilter() {
        String minCapText = capacityMinField.getText().trim();
        String maxCapText = capacityMaxField.getText().trim();

        try {
            Integer minCap = minCapText.isEmpty() ? null : Integer.parseInt(minCapText);
            Integer maxCap = maxCapText.isEmpty() ? null : Integer.parseInt(maxCapText);

            capacityPredicate = arene -> {
                int capacity = arene.getCapacity();
                boolean minValid = minCap == null || capacity >= minCap;
                boolean maxValid = maxCap == null || capacity <= maxCap;
                return minValid && maxValid;
            };
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Entrée invalide", "Les capacités doivent être des nombres valides.");
            capacityPredicate = arene -> true;
        }
        applyFilters();
    }

    private void applyFilters() {
        filteredArenas.setPredicate(namePredicate.and(locationPredicate).and(capacityPredicate));
        updateStatusLabel();
    }

    @FXML
    private void handleReset() {
        nameFilterField.clear();
        locationFilterField.clear();
        capacityMinField.clear();
        capacityMaxField.clear();

        namePredicate = arene -> true;
        locationPredicate = arene -> true;
        capacityPredicate = arene -> true;

        applyFilters();
        statusLabel.setText("Filtres réinitialisés. Affichage de toutes les arènes.");
    }

    @FXML
    private void handleRefresh() {
        loadAllArenas();
        statusLabel.setText("Données actualisées.");
    }

    @FXML
    private void handleAjouterArene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterArene.fxml"));
            Parent root = loader.load();

            AjouterAreneController controller = loader.getController();
            controller.setOnAreneAdded(this::handleRefresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter une Arène");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre d'ajout d'arène: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les arènes en CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("arenes_export.csv");

        File file = fileChooser.showSaveDialog(arenaTable.getScene().getWindow());
        if (file != null) {
            exportToCSV(file, filteredArenas);
        }
    }

    private void exportToCSV(File file, List<Arene> arenes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Nom,Localisation,Capacité,Statut");
            for (Arene arene : arenes) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCSV(arene.getName())).append(",");
                line.append(escapeCSV(arene.getLocation())).append(",");
                line.append(arene.getCapacity()).append(",");
                boolean isAvailable = matchDAO.isArenaAvailable(arene.getAreneId());
                line.append(isAvailable ? "Disponible" : "Occupée");
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
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void viewArene(Arene arene) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailArene.fxml"));
            Parent root = loader.load();

            DetailAreneController controller = loader.getController();
            controller.setArene(arene);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Détails de l'Arène");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir les détails de l'arène: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editArene(Arene arene) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierArene.fxml"));
            Parent root = loader.load();

            ModifierAreneController controller = loader.getController();
            controller.setArene(arene);
            controller.setOnAreneUpdated(this::handleRefresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier l'Arène");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la modification de l'arène: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteArene(Arene arene) {
        if (areneDAO.hasAssociatedMatches(arene.getAreneId())) {
            showAlert(Alert.AlertType.ERROR, "Suppression impossible",
                    "Cette arène est associée à des matchs. Supprimez d'abord les matchs associés.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'arène");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'arène " + arene.getName() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                areneDAO.supprimer(arene.getAreneId());
                handleRefresh();
                statusLabel.setText("Arène supprimée avec succès.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                        "Impossible de supprimer l'arène: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateStatusLabel() {
        int totalArenas = allArenas.size();
        int displayedArenas = filteredArenas.size();

        if (totalArenas == displayedArenas) {
            statusLabel.setText("Affichage de toutes les arènes (" + totalArenas + ")");
        } else {
            statusLabel.setText("Affichage de " + displayedArenas + " sur " + totalArenas + " arènes");
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

}