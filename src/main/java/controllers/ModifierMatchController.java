package controllers;

import dao.AreneDAO;
import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import enums.StatutMatch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import models.Arene;
import models.Equipe;
import models.Match;
import models.Tournoi;

import java.sql.Timestamp; // Change from Date to Timestamp
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime; // Add this import
import java.util.List;
import java.sql.Timestamp; // Replace java.sql.Date
import java.time.LocalTime;

public class ModifierMatchController {

    @FXML private Label idLabel;
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

    private Match match;
    private MatchDAO matchDAO;
    private EquipeDAO equipeDAO;
    private TournoiDAO tournoiDAO;
    private AreneDAO areneDAO;
    private Stage previousStage;
    private Runnable onMatchUpdated;

    @FXML
    public void initialize() {
        try {
            matchDAO = new MatchDAO();
            equipeDAO = new EquipeDAO();
            tournoiDAO = new TournoiDAO();
            areneDAO = new AreneDAO();

            // Initialize time spinners
            SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
            hourSpinner.setValueFactory(hourFactory);
            
            SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
            minuteSpinner.setValueFactory(minuteFactory);
            
            statutComboBox.setItems(FXCollections.observableArrayList(StatutMatch.values()));
            
            // Add listener to statutComboBox to show/hide time spinners
            statutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateTimeSpinnersVisibility(newVal);
            });
            
