package controllers;

import dao.MatchDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Match;
import utils.WindowManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class DetailMatchController {

    @FXML private Label dateLabel;
    @FXML private Label equipe1Label;
    @FXML private Label equipe2Label;
    @FXML private Label tournoiLabel;
    @FXML private Label areneLabel;
    @FXML private Label scoreLabel;
    @FXML private Label vainqueurLabel;
    @FXML private Label dureeLabel;
    @FXML private Label nomJeuLabel;
    @FXML private Label statutLabel;

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

    public void setMatch(Match match) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateLabel.setText(dateFormat.format(match.getDateMatch()));
        equipe1Label.setText(match.getEquipe1Nom() != null ? match.getEquipe1Nom() : "N/A");
        equipe2Label.setText(match.getEquipe2Nom() != null ? match.getEquipe2Nom() : "N/A");
        tournoiLabel.setText(match.getTournoiNom() != null ? match.getTournoiNom() : "N/A");
        areneLabel.setText(match.getAreneNom() != null ? match.getAreneNom() : "N/A");
        scoreLabel.setText(match.getScoreEquipe1() + " - " + match.getScoreEquipe2());
        vainqueurLabel.setText(match.getVainqueur() != null ?
                (match.getVainqueur().equals(match.getIdEquipe1()) ?
                        match.getEquipe1Nom() : match.getEquipe2Nom()) : "Non déterminé");
        dureeLabel.setText(match.getDuree() != null ? match.getDuree() + " minutes" : "Non spécifié");
        nomJeuLabel.setText(match.getNomJeu() != null ? match.getNomJeu() : "Non spécifié");
        statutLabel.setText(match.getStatutMatch().toString());
    }

    @FXML
    private void handleClose() {
        WindowManager.getInstance().closeCurrentWindow();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}