package controllers;

import dao.EquipeDAO;
import dao.RecompenseDAOImpl;
import enums.TypeRecompense;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Equipe;
import models.Recompense;
import utils.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecompenseController {

    @FXML private TableView<Recompense> recompenseTable;
    @FXML private TableColumn<Recompense, String> equipeColumn;
    @FXML private TableColumn<Recompense, String> coachColumn;
    @FXML private TableColumn<Recompense, TypeRecompense> typeColumn;
    @FXML private TableColumn<Recompense, Double> valeurColumn;
    @FXML private TableColumn<Recompense, Void> actionsColumn;

    @FXML private ComboBox<String> typeFilterComboBox;
    @FXML private TextField searchField;
    @FXML private Button addButton;

    private RecompenseDAOImpl recompenseDAO;
    private EquipeDAO equipeDAO;
    private ObservableList<Recompense> recompenses;

    // Cache for coach names to avoid repeated database queries
    private Map<Integer, String> coachNameCache = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            System.out.println("Initializing RecompenseController...");

            // Test database connection
            testDatabaseConnection();

            // Initialize DAOs
            recompenseDAO = new RecompenseDAOImpl();
            equipeDAO = new EquipeDAO();

            // Preload coach names
            preloadCoachNames();

            // Setup table columns
            setupTableColumns();

            // Configure the table for responsive columns
            makeTableColumnsResponsive();

            // Initialize type filter
            initializeTypeFilter();

            // Load data
            loadTableData();

            System.out.println("RecompenseController initialized successfully.");

        } catch (Exception e) {
            System.err.println("Error initializing RecompenseController: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Une erreur est survenue lors de l'initialisation: " + e.getMessage());
        }
    }

    /**
     * Test the database connection and check if the Recompense table has data
     */
    private void testDatabaseConnection() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null) {
                System.out.println("Database connection is working!");

                // Test query to check if Recompense table exists and has data
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT COUNT(*) FROM Recompense");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Recompense table has " + count + " rows");
                }
                rs.close();

                // Test query to check if Equipe table exists and has data
                rs = stmt.executeQuery("SELECT COUNT(*) FROM Equipe");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Equipe table has " + count + " rows");
                }
            } else {
                System.err.println("Database connection is null!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
    }

    /**
     * Preload all coach names to avoid repeated database queries
     */
    private void preloadCoachNames() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT id, nom, prenom FROM Utilisateur WHERE role = 'COACH'";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                coachNameCache.put(id, prenom + " " + nom);
            }
            System.out.println("Preloaded " + coachNameCache.size() + " coach names");
        } catch (SQLException e) {
            System.err.println("Error preloading coach names: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }
    }

    private void setupTableColumns() {
        System.out.println("Setting up table columns...");

        // Equipe Column - Fixed to properly display team name
        equipeColumn.setCellValueFactory(cellData -> {
            Recompense recompense = cellData.getValue();
            if (recompense == null) {
                System.out.println("Warning: Null recompense in equipeColumn");
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }

            Equipe equipe = recompense.getEquipe();
            String equipeName = equipe != null ? equipe.getNom() : "N/A";
            System.out.println("Equipe for reward " + recompense.getId() + ": " + equipeName);
            return new javafx.beans.property.SimpleStringProperty(equipeName);
        });

        // Coach Column - Fixed to display coach name from cache
        coachColumn.setCellValueFactory(cellData -> {
            Recompense recompense = cellData.getValue();
            if (recompense == null) {
                System.out.println("Warning: Null recompense in coachColumn");
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }

            Equipe equipe = recompense.getEquipe();
            String coachName = getCoachNameFromCache(equipe);
            System.out.println("Coach for reward " + recompense.getId() + ": " + coachName);
            return new javafx.beans.property.SimpleStringProperty(coachName);
        });

        // Type Column
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setCellFactory(column -> new TableCell<Recompense, TypeRecompense>() {
            @Override
            protected void updateItem(TypeRecompense item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());

                    // Apply different styles based on type
                    switch (item) {
                        case PRIX:
                            setStyle("-fx-background-color: rgba(0, 247, 255, 0.2); -fx-text-fill: white; -fx-font-weight: bold;");
                            break;
                        case BONUS_FINANCIER:
                            setStyle("-fx-background-color: rgba(76, 175, 80, 0.2); -fx-text-fill: white; -fx-font-weight: bold;");
                            break;
                        case TROPHEE:
                            setStyle("-fx-background-color: rgba(255, 193, 7, 0.2); -fx-text-fill: white; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-background-color: rgba(158, 158, 158, 0.2); -fx-text-fill: white;");
                            break;
                    }
                }
            }
        });

        // Valeur Column
        valeurColumn.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        valeurColumn.setCellFactory(column -> new TableCell<Recompense, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    // Format as currency if it's a financial reward
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Recompense recompense = getTableView().getItems().get(index);
                        if (recompense != null && (recompense.getType() == TypeRecompense.PRIX ||
                                recompense.getType() == TypeRecompense.BONUS_FINANCIER)) {
                            setText(currencyFormat.format(item));
                        } else {
                            setText(String.valueOf(item));
                        }
                    } else {
                        setText(String.valueOf(item));
                    }
                }
            }
        });

        // Actions Column
        actionsColumn.setCellFactory(column -> new TableCell<Recompense, Void>() {
            private final Button modifierButton = new Button("Modifier");
            private final Button supprimerButton = new Button("Supprimer");

            {
                modifierButton.getStyleClass().add("button-modifier");
                supprimerButton.getStyleClass().add("button-supprimer");

                modifierButton.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Recompense recompense = getTableView().getItems().get(index);
                        System.out.println("Modifying reward: " + recompense);
                        openRecompensePopup(recompense);
                    }
                });

                supprimerButton.setOnAction(event -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Recompense recompense = getTableView().getItems().get(index);
                        System.out.println("Deleting reward: " + recompense);
                        handleDeleteRecompense(recompense);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    buttons.getChildren().addAll(modifierButton, supprimerButton);
                    setGraphic(buttons);
                }
            }
        });

        System.out.println("Table columns setup complete.");
    }

    /**
     * Get coach name from cache instead of querying the database each time
     */
    private String getCoachNameFromCache(Equipe equipe) {
        if (equipe == null) {
            System.out.println("Warning: Null equipe in getCoachNameFromCache");
            return "N/A";
        }

        int coachId = equipe.getCoachId();

        // Check if coach name is in cache
        if (coachNameCache.containsKey(coachId)) {
            return coachNameCache.get(coachId);
        }

        // If not in cache, try to load it
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String query = "SELECT nom, prenom FROM Utilisateur WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, coachId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String coachName = prenom + " " + nom;

                // Add to cache
                coachNameCache.put(coachId, coachName);
                return coachName;
            }
        } catch (SQLException e) {
            System.err.println("Error loading coach name for ID " + coachId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null); // Don't close the connection
        }

        // If coach not found, return a placeholder
        return "Coach ID: " + coachId;
    }

    private void makeTableColumnsResponsive() {
        System.out.println("Making table columns responsive...");

        // Set the column resize policy to constrained resize
        recompenseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Make the table fill its parent container
        recompenseTable.prefWidthProperty().bind(recompenseTable.getParent().layoutBoundsProperty().map(bounds -> bounds.getWidth() - 40));

        // Set custom column widths (Equipe: 25%, Coach: 20%, Type: 20%, Valeur: 15%, Actions: 20%)
        double[] columnPercentages = {25, 20, 20, 15, 20};

        TableColumn<?, ?>[] columns = {equipeColumn, coachColumn, typeColumn, valeurColumn, actionsColumn};

        for (int i = 0; i < columns.length; i++) {
            TableColumn<?, ?> column = columns[i];
            double percentage = columnPercentages[i] / 100.0;

            // Set minimum width to prevent columns from disappearing
            column.setMinWidth(50);

            // Bind the column width to a percentage of the table width
            column.prefWidthProperty().bind(recompenseTable.widthProperty().multiply(percentage));
        }

        System.out.println("Table columns made responsive.");
    }

    private void initializeTypeFilter() {
        System.out.println("Initializing type filter...");

        typeFilterComboBox.getItems().clear();
        typeFilterComboBox.getItems().add("Tous les types");

        for (TypeRecompense type : TypeRecompense.values()) {
            typeFilterComboBox.getItems().add(type.toString());
        }

        typeFilterComboBox.setValue("Tous les types");

        // Add listener for filtering
        typeFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            filterTableByType(newValue);
        });

        System.out.println("Type filter initialized.");
    }

    private void loadTableData() {
        System.out.println("Loading table data...");

        try {
            // Get all rewards
            List<Recompense> recompenseList = recompenseDAO.lireTous();
            System.out.println("Loaded " + recompenseList.size() + " rewards from database.");

            // Debug: Print each reward to verify data
            for (Recompense r : recompenseList) {
                System.out.println("Reward: " + r);
                System.out.println("  - Equipe: " + (r.getEquipe() != null ? r.getEquipe().getNom() : "NULL"));
                System.out.println("  - Type: " + r.getType());
                System.out.println("  - Valeur: " + r.getValeur());
            }

            // Convert to observable list
            recompenses = FXCollections.observableArrayList(recompenseList);

            // Set items to table
            recompenseTable.setItems(recompenses);

            System.out.println("Table data loaded successfully.");

        } catch (Exception e) {
            System.err.println("Error loading table data: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Une erreur est survenue lors du chargement des données: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        System.out.println("Handling search...");

        String searchTerm = searchField.getText().trim().toLowerCase();
        String typeFilterValue = typeFilterComboBox.getValue();

        try {
            // Get all rewards
            List<Recompense> allRecompenses = recompenseDAO.lireTous();

            // Filter by search term and type
            ObservableList<Recompense> filteredList = FXCollections.observableArrayList();

            for (Recompense recompense : allRecompenses) {
                boolean matchesSearch = searchTerm.isEmpty() ||
                        (recompense.getEquipe() != null &&
                                recompense.getEquipe().getNom().toLowerCase().contains(searchTerm));

                boolean matchesType = "Tous les types".equals(typeFilterValue) ||
                        recompense.getType().toString().equals(typeFilterValue);

                if (matchesSearch && matchesType) {
                    filteredList.add(recompense);
                }
            }

            // Update table
            recompenseTable.setItems(filteredList);

            System.out.println("Search complete. Found " + filteredList.size() + " results.");

        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de recherche",
                    "Une erreur est survenue lors de la recherche: " + e.getMessage());
        }
    }

    private void filterTableByType(String typeValue) {
        System.out.println("Filtering table by type: " + typeValue);

        if (typeValue == null || recompenses == null) return;

        try {
            if ("Tous les types".equals(typeValue)) {
                recompenseTable.setItems(recompenses);
            } else {
                // Filter by type
                ObservableList<Recompense> filteredList = FXCollections.observableArrayList();

                for (Recompense recompense : recompenses) {
                    if (recompense.getType().toString().equals(typeValue)) {
                        filteredList.add(recompense);
                    }
                }

                recompenseTable.setItems(filteredList);
            }

            // Apply search filter if search field is not empty
            if (!searchField.getText().trim().isEmpty()) {
                handleSearch();
            }

            System.out.println("Type filtering complete.");

        } catch (Exception e) {
            System.err.println("Error during type filtering: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de filtrage",
                    "Une erreur est survenue lors du filtrage par type: " + e.getMessage());
        }
    }

    @FXML
    public void handleResetFilter() {
        System.out.println("Resetting filters...");

        searchField.clear();
        typeFilterComboBox.setValue("Tous les types");
        recompenseTable.setItems(recompenses);

        System.out.println("Filters reset.");
    }

    @FXML
    public void handleAddRecompense() {
        System.out.println("Adding new reward...");
        openRecompensePopup(null);
    }

    private void handleDeleteRecompense(Recompense recompense) {
        System.out.println("Handling delete reward...");

        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette récompense ?");

        // Style the alert dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            dialogPane.getStyleClass().add("alert-dialog");
        } catch (Exception e) {
            System.err.println("Could not load CSS for dialog: " + e.getMessage());
        }

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    System.out.println("Deleting reward with ID: " + recompense.getId());

                    // Delete from database
                    recompenseDAO.supprimer(recompense.getId());

                    // Remove from list
                    recompenses.remove(recompense);

                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Suppression réussie",
                            "La récompense a été supprimée avec succès.");

                    System.out.println("Reward deleted successfully.");

                } catch (Exception e) {
                    System.err.println("Error deleting reward: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                            "Une erreur est survenue lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void openRecompensePopup(Recompense recompense) {
        System.out.println("Opening reward popup...");

        try {
            // Try multiple possible FXML file paths
            String[] possiblePaths = {
                    "/RecompensePopup.fxml",
                    "/views/RecompensePopup.fxml",
                    "/fxml/RecompensePopup.fxml",
                    "RecompensePopup.fxml"
            };

            FXMLLoader loader = null;
            String successPath = null;

            for (String path : possiblePaths) {
                System.out.println("Trying to load FXML from: " + path);
                loader = new FXMLLoader(getClass().getResource(path));
                if (loader.getLocation() != null) {
                    successPath = path;
                    break;
                }
            }

            if (loader == null || loader.getLocation() == null) {
                throw new IOException("Could not find RecompensePopup.fxml in any of the expected locations");
            }

            System.out.println("FXML found at: " + successPath);
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            RecompensePopupController controller = loader.getController();

            // Make sure these DAOs are properly initialized
            if (recompenseDAO == null) {
                System.out.println("RecompenseDAO was null, creating new instance");
                recompenseDAO = new RecompenseDAOImpl();
            }

            if (equipeDAO == null) {
                System.out.println("EquipeDAO was null, creating new instance");
                equipeDAO = new EquipeDAO();
            }

            controller.setRecompenseDAO(recompenseDAO);
            controller.setEquipeDAO(equipeDAO);

            if (recompense != null) {
                System.out.println("Setting existing reward for editing: " + recompense);
                controller.setRecompense(recompense);
            }

            // Set callback for when a reward is saved
            controller.setOnSaveCallback(() -> {
                System.out.println("Reward saved callback triggered, reloading table data");
                loadTableData(); // Refresh the table
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Attribuer une Récompense");

            Scene scene = new Scene(root);

            // Try multiple possible CSS file paths
            String[] possibleCssPaths = {
                    "/styles/application.css",
                    "/css/application.css",
                    "styles/application.css",
                    "css/application.css"
            };

            boolean cssLoaded = false;
            for (String cssPath : possibleCssPaths) {
                try {
                    System.out.println("Trying to load CSS from: " + cssPath);
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                    cssLoaded = true;
                    System.out.println("CSS loaded successfully from: " + cssPath);
                    break;
                } catch (Exception e) {
                    System.err.println("Could not load CSS from " + cssPath + ": " + e.getMessage());
                }
            }

            if (!cssLoaded) {
                System.err.println("Warning: Could not load CSS from any of the expected locations");
            }

            stage.setScene(scene);
            stage.showAndWait();

            System.out.println("Reward popup closed");

        } catch (IOException e) {
            System.err.println("Error opening reward popup: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre de récompense: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error opening reward popup: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur inattendue",
                    "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        try {
            // Try multiple possible CSS file paths
            String[] possibleCssPaths = {
                    "/styles/application.css",
                    "/css/application.css",
                    "styles/application.css",
                    "css/application.css"
            };

            boolean cssLoaded = false;
            for (String cssPath : possibleCssPaths) {
                try {
                    dialogPane.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                    cssLoaded = true;
                    break;
                } catch (Exception e) {
                    // Continue to next path
                }
            }

            if (cssLoaded) {
                dialogPane.getStyleClass().add("alert-dialog");
            }
        } catch (Exception e) {
            System.err.println("Could not load CSS for alert: " + e.getMessage());
        }

        alert.showAndWait();
    }

    // Helper method to close resources
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}