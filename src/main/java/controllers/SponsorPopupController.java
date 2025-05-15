package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Sponsor;
import utils.validators.SponsorValidator;
import java.util.List;

public class SponsorPopupController {

    @FXML private TextField fnameField;
    @FXML private TextField lnameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField montantField;
    @FXML private Label titleLabel;

    private Sponsor sponsor;
    private boolean isEditMode = false;

    public void initialize() {
        // Initialize any necessary components
    }

    public void setSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
        this.isEditMode = true;
        populateFields();
        titleLabel.setText("Modifier Sponsor");
    }

    private void populateFields() {
        if (sponsor != null) {
            fnameField.setText(sponsor.getFname());
            lnameField.setText(sponsor.getLname());
            emailField.setText(sponsor.getEmail());
            phoneField.setText(sponsor.getPhone());
            addressField.setText(sponsor.getAddress());
            montantField.setText(String.valueOf(sponsor.getMontant()));
        }
    }

    @FXML
    public void enregistrer() {
        String fname = fnameField.getText().trim();
        String lname = lnameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String montantStr = montantField.getText().trim();

        // Créer un objet temporaire pour la validation
        Sponsor tempSponsor = new Sponsor();
        tempSponsor.setFname(fname);
        tempSponsor.setLname(lname);
        tempSponsor.setEmail(email);
        tempSponsor.setPhone(phone);
        tempSponsor.setAddress(address);
        
        try {
            double montant = Double.parseDouble(montantStr);
            tempSponsor.setMontant(montant);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide.");
            return;
        }

        // Valider le sponsor
        List<String> errors = SponsorValidator.validateSponsor(tempSponsor);
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        // Si la validation est réussie, procéder à l'enregistrement
        if (isEditMode) {
            sponsor.setFname(fname);
            sponsor.setLname(lname);
            sponsor.setEmail(email);
            sponsor.setPhone(phone);
            sponsor.setAddress(address);
            sponsor.setMontant(tempSponsor.getMontant());
        } else {
            sponsor = new Sponsor(fname, lname, email, phone, address, tempSponsor.getMontant());
        }

        closeStage();
    }

    private void showValidationErrors(List<String> errors) {
        StringBuilder message = new StringBuilder("Veuillez corriger les erreurs suivantes :\n\n");
        for (String error : errors) {
            message.append("• ").append(error).append("\n");
        }
        showAlert("Erreurs de validation", message.toString());
    }

    @FXML
    public void annuler() {
        sponsor = null;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) fnameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Sponsor getSponsor() {
        return sponsor;
    }
} 