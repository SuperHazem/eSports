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
    @FXML private Button saveButton;
    @FXML private Label titleLabel;

    private Sponsor sponsor;
    private boolean isEditMode;

    public void setSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
        this.isEditMode = sponsor != null;
        if (sponsor != null) {
            fnameField.setText(sponsor.getFname());
            lnameField.setText(sponsor.getLname());
            emailField.setText(sponsor.getEmail());
            phoneField.setText(sponsor.getPhone());
            addressField.setText(sponsor.getAddress());
            montantField.setText(String.valueOf(sponsor.getMontant()));
        }
    }

    public Sponsor getSponsor() {
        return sponsor;
    }

    @FXML
    private void initialize() {
        saveButton.setOnAction(e -> {
            try {
                String fname = fnameField.getText();
                String lname = lnameField.getText();
                String email = emailField.getText();
                String phone = phoneField.getText();
                String address = addressField.getText();
                double montant = Double.parseDouble(montantField.getText());

                Sponsor newSponsor = new Sponsor(fname, lname, address, email, phone, montant);
                
                // Validate the sponsor
                List<String> errors = SponsorValidator.validateSponsor(newSponsor);
                if (!errors.isEmpty()) {
                    showError("Erreur de validation", String.join("\n", errors));
                    return;
                }

                if (isEditMode) {
                    this.sponsor.setFname(fname);
                    this.sponsor.setLname(lname);
                    this.sponsor.setEmail(email);
                    this.sponsor.setPhone(phone);
                    this.sponsor.setAddress(address);
                    this.sponsor.setMontant(montant);
                } else {
                    this.sponsor = newSponsor;
                }

                closeStage();
            } catch (NumberFormatException ex) {
                showError("Erreur", "Le montant doit être un nombre valide");
            }
        });
    }

    @FXML
    public void annuler() {
        sponsor = null;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 