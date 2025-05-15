package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Ticket;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class QRScannerController {

    @FXML
    private ImageView qrImageView;

    @FXML
    private Button chargerQRButton;

    @FXML
    private Label idLabel;

    @FXML
    private Label siegeLabel;

    @FXML
    private Label prixLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label statutLabel;

    @FXML
    private VBox ticketInfoContainer;

    private Ticket ticketCourant;
    private BufferedImage qrBufferedImage;

    public void initialize() {
        chargerQRButton.setOnAction(event -> sauvegarderQRCode());
    }

    public void setTicket(Ticket ticket) {
        this.ticketCourant = ticket;

        if (ticket != null) {
            // Afficher les informations du ticket
            idLabel.setText("ID: " + ticket.getId());
            siegeLabel.setText("Siège: " + ticket.getSiege());
            prixLabel.setText("Prix: " + ticket.getPrix() + " dt");

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateLabel.setText("Date d'achat: " + dateFormat.format(ticket.getDateAchat()));

            statutLabel.setText("Statut: " + ticket.getStatutPaiement());

            // Rendre visible le conteneur d'informations
            ticketInfoContainer.setVisible(true);

            // Générer le code QR
            genererQRCode();
        }
    }

    private void genererQRCode() {
        try {
            // Créer un objet JSON avec les informations du ticket
            JSONObject ticketJson = new JSONObject();
            ticketJson.put("id", ticketCourant.getId());
            ticketJson.put("siege", ticketCourant.getSiege());
            ticketJson.put("prix", ticketCourant.getPrix());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            ticketJson.put("date", dateFormat.format(ticketCourant.getDateAchat()));

            ticketJson.put("statut", ticketCourant.getStatutPaiement());

            String contenuQR = ticketJson.toString();

            // Configurer le générateur de QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            // Générer la matrice du QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(contenuQR, BarcodeFormat.QR_CODE, 250, 250, hints);

            // Convertir en image
            qrBufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image qrImage = SwingFXUtils.toFXImage(qrBufferedImage, null);

            // Afficher l'image
            qrImageView.setImage(qrImage);

        } catch (WriterException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de générer le code QR: " + e.getMessage());
        }
    }

    private void sauvegarderQRCode() {
        if (qrBufferedImage == null) {
            afficherErreur("Erreur", "Aucun code QR n'a été généré");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le code QR");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
        );
        fileChooser.setInitialFileName("ticket-qr-" + ticketCourant.getId() + "-" + ticketCourant.getSiege() + ".png");

        File file = fileChooser.showSaveDialog(qrImageView.getScene().getWindow());

        if (file != null) {
            try {
                ImageIO.write(qrBufferedImage, "png", file);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sauvegarde réussie");
                alert.setHeaderText(null);
                alert.setContentText("Le code QR a été enregistré avec succès.");
                alert.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                afficherErreur("Erreur", "Impossible d'enregistrer le code QR: " + e.getMessage());
            }
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}