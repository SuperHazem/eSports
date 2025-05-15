package controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import models.Ticket;
import services.EmailService;
import services.StripeService;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.scene.layout.VBox;

import java.util.concurrent.CompletableFuture;

public class PaiementController {

    @FXML
    private WebView webView;

    @FXML
    private TextField emailField;

    @FXML
    private Button annulerButton;

    private static PaiementController activeInstance;

    private Ticket ticket;
    private TicketController parentController;
    private String clientSecret;
    private boolean paiementReussi = false;

    public void initialize() {
        annulerButton.setOnAction(event -> fermerFenetre());

        emailField.setDisable(false);
        emailField.setEditable(true);

        Button payerButton = new Button("Payer avec Stripe");
        payerButton.setMaxWidth(Double.MAX_VALUE);
        payerButton.getStyleClass().add("button-primary");

        VBox parent = (VBox) annulerButton.getParent();
        parent.getChildren().add(payerButton);

        payerButton.setOnAction(event -> {
            if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
                afficherErreur("Champ requis", "Veuillez entrer votre adresse email pour recevoir le ticket.");
                return;
            }

            traiterPaiement();
        });
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        activeInstance = this;
    }

    public void setParentController(TicketController controller) {
        this.parentController = controller;
    }

    private void finaliserPaiement() {
        try {
            paiementReussi = true;
            ticket.setStatutPaiement("Payé");

            String emailClient = emailField.getText();

            if (emailClient != null && !emailClient.isEmpty()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        EmailService.envoyerEmailTicketSimple(ticket, emailClient);

                        Platform.runLater(() -> {
                            afficherInfo("Paiement réussi",
                                    "Votre paiement a été traité avec succès.\n" +
                                            "Un email contenant les informations du ticket a été envoyé à : " + emailClient);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            afficherErreur("Erreur d'envoi",
                                    "Le paiement a réussi, mais l'envoi de l'email a échoué : " + e.getMessage());
                        });
                    }
                });
            } else {
                afficherInfo("Paiement réussi",
                        "Votre paiement a été traité avec succès.\nAucun email n'a été fourni pour l'envoi du ticket.");
            }

            fermerFenetre();

            if (parentController != null) {
                parentController.rechargerInterface();
            }

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Une erreur s'est produite lors de la finalisation du paiement : " + e.getMessage());
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

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void traiterPaiement() {
        try {
            clientSecret = StripeService.creerIntentionPaiement(ticket);

            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation de paiement");
            confirmationAlert.setHeaderText("Simuler un paiement Stripe");
            confirmationAlert.setContentText("Dans un environnement réel, l'utilisateur serait redirigé vers Stripe.\n\n" +
                    "Voulez-vous simuler un paiement réussi ?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                finaliserPaiement();
            }

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur de paiement", "Impossible d'initialiser le paiement : " + e.getMessage());
        }
    }

    public static void paiementReussi(String ticketId) {
        Platform.runLater(() -> {
            try {
                if (activeInstance != null) {
                    activeInstance.finaliserPaiement();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de finaliser le paiement : contrôleur non disponible");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de vérification");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la finalisation du paiement : " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    private String genererHTMLPaiement(String clientSecret) {
        return "<!DOCTYPE html>\n" +
                "<html><body><h3>Simulation de paiement Stripe</h3></body></html>";
    }

    private void chargerInterfacePaiement() {
        // Non utilisé dans cette version
    }
}
