package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Publication;
import dao.PublicationDAO;
import dao.PublicationDAOImpl;

public class PublicationPopupController {
    private static final int CURRENT_USER_ID = 1;
    private final PublicationDAO publicationDAO = new PublicationDAOImpl();
    private Publication publication;
    private boolean isEditMode = false;

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;

    @FXML
    public void initialize() {
        // Initialization code if needed
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
        this.isEditMode = true;
        titleLabel.setText("Modifier Publication");
        populateFields();
    }

    private void populateFields() {
        if (publication != null) {
            titreField.setText(publication.getTitre());
            contenuField.setText(publication.getContenu());
        }
    }

    @FXML
    private void enregistrer() {
        if (!validateFields()) {
            return;
        }

        String titre = titreField.getText().trim();
        String contenu = contenuField.getText().trim();

        try {
            if (isEditMode) {
                publication.setTitre(titre);
                publication.setContenu(contenu);
                publicationDAO.modifier(publication);
            } else {
                Publication nouvellePublication = new Publication(titre, contenu);
                publicationDAO.ajouter(nouvellePublication);
            }
            closeStage();
        } catch (RuntimeException e) {
            String errorMessage = "Erreur lors de l'enregistrement: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += "\nCause: " + e.getCause().getMessage();
            }
            afficherAlerte("Erreur", errorMessage);
        }
    }

    private boolean validateFields() {
        String titre = titreField.getText().trim();
        String contenu = contenuField.getText().trim();
        
        if (titre.isEmpty()) {
            afficherAlerte("Erreur de validation", "Le titre ne peut pas être vide.");
            titreField.requestFocus();
            return false;
        }
        
        if (contenu.isEmpty()) {
            afficherAlerte("Erreur de validation", "Le contenu ne peut pas être vide.");
            contenuField.requestFocus();
            return false;
        }
        
        return true;
    }

    @FXML
    private void annuler() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) contenuField.getScene().getWindow();
        stage.close();
    }

    private void afficherAlerte(String titre, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 