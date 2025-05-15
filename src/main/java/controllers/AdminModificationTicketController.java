package controllers;

import dao.TicketDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Ticket;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AdminModificationTicketController {
    @FXML
    private TextField siegeField;

    @FXML
    private TextField prixField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> statutComboBox;

    @FXML
    private Button annulerButton;

    @FXML
    private Button confirmerButton;

    private TicketDAO ticketDAO;
    private AdminController parentController;
    private Ticket ticketAModifier;

    public void initialize() {
        try {
            ticketDAO = new TicketDAO();

            // Initialiser la combobox des statuts
            statutComboBox.getItems().addAll("Payé", "Non payé");

            // Configurer les boutons
            annulerButton.setOnAction(event -> fermerFenetre());
            confirmerButton.setOnAction(event -> modifierTicket());

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    public void setTicket(Ticket ticket) {
        this.ticketAModifier = ticket;

        // Remplir les champs avec les données du ticket
        siegeField.setText(ticket.getSiege());
        prixField.setText(String.valueOf(ticket.getPrix()));

        // Convertir Date en LocalDate pour le DatePicker
        if (ticket.getDateAchat() != null) {
            LocalDate localDate = ticket.getDateAchat().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            datePicker.setValue(localDate);
        }

        statutComboBox.setValue(ticket.getStatutPaiement());
    }

    public void setParentController(AdminController controller) {
        this.parentController = controller;
    }

    private void modifierTicket() {
        try {
            if (siegeField.getText().trim().isEmpty() || prixField.getText().trim().isEmpty()
                    || datePicker.getValue() == null || statutComboBox.getValue() == null) {

                afficherErreur("Erreur", "Veuillez remplir tous les champs");
                return;
            }

            // Mettre à jour les données du ticket
            ticketAModifier.setSiege(siegeField.getText().trim());

            try {
                double prix = Double.parseDouble(prixField.getText().trim());
                ticketAModifier.setPrix(prix);
            } catch (NumberFormatException e) {
                afficherErreur("Erreur", "Le prix doit être un nombre valide");
                return;
            }

            Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            ticketAModifier.setDateAchat(date);

            ticketAModifier.setStatutPaiement(statutComboBox.getValue());

            // Mettre à jour le ticket dans la base de données
            ticketDAO.modifier(ticketAModifier);

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket modifié");
            alert.setHeaderText(null);
            alert.setContentText("Le ticket a été modifié avec succès.");
            alert.showAndWait();

            // Rafraîchir le tableau dans la vue parent
            if (parentController != null) {
                parentController.rafraichirTableau();
            }

            // Fermer la fenêtre
            fermerFenetre();

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de modifier le ticket: " + e.getMessage());
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}