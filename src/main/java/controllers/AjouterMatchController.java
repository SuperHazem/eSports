package controllers;

import dao.AreneDAO;
import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import enums.StatutMatch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import models.Arene;
import models.Equipe;
import models.Match;
import models.Tournoi;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AjouterMatchController {

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private ComboBox<Equipe> equipe1ComboBox;
    @FXML private ComboBox<Equipe> equipe2ComboBox;
    @FXML private ComboBox<Tournoi> tournoiComboBox;
    @FXML private ComboBox<Arene> areneComboBox;
    @FXML private TextField scoreEquipe1Field;
    @FXML private TextField scoreEquipe2Field;
    @FXML private ComboBox<Equipe> vainqueurComboBox;
    @FXML private TextField dureeField;
    @FXML private TextField nomJeuField;
    @FXML private ComboBox<StatutMatch> statutComboBox;

    private MatchDAO matchDAO;
    private EquipeDAO equipeDAO;
    private TournoiDAO tournoiDAO;
    private AreneDAO areneDAO;
    private Stage previousStage;

    // Add this field for the callback
    private Runnable onMatchAdded;

    @FXML
    public void initialize() {
        try {
            matchDAO = new MatchDAO();
            equipeDAO = new EquipeDAO();
            tournoiDAO = new TournoiDAO();
            areneDAO = new AreneDAO();

            datePicker.setValue(LocalDate.now());
            
            // Initialize time spinners
            SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
            hourSpinner.setValueFactory(hourFactory);
            
            SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
            minuteSpinner.setValueFactory(minuteFactory);
            
            statutComboBox.setItems(FXCollections.observableArrayList(StatutMatch.values()));
            statutComboBox.setValue(StatutMatch.EN_ATTENTE);
            
            // Add listener to statutComboBox to show/hide time spinners
            statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateTimeSpinnersVisibility(newVal);
            });
            
            // Initial visibility setup
            updateTimeSpinnersVisibility(statutComboBox.getValue());

            loadEquipes();
            loadTournois();
            loadArenes();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données.");
            e.printStackTrace();
        }
    }
    
    // Add this method to control time spinners visibility
    private void updateTimeSpinnersVisibility(StatutMatch statut) {
        boolean showSpinners = (statut == StatutMatch.EN_ATTENTE || statut == StatutMatch.TERMINE);
        hourSpinner.setVisible(showSpinners);
        hourSpinner.setManaged(showSpinners);
        minuteSpinner.setVisible(showSpinners);
        minuteSpinner.setManaged(showSpinners);

        // Trouver et cacher/afficher le label "Heure:" dans le GridPane
        GridPane gridPane = (GridPane) hourSpinner.getParent();
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if ("Heure:".equals(label.getText())) {
                    label.setVisible(showSpinners);
                    label.setManaged(showSpinners);
                    break;
                }
            }
        }
    }
    
    // Add this method to set the callback
    public void setOnMatchAdded(Runnable callback) {
        this.onMatchAdded = callback;
    }

    private void loadEquipes() {
        List<Equipe> equipes = equipeDAO.lireTous();
        equipe1ComboBox.setItems(FXCollections.observableArrayList(equipes));
        equipe2ComboBox.setItems(FXCollections.observableArrayList(equipes));
        
        // Create a special observable list for vainqueur that includes a null option
        ObservableList<Equipe> vainqueurOptions = FXCollections.observableArrayList(equipes);
        // Add null as the first option
        vainqueurOptions.add(0, null);
        vainqueurComboBox.setItems(vainqueurOptions);
        
        equipe1ComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Equipe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        equipe1ComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Equipe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        equipe2ComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Equipe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        equipe2ComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Equipe item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        vainqueurComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Equipe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Aucun vainqueur"); // Display "No winner" for the null option
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
                    setText("Aucun vainqueur"); // Display "No winner" for the null option
                } else {
                    setText(item.getNom());
                }
            }
        });
        if (!equipes.isEmpty()) {
            equipe1ComboBox.setValue(equipes.get(0));
            equipe2ComboBox.setValue(equipes.get(0));
        }
        
        // Set the default vainqueur to null ("No winner")
        vainqueurComboBox.setValue(null);
    }

    private void loadTournois() {
        List<Tournoi> tournoiss = tournoiDAO.lireTous();
        tournoiComboBox.setItems(FXCollections.observableArrayList(tournoiss));
        tournoiComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Tournoi item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        tournoiComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tournoi item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        if (!tournoiss.isEmpty()) {
            tournoiComboBox.setValue(tournoiss.get(0));
        }
    }

    private void loadArenes() {
        List<Arene> arene = areneDAO.lireTous();
        areneComboBox.setItems(FXCollections.observableArrayList(arene));
        areneComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Arene item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getLocation() + ")");
            }
        });
        areneComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Arene item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getLocation() + ")");
            }
        });
        if (!arene.isEmpty()) {
            areneComboBox.setValue(arene.get(0));
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            // Validate date
            LocalDate localDate = datePicker.getValue();
            if (localDate == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner une date.");
                return;
            }
            
            // Get match status
            StatutMatch statut = statutComboBox.getValue();
            if (statut == null) {
                statut = StatutMatch.EN_ATTENTE; // Default value
            }
            
            // Create Timestamp based on match status
            Timestamp matchTimestamp;
            if (statut == StatutMatch.EN_COURS) {
                // For EN_COURS matches, always use current time
                matchTimestamp = new Timestamp(System.currentTimeMillis());
            } else {
                // For EN_ATTENTE and TERMINE, use date picker and time spinners
                LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
                matchTimestamp = Timestamp.valueOf(localDate.atTime(time));
                
                // For TERMINE matches, validate that the timestamp is not in the future
                if (statut == StatutMatch.TERMINE && matchTimestamp.after(new Timestamp(System.currentTimeMillis()))) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de saisie", 
                            "Un match terminé ne peut pas avoir une date et heure dans le futur.");
                    return;
                }
            }
            
            // Validate teams
            Equipe equipe1 = equipe1ComboBox.getValue();
            Equipe equipe2 = equipe2ComboBox.getValue();
            if (equipe1 == null || equipe2 == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner les deux équipes.");
                return;
            }
            
            // Check if same team is selected for both
            if (equipe1.getId() == equipe2.getId()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les deux équipes ne peuvent pas être identiques.");
                return;
            }
            
            // Validate tournament
            Tournoi tournoi = tournoiComboBox.getValue();
            if (tournoi == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner un tournoi.");
                return;
            }
            
            // Validate arena
            Arene arene = areneComboBox.getValue();
            if (arene == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner une arène.");
                return;
            }
            
            // Create Timestamp from LocalDate and time spinners
            

            
            // Parse scores
            int scoreEquipe1 = 0;
            int scoreEquipe2 = 0;
            
            if (!scoreEquipe1Field.getText().isEmpty()) {
                scoreEquipe1 = Integer.parseInt(scoreEquipe1Field.getText());
            }
            
            if (!scoreEquipe2Field.getText().isEmpty()) {
                scoreEquipe2 = Integer.parseInt(scoreEquipe2Field.getText());
            }
            
            // Parse duration if provided
            Integer duree = null;
            if (!dureeField.getText().isEmpty()) {
                duree = Integer.parseInt(dureeField.getText());
            }
            
            // Get game name if provided
            String nomJeu = nomJeuField.getText();
            
            // Get winner if selected
            Equipe vainqueur = vainqueurComboBox.getValue();
            Integer vainqueurId = null;
            if (vainqueur != null) {
                vainqueurId = vainqueur.getId();
            }
            
            // Create match object
            Match match = new Match();
            match.setDateMatch(matchTimestamp);
            match.setIdEquipe1(equipe1.getId());
            match.setIdEquipe2(equipe2.getId());
            match.setIdTournoi(tournoi.getId());
            match.setIdArene(arene.getAreneId());
            match.setScoreEquipe1(scoreEquipe1);
            match.setScoreEquipe2(scoreEquipe2);
            match.setStatutMatch(statut);
            match.setDuree(duree);
            match.setNomJeu(nomJeu);
            match.setVainqueur(vainqueurId);
            
            // Check if arena is available
            if (!matchDAO.isArenaAvailable(arene.getAreneId())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "L'arène sélectionnée n'est pas disponible actuellement.");
                return;
            }
            
            // Add match to database
            matchDAO.ajouter(match);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le match a été ajouté avec succès.");

            // Call the callback if it exists
            if (onMatchAdded != null) {
                onMatchAdded.run();
            }

            // Reset form
            initialize();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les scores et la durée doivent être des nombres entiers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout du match.");
            e.printStackTrace();
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
    private void handleClose() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
        
        if (previousStage != null) {
            previousStage.show();
        }
    }
    
    // Using WindowManager for navigation instead of manual stage management
}