            loadEquipes();
            loadTournois();
            loadArenes();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de se connecter à la base de données.");
            e.printStackTrace();
        }
    }

    private void loadEquipes() {
        List<Equipe> equipes = equipeDAO.lireTous();
        equipe1ComboBox.setItems(FXCollections.observableArrayList(equipes));
        equipe2ComboBox.setItems(FXCollections.observableArrayList(equipes));
        
        // Ne pas charger toutes les équipes dans vainqueurComboBox ici
        // Le chargement sera fait dans setMatch() avec seulement les équipes du match
        
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
    }

    private void loadTournois() {
        List<Tournoi> tournois = tournoiDAO.lireTous();
        tournoiComboBox.setItems(FXCollections.observableArrayList(tournois));
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
    }

    private void loadArenes() {
        List<Arene> arenes = areneDAO.lireTous();
        areneComboBox.setItems(FXCollections.observableArrayList(arenes));
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
    }


    
    // In the setMatch method, add:


    public void setOnMatchUpdated(Runnable callback) {
        this.onMatchUpdated = callback;
    }

    @FXML
    private void handleSave() {
        try {
            LocalDate localDate = datePicker.getValue();
            if (localDate == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner une date.");
                return;
            }
            

            
            // With:
            LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
            Timestamp matchTimestamp = Timestamp.valueOf(localDate.atTime(time));
            
            // Then use matchTimestamp instead of matchDate in all places
            // For example:
            match.setDateMatch(matchTimestamp);
            
            // Check if date is before now for EN_ATTENTE matches
            StatutMatch statut = statutComboBox.getValue();
            if (statut == StatutMatch.EN_ATTENTE && matchTimestamp.before(new Timestamp(System.currentTimeMillis()))) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", 
                        "Vous ne pouvez pas définir une date antérieure à maintenant pour un match en attente.");
                return;
            }
            
            // Check if scores are being modified for a TERMINE match
            if (match.getStatutMatch() == StatutMatch.TERMINE) {
                int originalScore1 = match.getScoreEquipe1();
                int originalScore2 = match.getScoreEquipe2();
                int newScore1 = Integer.parseInt(scoreEquipe1Field.getText().isEmpty() ? "0" : scoreEquipe1Field.getText());
                int newScore2 = Integer.parseInt(scoreEquipe2Field.getText().isEmpty() ? "0" : scoreEquipe2Field.getText());
                
                if (originalScore1 != newScore1 || originalScore2 != newScore2) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de saisie", 
                            "Vous ne pouvez pas modifier les scores d'un match terminé.");
                    return;
                }
            }
            
            Equipe equipe1 = equipe1ComboBox.getValue();
            Equipe equipe2 = equipe2ComboBox.getValue();
            Tournoi tournoi = tournoiComboBox.getValue();
            Arene arene = areneComboBox.getValue();


            if (equipe1 == null || equipe2 == null || tournoi == null || arene == null || statut == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs obligatoires.");
                return;
            }
            if (equipe1.equals(equipe2)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les deux équipes doivent être différentes.");
                return;
            }

            int scoreEquipe1 = Integer.parseInt(scoreEquipe1Field.getText().isEmpty() ? "0" : scoreEquipe1Field.getText());
            int scoreEquipe2 = Integer.parseInt(scoreEquipe2Field.getText().isEmpty() ? "0" : scoreEquipe2Field.getText());

            Integer vainqueurId = null;
            Equipe vainqueur = vainqueurComboBox.getValue();
            if (vainqueur != null) {
                // Vérifier si on peut attribuer un vainqueur pour ce statut
                if (statut == StatutMatch.EN_ATTENTE || statut == StatutMatch.EN_COURS) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de saisie", 
                            "Vous ne pouvez pas attribuer un vainqueur pour un match en attente ou en cours.");
                    return;
                }
                vainqueurId = vainqueur.getId();
            }
            
            // Check if duree is set for EN_ATTENTE matches
            Integer duree = null;
            if (!dureeField.getText().isEmpty()) {
                if (statut == StatutMatch.EN_ATTENTE) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de saisie", 
                            "Vous ne pouvez pas définir une durée pour un match en attente.");
                    return;
                }
                duree = Integer.parseInt(dureeField.getText());
            }
            
            String nomJeu = nomJeuField.getText().isEmpty() ? null : nomJeuField.getText();

            if (statut == StatutMatch.EN_COURS &&
                    !matchDAO.isArenaAvailable(arene.getAreneId()) &&
                    (match.getIdArene() != arene.getAreneId() || match.getStatutMatch() != StatutMatch.EN_COURS)) {
                showAlert(Alert.AlertType.ERROR, "Arène non disponible",
                        "Cette arène a déjà un match en cours. Veuillez choisir une autre arène ou un autre statut.");
                return;
            }

            match.setIdEquipe1(equipe1.getId());
            match.setIdEquipe2(equipe2.getId());
            match.setIdTournoi(tournoi.getId());
            match.setIdArene(arene.getAreneId());
            match.setScoreEquipe1(scoreEquipe1);
            match.setScoreEquipe2(scoreEquipe2);
            match.setVainqueur(vainqueurId);
            match.setDuree(duree);
            match.setNomJeu(nomJeu);
            match.setDateMatch(matchTimestamp);
            match.setStatutMatch(statut);

            matchDAO.modifier(match);

            if (onMatchUpdated != null) {
                onMatchUpdated.run();
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le match a été modifié avec succès.");
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les scores et la durée doivent être des nombres entiers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification du match.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
        
        if (previousStage != null) {
            previousStage.show();
        }
    }
    
    public void setPreviousStage(Stage stage) {
        this.previousStage = stage;
    }

    private void closeWindow() {
        ((Stage) idLabel.getScene().getWindow()).close();
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

    // Keep only one setMatch method and make sure it includes the updateTimeSpinnersVisibility call
    public void setMatch(Match match) {
        this.match = match;
        idLabel.setText(String.valueOf(match.getMatchId()));
        
        // Extract date and time from Timestamp
        LocalDate date = match.getDateMatch().toLocalDateTime().toLocalDate();
        LocalTime time = match.getDateMatch().toLocalDateTime().toLocalTime();
        
        datePicker.setValue(date);
        hourSpinner.getValueFactory().setValue(time.getHour());
        minuteSpinner.getValueFactory().setValue(time.getMinute());
        
        scoreEquipe1Field.setText(String.valueOf(match.getScoreEquipe1()));
        scoreEquipe2Field.setText(String.valueOf(match.getScoreEquipe2()));
        statutComboBox.setValue(match.getStatutMatch());
        dureeField.setText(match.getDuree() != null ? String.valueOf(match.getDuree()) : "");
        nomJeuField.setText(match.getNomJeu() != null ? match.getNomJeu() : "");

        Equipe equipe1 = null;
        Equipe equipe2 = null;
        
        for (Equipe equipe : equipe1ComboBox.getItems()) {
            if (equipe.getId() == match.getIdEquipe1()) {
                equipe1 = equipe;
                equipe1ComboBox.setValue(equipe);
                break;
            }
        }
        for (Equipe equipe : equipe2ComboBox.getItems()) {
            if (equipe.getId() == match.getIdEquipe2()) {
                equipe2 = equipe;
                equipe2ComboBox.setValue(equipe);
                break;
            }
        }
        
        // Configurer le ComboBox vainqueur avec seulement les deux équipes du match et l'option "Aucun vainqueur"
        ObservableList<Equipe> vainqueurOptions = FXCollections.observableArrayList();
        vainqueurOptions.add(null); // Option "Aucun vainqueur"
        if (equipe1 != null) vainqueurOptions.add(equipe1);
        if (equipe2 != null) vainqueurOptions.add(equipe2);
        vainqueurComboBox.setItems(vainqueurOptions);
        
        // Sélectionner le vainqueur actuel s'il existe
        if (match.getVainqueur() != null) {
            for (Equipe equipe : vainqueurComboBox.getItems()) {
                if (equipe != null && equipe.getId() == match.getVainqueur()) {
                    vainqueurComboBox.setValue(equipe);
                    break;
                }
            }
        } else {
            vainqueurComboBox.setValue(null); // Aucun vainqueur
        }
        
        for (Tournoi tournoi : tournoiComboBox.getItems()) {
            if (tournoi.getId() == match.getIdTournoi()) {
                tournoiComboBox.setValue(tournoi);
                break;
            }
        }
        for (Arene arene : areneComboBox.getItems()) {
            if (arene.getAreneId() == match.getIdArene()) {
                areneComboBox.setValue(arene);
                break;
            }
        }
        
        // Update time spinners visibility based on match status
        updateTimeSpinnersVisibility(match.getStatutMatch());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}