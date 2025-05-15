package controllers;

import dao.SiegeDAO;
import dao.TicketDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Siege;
import models.Ticket;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ReservationController {
    @FXML
    private GridPane categorieAGrid;

    @FXML
    private GridPane categorieBGrid;

    @FXML
    private GridPane categorieCGrid;

    @FXML
    private Label selectedSiegeLabel;

    @FXML
    private Label prixLabel;

    @FXML
    private Button reserverButton;

    @FXML
    private Button annulerButton;

    private SiegeDAO siegeDAO;
    private TicketDAO ticketDAO;
    private String selectedSiege = null;
    private double selectedPrix = 0.0;

    public void initialize() {
        try {
            siegeDAO = new SiegeDAO();
            ticketDAO = new TicketDAO();

            // Charger tous les sièges disponibles
            chargerSieges();

            // Configurer les boutons
            reserverButton.setDisable(true);
            reserverButton.setOnAction(event -> reserverSiege());
            annulerButton.setOnAction(event -> fermerFenetre());

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    private void chargerSieges() throws SQLException {
        // Charger les sièges par catégorie
        chargerSiegesCategorie("A", categorieAGrid);
        chargerSiegesCategorie("B", categorieBGrid);
        chargerSiegesCategorie("C", categorieCGrid);
    }

    private void chargerSiegesCategorie(String categorie, GridPane grid) throws SQLException {
        // Effacer la grille existante
        grid.getChildren().clear();

        // Récupérer tous les sièges de cette catégorie
        List<Siege> sieges = siegeDAO.lireSiegesParCategorie(categorie);

        int col = 0;
        int row = 0;
        int maxCols = 6; // Nombre de colonnes dans la grille

        for (Siege siege : sieges) {
            Button seatButton = new Button(siege.getId());
            seatButton.setPrefWidth(60);
            seatButton.setPrefHeight(40);

            // Appliquer le style en fonction de la disponibilité
            if (siege.isDisponible()) {
                seatButton.getStyleClass().add("siege-disponible");
                seatButton.setStyle("-fx-background-color: #FFD700;"); // Jaune pour disponible

                // Ajouter l'action de sélection
                seatButton.setOnAction(event -> {
                    // Désélectionner le siège précédent si nécessaire
                    if (selectedSiege != null) {
                        deselectAllSeats();
                    }

                    // Sélectionner ce siège
                    selectedSiege = siege.getId();
                    selectedPrix = siege.getPrix();
                    seatButton.getStyleClass().add("siege-selectionne");
                    seatButton.setStyle("-fx-background-color: #32CD32;"); // Vert pour sélectionné

                    // Mettre à jour les labels
                    selectedSiegeLabel.setText(selectedSiege);
                    prixLabel.setText(String.format("%.2f dt", selectedPrix));

                    // Activer le bouton de réservation
                    reserverButton.setDisable(false);
                });
            } else {
                seatButton.getStyleClass().add("siege-reserve");
                seatButton.setStyle("-fx-background-color: #A9A9A9;"); // Gris pour réservé
                seatButton.setDisable(true);
            }

            // Ajouter le bouton à la grille
            grid.add(seatButton, col, row);

            // Passer à la colonne suivante ou à la ligne suivante si nécessaire
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }

    private void deselectAllSeats() {
        // Parcourir toutes les grilles et réinitialiser les styles
        for (GridPane grid : new GridPane[]{categorieAGrid, categorieBGrid, categorieCGrid}) {
            for (javafx.scene.Node node : grid.getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    if (button.getText().equals(selectedSiege)) {
                        button.getStyleClass().remove("siege-selectionne");
                        button.setStyle("-fx-background-color: #FFD700;"); // Jaune pour disponible
                    }
                }
            }
        }
    }

    private void reserverSiege() {
        try {
            if (selectedSiege == null) {
                afficherErreur("Erreur", "Veuillez sélectionner un siège");
                return;
            }

            // Créer un nouveau ticket
            Ticket ticket = new Ticket();
            ticket.setSiege(selectedSiege);
            ticket.setPrix(selectedPrix);
            ticket.setDateAchat(new Date());
            ticket.setStatutPaiement("Non payé"); // Par défaut

            // Ajouter le ticket à la base de données
            ticketDAO.ajouter(ticket);

            // Marquer le siège comme réservé
            siegeDAO.marquerCommeReserve(selectedSiege);

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation effectuée");
            alert.setHeaderText(null);
            alert.setContentText("Le siège " + selectedSiege + " a été réservé avec succès.");
            alert.showAndWait();

            // Fermer la fenêtre
            fermerFenetre();

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de réserver le siège: " + e.getMessage());
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