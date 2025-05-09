package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Sponsor;

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

        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || montantStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            double montant = Double.parseDouble(montantStr);

            if (isEditMode) {
                sponsor.setFname(fname);
                sponsor.setLname(lname);
                sponsor.setEmail(email);
                sponsor.setPhone(phone);
                sponsor.setAddress(address);
                sponsor.setMontant(montant);
            } else {
                sponsor = new Sponsor(fname, lname, address, email, phone, montant);
            }

            closeStage();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide.");
        }
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