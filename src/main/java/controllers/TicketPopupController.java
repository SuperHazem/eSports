package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Ticket;
import models.Siege;
import dao.TicketDAO;
import dao.SiegeDAO;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import utils.SiegeEvent;
import utils.SiegeEventListener;
import utils.SiegeEventManager;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class TicketPopupController implements SiegeEventListener {
    @FXML
    private TextField prixField;
    @FXML
    private Label prixErrorLabel;

    @FXML
    private TextField siegeField;
    @FXML
    private Label siegeErrorLabel;

    @FXML
    private Label categorieLabel;

    @FXML
    private Label placesLibresLabel;

    @FXML
    private FlowPane siegesCategorieA;

    @FXML
    private FlowPane siegesCategorieB;

    @FXML
    private FlowPane siegesCategorieC;

    @FXML
    private Button annulerButton;

    @FXML
    private Button confirmerButton;

    private String siegeSelectionne = null;
    private Ticket ticketCourant;
    private TicketDAO ticketDAO;
    private SiegeDAO siegeDAO;
    private boolean estModification = false;
    private TicketController parentController;
    private String mode = "ajouter"; // "ajouter" ou "modifier"
    private boolean paymentSuccess = false;

    // Configuration des sièges - Alignée avec l'interface admin
    private final int SIEGES_PAR_RANGEE_A = 17;
    private final int SIEGES_PAR_RANGEE_B = 17;
    private final int SIEGES_PAR_RANGEE_C = 17;
    private final int RANGEES_A = 2;
    private final int RANGEES_B = 3;
    private final int RANGEES_C = 4;

    private final Map<String, Button> siegesMap = new HashMap<>();
    private final List<String> siegesReserves = new ArrayList<>();
    private final Set<String> siegesExistants = new HashSet<>();

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initialize() {
        try {
            System.out.println("Initialisation du TicketPopupController");
            ticketDAO = new TicketDAO();
            siegeDAO = new SiegeDAO();

            // Initialiser le ticket courant si c'est un nouveau ticket
            if (ticketCourant == null) {
                ticketCourant = new Ticket();
                ticketCourant.setDateAchat(new Date()); // Date actuelle
                ticketCourant.setStatutPaiement("Non payé"); // Statut par défaut
            }

            // S'inscrire aux événements de siège
            SiegeEventManager.getInstance().ajouterEcouteur(this);

            // Charger les sièges existants depuis la base de données
            chargerSiegesExistants();

            // Charger les sièges réservés
            chargerSiegesReserves();

            // Générer les sièges par catégorie
            genererSiegesCategorie("A", RANGEES_A, SIEGES_PAR_RANGEE_A, siegesCategorieA);
            genererSiegesCategorie("B", RANGEES_B, SIEGES_PAR_RANGEE_B, siegesCategorieB);
            genererSiegesCategorie("C", RANGEES_C, SIEGES_PAR_RANGEE_C, siegesCategorieC);

            // Mettre à jour le nombre de places libres
            mettreAJourPlacesLibres();

            // Configuration des boutons
            annulerButton.setOnAction(this::annuler);
            confirmerButton.setOnAction(this::confirmer);

            // Rendre les champs de prix et siège non éditables (sélection via l'interface graphique)
            prixField.setEditable(false);
            siegeField.setEditable(false);

            System.out.println("Initialisation du TicketPopupController terminée");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    private void chargerSiegesExistants() {
        try {
            siegesExistants.clear();
            siegesExistants.addAll(siegeDAO.getTousLesSiegesIds());
            System.out.println("Sièges existants chargés: " + siegesExistants.size());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des sièges existants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void chargerSiegesReserves() {
        try {
            System.out.println("Chargement des sièges réservés");
            siegesReserves.clear();
            siegesReserves.addAll(ticketDAO.getSiegesReserves());

            // Ne pas considérer le siège du ticket en cours de modification comme réservé
            if (estModification && ticketCourant != null) {
                siegesReserves.remove(ticketCourant.getSiege());
            }
            System.out.println("Nombre de sièges réservés: " + siegesReserves.size());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des sièges réservés: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur de base de données", "Impossible de charger les sièges réservés: " + e.getMessage());
        }
    }

    private void genererSiegesCategorie(String categorie, int nbRangees, int siegesParRangee, FlowPane container) {
        container.getChildren().clear();

        for (int i = 1; i <= nbRangees; i++) {
            for (int j = 1; j <= siegesParRangee; j++) {
                String siegeId = categorie + ((i-1) * siegesParRangee + j);

                // Vérifier si le siège existe dans la base de données
                if (siegesExistants.contains(siegeId) ||
                        (categorie.equals("A") && Integer.parseInt(siegeId.substring(1)) <= 30) ||
                        (categorie.equals("B") && Integer.parseInt(siegeId.substring(1)) <= 46) ||
                        (categorie.equals("C") && Integer.parseInt(siegeId.substring(1)) <= 60)) {

                    Button siegeButton = creerBoutonSiege(siegeId);
                    container.getChildren().add(siegeButton);
                    siegesMap.put(siegeId, siegeButton);
                }
            }
        }
    }

    private Button creerBoutonSiege(String siegeId) {
        Button button = new Button(siegeId);
        button.setPrefSize(40, 40);
        button.setAlignment(Pos.CENTER);

        // Vérifier si le siège est déjà réservé
        if (siegesReserves.contains(siegeId)) {
            button.getStyleClass().add("siege-indisponible");
            button.setDisable(true);
        } else {
            button.getStyleClass().add("siege-disponible");
            button.setOnAction(event -> selectionnerSiege(siegeId, button));
        }

        return button;
    }

    private void selectionnerSiege(String siegeId, Button siegeButton) {
        // Réinitialiser tous les sièges disponibles
        for (Button button : siegesMap.values()) {
            if (!button.isDisabled() && button.getStyleClass().contains("siege-selectionne")) {
                button.getStyleClass().remove("siege-selectionne");
                button.getStyleClass().add("siege-disponible");
            }
        }

        // Marquer le siège sélectionné
        siegeButton.getStyleClass().remove("siege-disponible");
        siegeButton.getStyleClass().add("siege-selectionne");
        siegeSelectionne = siegeId;

        // Mettre à jour les champs d'information
        siegeField.setText(siegeId);
        siegeErrorLabel.setVisible(false);

        // Calculer et afficher le prix en fonction de la catégorie
        double prix = Ticket.calculerPrix(siegeId);
        prixField.setText(String.format("%.2f dt", prix));

        // Afficher la catégorie
        String categorie = "";
        switch (siegeId.charAt(0)) {
            case 'A':
                categorie = "Premium (60dt)";
                break;
            case 'B':
                categorie = "Standard (40dt)";
                break;
            case 'C':
                categorie = "Économique (20dt)";
                break;
            default:
                categorie = siegeId.charAt(0) + " (Prix variable)";
        }
        categorieLabel.setText(categorie);
    }

    private void mettreAJourPlacesLibres() {
        int placesLibres = siegesMap.size() - siegesReserves.size();
        placesLibresLabel.setText(placesLibres + " places libres");
    }

    public void setTicket(Ticket ticket) {
        this.ticketCourant = ticket;
        estModification = true;

        // Remplir les champs avec les données du ticket
        prixField.setText(String.format("%.2f dt", ticket.getPrix()));
        siegeField.setText(ticket.getSiege());

        // Afficher la catégorie
        String categorie = "";
        char premiereLettre = ticket.getSiege().charAt(0);
        switch (premiereLettre) {
            case 'A':
                categorie = "Premium (60dt)";
                break;
            case 'B':
                categorie = "Standard (40dt)";
                break;
            case 'C':
                categorie = "Économique (20dt)";
                break;
            default:
                categorie = premiereLettre + " (Prix variable)";
        }
        categorieLabel.setText(categorie);

        // Sélectionner le siège dans la grille
        siegeSelectionne = ticket.getSiege();
        Button siegeButton = siegesMap.get(siegeSelectionne);
        if (siegeButton != null) {
            siegeButton.getStyleClass().remove("siege-disponible");
            siegeButton.getStyleClass().add("siege-selectionne");
        }
    }

    public void setParentController(TicketController controller) {
        this.parentController = controller;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    private void afficherErreur(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    @FXML
    private void confirmer(ActionEvent event) {
        System.out.println("Confirmation du ticket");

        // Réinitialiser les messages d'erreur
        prixErrorLabel.setVisible(false);
        siegeErrorLabel.setVisible(false);

        boolean isValid = true;
        if (siegeSelectionne == null || siegeSelectionne.isEmpty()) {
            afficherErreur(siegeErrorLabel, "Veuillez sélectionner un siège.");
            isValid = false;
        }

        // Si l'une des vérifications échoue, on arrête ici
        if (!isValid) {
            return;
        }

        try {
            // Mise à jour des données du ticket
            ticketCourant.setPrix(Ticket.calculerPrix(siegeSelectionne));
            ticketCourant.setSiege(siegeSelectionne);
            ticketCourant.setDateAchat(new Date()); // Date actuelle pour les nouveaux tickets
            ticketCourant.setStatutPaiement("Non payé"); // Statut par défaut

            // Sauvegarde
            if (mode.equals("modifier")) {
                System.out.println("Modification du ticket ID=" + ticketCourant.getId() + ", Siège=" + ticketCourant.getSiege());
                ticketDAO.modifier(ticketCourant);

                // Rafraîchir la vue principale
                if (parentController != null) {
                    // IMPORTANT: Utiliser la nouvelle méthode pour recharger l'interface
                    parentController.rechargerInterface();
                }

                // Fermer la fenêtre
                fermerFenetre();
                System.out.println("Ticket modifié avec succès");
            } else {
                // Pour un nouveau ticket, on l'ajoute d'abord
                System.out.println("Ajout d'un nouveau ticket, Siège=" + ticketCourant.getSiege());
                ticketDAO.ajouter(ticketCourant);
                System.out.println("Ticket ajouté avec succès, ID=" + ticketCourant.getId());

                // Puis on ouvre la fenêtre de paiement
                ouvrirInterfacePaiement();
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du ticket: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur lors de la sauvegarde du ticket: " + e.getMessage());
        }
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre();
    }

    private void fermerFenetre() {
        // Se désinscrire des événements de siège
        SiegeEventManager.getInstance().supprimerEcouteur(this);

        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }

    private void ouvrirInterfacePaiement() {
        try {
            System.out.println("Ouverture de l'interface de paiement");

            URL fxmlUrl = getClass().getResource("/PaiementView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier PaiementView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            PaiementController controller = loader.getController();
            controller.setTicket(ticketCourant);
            controller.setParentController(parentController);

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/paiement-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Paiement du ticket");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            // Fermer la fenêtre actuelle
            fermerFenetre();

            // Afficher la fenêtre de paiement
            stage.showAndWait();

            System.out.println("Interface de paiement fermée");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML: " + e.getMessage());
            e.printStackTrace();
            showAlert("Impossible d'ouvrir l'interface de paiement: " + e.getMessage());
        }
    }

    /**
     * Gère les événements de siège
     */
    @Override
    public void onSiegeEvent(SiegeEvent event) {
        // Exécuter sur le thread JavaFX
        javafx.application.Platform.runLater(() -> {
            try {
                if (event.getType().equals(SiegeEvent.SIEGE_AJOUTE)) {
                    Siege nouveauSiege = event.getSiege();
                    String siegeId = nouveauSiege.getId();
                    System.out.println("Événement reçu: Nouveau siège ajouté: " + siegeId);

                    // Ajouter le siège à l'ensemble des sièges existants
                    siegesExistants.add(siegeId);

                    // Vérifier si le siège n'est pas déjà dans l'interface
                    if (!siegesMap.containsKey(siegeId)) {
                        // Créer le bouton pour le nouveau siège
                        Button siegeButton = creerBoutonSiege(siegeId);
                        siegesMap.put(siegeId, siegeButton);

                        // Ajouter le bouton à la catégorie appropriée
                        String categorie = siegeId.substring(0, 1);
                        switch (categorie) {
                            case "A":
                                siegesCategorieA.getChildren().add(siegeButton);
                                break;
                            case "B":
                                siegesCategorieB.getChildren().add(siegeButton);
                                break;
                            case "C":
                                siegesCategorieC.getChildren().add(siegeButton);
                                break;
                        }

                        // Mettre en évidence le nouveau siège
                        siegeButton.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white;");

                        // Restaurer le style normal après 3 secondes
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                                javafx.application.Platform.runLater(() -> {
                                    siegeButton.getStyleClass().add("siege-disponible");
                                    siegeButton.setStyle(null);
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                        // Mettre à jour le nombre de places libres
                        mettreAJourPlacesLibres();

                        // Afficher une notification visuelle
                        afficherNotification("Nouveau siège disponible: " + siegeId);
                    }

                } else if (event.getType().equals(SiegeEvent.SIEGE_SUPPRIME)) {
                    Siege siegeSupprime = event.getSiege();
                    String siegeId = siegeSupprime.getId();
                    System.out.println("TicketPopupController: Événement de suppression reçu pour le siège: " + siegeId);

                    // Retirer le siège de l'ensemble des sièges existants
                    siegesExistants.remove(siegeId);

                    // Récupérer le bouton du siège
                    Button siegeButton = siegesMap.get(siegeId);
                    if (siegeButton != null) {
                        System.out.println("Bouton trouvé pour le siège " + siegeId + ", suppression en cours...");

                        // Déterminer la catégorie pour supprimer du bon conteneur
                        String categorie = siegeId.substring(0, 1);
                        FlowPane container = null;

                        switch (categorie) {
                            case "A":
                                container = siegesCategorieA;
                                break;
                            case "B":
                                container = siegesCategorieB;
                                break;
                            case "C":
                                container = siegesCategorieC;
                                break;
                        }

                        if (container != null) {
                            // Supprimer le bouton du conteneur
                            boolean removed = container.getChildren().remove(siegeButton);
                            System.out.println("Suppression du bouton du conteneur: " + (removed ? "réussie" : "échouée"));

                            // Supprimer de la map
                            siegesMap.remove(siegeId);

                            // Si c'était le siège sélectionné, réinitialiser la sélection
                            if (siegeId.equals(siegeSelectionne)) {
                                siegeSelectionne = null;
                                siegeField.setText("");
                                prixField.setText("");
                                categorieLabel.setText("");
                            }

                            // Mettre à jour le nombre de places libres
                            mettreAJourPlacesLibres();

                            // Afficher une notification visuelle
                            afficherNotification("Siège supprimé: " + siegeId);
                        } else {
                            System.err.println("Conteneur non trouvé pour la catégorie: " + categorie);
                        }
                    } else {
                        System.err.println("Bouton non trouvé pour le siège: " + siegeId);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement de l'événement de siège: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Affiche une notification temporaire à l'utilisateur
     */
    private void afficherNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mise à jour des sièges");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Fermer automatiquement après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        alert.show();
    }

    // Ajouter cette nouvelle méthode pour forcer le rafraîchissement de l'interface
    public void rafraichirInterface() {
        try {
            System.out.println("Rafraîchissement de l'interface TicketPopup...");

            // Recharger les sièges existants depuis la base de données
            chargerSiegesExistants();

            // Recharger les sièges réservés
            chargerSiegesReserves();

            // Vider les conteneurs
            siegesCategorieA.getChildren().clear();
            siegesCategorieB.getChildren().clear();
            siegesCategorieC.getChildren().clear();
            siegesMap.clear();

            // Régénérer les sièges par catégorie
            genererSiegesCategorie("A", RANGEES_A, SIEGES_PAR_RANGEE_A, siegesCategorieA);
            genererSiegesCategorie("B", RANGEES_B, SIEGES_PAR_RANGEE_B, siegesCategorieB);
            genererSiegesCategorie("C", RANGEES_C, SIEGES_PAR_RANGEE_C, siegesCategorieC);

            // Mettre à jour le nombre de places libres
            mettreAJourPlacesLibres();

            System.out.println("Rafraîchissement terminé. Nombre de sièges affichés: " + siegesMap.size());
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement de l'interface: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
