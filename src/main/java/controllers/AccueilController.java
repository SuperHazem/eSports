package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AccueilController {
    @FXML
    private Button adminButton;

    @FXML
    private Button userButton;

    public void initialize() {
        // Configurer le bouton administrateur
        adminButton.setOnAction(event -> ouvrirInterfaceAdmin());

        // Configurer le bouton utilisateur
        userButton.setOnAction(event -> ouvrirInterfaceUtilisateur());
    }

    private void ouvrirInterfaceAdmin() {
        try {
            // Charger l'interface administrateur
            URL fxmlUrl = getClass().getResource("/AdminView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AdminView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS si nécessaire
            URL cssUrl = getClass().getResource("/styles/admin-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("E-Sport Manager - Administration");
            stage.setScene(scene);
            stage.setMaximized(true);

            // Fermer la fenêtre d'accueil
            Stage accueilStage = (Stage) adminButton.getScene().getWindow();
            accueilStage.close();

            // Afficher l'interface administrateur
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface administrateur: " + e.getMessage());
        }
    }

    private void ouvrirInterfaceUtilisateur() {
        try {
            // Charger l'interface utilisateur existante
            URL fxmlUrl = getClass().getResource("/TicketView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier TicketView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS si nécessaire
            URL cssUrl = getClass().getResource("/styles/ticket-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("E-Sport Manager - Utilisateur");
            stage.setScene(scene);
            stage.setMaximized(true);

            // Fermer la fenêtre d'accueil
            Stage accueilStage = (Stage) userButton.getScene().getWindow();
            accueilStage.close();

            // Afficher l'interface utilisateur
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface utilisateur: " + e.getMessage());
        }
    }
}