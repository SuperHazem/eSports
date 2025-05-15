package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.Match;
import models.Tournoi;
import services.SmartMatchmakingService;
import dao.TournoiDAO;
import utils.WindowManager;

import java.sql.SQLException;
import java.util.List;

public class SmartMatchmakingController {

    @FXML
    private ComboBox<Tournoi> tournoiComboBox;
    
    @FXML
    private Label statusLabel;
    
    private TournoiDAO tournoiDAO;
    private SmartMatchmakingService matchmakingService;
    
    @FXML
    public void initialize() {
        try {
            tournoiDAO = new TournoiDAO();
            matchmakingService = new SmartMatchmakingService();
            
            // Charger les tournois dans le ComboBox
            List<Tournoi> tournois = tournoiDAO.lireTous();
            tournoiComboBox.setItems(FXCollections.observableArrayList(tournois));
            
            // Configurer l'affichage des tournois dans le ComboBox
            tournoiComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Tournoi>() {
                @Override
                protected void updateItem(Tournoi item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
            tournoiComboBox.setButtonCell(new javafx.scene.control.ListCell<Tournoi>() {
                @Override
                protected void updateItem(Tournoi item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Sélectionner un tournoi");
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
            statusLabel.setText("Prêt pour le matchmaking intelligent");
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation", 
                    "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGenerateMatches() {
        Tournoi selectedTournoi = tournoiComboBox.getValue();
        if (selectedTournoi == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélection requise", 
                    "Veuillez sélectionner un tournoi pour générer les matchs.");
            return;
        }
        
        try {
            statusLabel.setText("Génération des matchs en cours...");
            
            // Appeler le service pour générer les matchs
            List<Match> generatedMatches = matchmakingService.generateMatchesForTournament(selectedTournoi.getId());
            
            if (generatedMatches.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Résultat", "Aucun match généré", 
                        "Aucun match n'a pu être généré. Vérifiez que des équipes sont disponibles.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matchs générés", 
                        generatedMatches.size() + " matchs ont été générés et ajoutés avec succès.");
            }
            
            statusLabel.setText("Matchmaking terminé: " + generatedMatches.size() + " matchs générés");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de génération", 
                    "Une erreur est survenue lors de la génération des matchs: " + e.getMessage());
            statusLabel.setText("Erreur lors du matchmaking");
        }
    }

    @FXML
    private void handleRetourner() {
        WindowManager.getInstance().goBack();
    }
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}