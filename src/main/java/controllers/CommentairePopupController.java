package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import models.Commentaire;
import models.Publication;
import dao.CommentaireDAO;
import dao.CommentaireDAOImpl;

public class CommentairePopupController {
    private static final int CURRENT_USER_ID = 1;
    private final CommentaireDAO commentaireDAO = new CommentaireDAOImpl();
    private Commentaire commentaire;
    private Publication publication;
    private boolean isEditMode = false;

    @FXML private Label titleLabel;
    @FXML private TextArea contenuField;
    @FXML private Spinner<Integer> noteSpinner;

    @FXML
    public void initialize() {
        // Configure note spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 3);
        noteSpinner.setValueFactory(valueFactory);
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public void setCommentaire(Commentaire commentaire) {
        this.commentaire = commentaire;
        this.isEditMode = true;
        titleLabel.setText("Modifier Commentaire");
        populateFields();
    }

    private void populateFields() {
        if (commentaire != null) {
            contenuField.setText(commentaire.getContenu());
            noteSpinner.getValueFactory().setValue(commentaire.getNote());
        }
    }

    @FXML
    private void enregistrer() {
        if (!validateFields()) {
            return;
        }

        String contenu = contenuField.getText().trim();
        int note = noteSpinner.getValue();

        try {
            if (isEditMode) {
                commentaire.setContenu(contenu);
                commentaire.setNote(note);
                commentaireDAO.modifier(commentaire);
            } else {
                Commentaire nouveauCommentaire = new Commentaire(contenu, note, publication);
                commentaireDAO.ajouter(nouveauCommentaire);
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
        String contenu = contenuField.getText().trim();
        
        if (contenu.isEmpty()) {
            afficherAlerte("Erreur de validation", "Le contenu ne peut pas être vide.");
            contenuField.requestFocus();
            return false;
        }
        
        if (noteSpinner.getValue() == null) {
            afficherAlerte("Erreur de validation", "Veuillez sélectionner une note.");
            noteSpinner.requestFocus();
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