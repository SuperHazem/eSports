package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Publication;
import dao.PublicationDAO;
import dao.PublicationDAOImpl;
import java.io.File;

public class PublicationPopupController {
    private static final int CURRENT_USER_ID = 1;
    private final PublicationDAO publicationDAO = new PublicationDAOImpl();
    private Publication publication;
    private boolean isEditMode = false;
    private String selectedImagePath;

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea contenuField;
    @FXML private ImageView imagePreview;
    @FXML private Button selectImageButton;

    @FXML
    public void initialize() {
        imagePreview.setFitWidth(200);
        imagePreview.setFitHeight(200);
        imagePreview.setPreserveRatio(true);
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
            if (publication.getImage() != null && !publication.getImage().isEmpty()) {
                selectedImagePath = publication.getImage();
                displayImage(selectedImagePath);
            }
        }
    }

    @FXML
    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectImageButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            displayImage(selectedImagePath);
        }
    }

    private void displayImage(String imagePath) {
        try {
            Image image = new Image(new File(imagePath).toURI().toString());
            imagePreview.setImage(image);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de charger l'image: " + e.getMessage());
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
                publication.setImage(selectedImagePath);
                publicationDAO.modifier(publication);
            } else {
                Publication nouvellePublication = new Publication(titre, contenu, selectedImagePath);
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 