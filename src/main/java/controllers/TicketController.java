package controllers;

import dao.ReclamationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Ticket;
import models.Siege;
import dao.TicketDAO;
import javafx.scene.control.Alert;
import utils.SiegeEvent;
import utils.SiegeEventListener;
import utils.SiegeEventManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.awt.Desktop;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import javafx.scene.Node;

public class TicketController implements SiegeEventListener {
    @FXML
    private TableView<Ticket> ticketTableView;

    @FXML
    private TableColumn<Ticket, String> siegeColumn;

    @FXML
    private TableColumn<Ticket, Double> prixColumn;

    @FXML
    private TableColumn<Ticket, Date> dateColumn;
    @FXML
    private TableColumn<Ticket, String> statutPaiementColumn;


    @FXML
    private TableColumn<Ticket, Void> actionsColumn;

    @FXML
    private TextField rechercheField;

    @FXML
    private DatePicker rechercheDatePicker;

    @FXML
    private Button rechercherButton;

    @FXML
    private Button afficherTousButton;

    @FXML
    private Button ajouterTicketButton;

    @FXML
    private Button gererReclamationsButton;

    private TicketDAO ticketDAO;
    private ObservableList<Ticket> ticketsList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public void initialize() {
        try {
            ticketDAO = new TicketDAO();

            // IMPORTANT: Définir une taille fixe pour les cellules
            ticketTableView.setFixedCellSize(40);

            // Configurer les colonnes
            siegeColumn.setCellValueFactory(new PropertyValueFactory<>("siege"));
            prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
            statutPaiementColumn.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));

            // Formater la date
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
            dateColumn.setCellFactory(column -> new TableCell<Ticket, Date>() {
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(dateFormat.format(item));
                    }
                }
            });

            // Configurer la colonne d'actions
            configurerColonneActions();

            // Configurer les boutons
            rechercherButton.setOnAction(event -> rechercherTicket());
            afficherTousButton.setOnAction(event -> afficherTousTickets());
            ajouterTicketButton.setOnAction(event -> ouvrirPopupAjout());

            // Charger tous les tickets au démarrage
            afficherTousTickets();

            // S'inscrire aux événements de siège
            SiegeEventManager.getInstance().ajouterEcouteur(this);

            // IMPORTANT: Forcer un rafraîchissement après l'initialisation
            ticketTableView.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    private void configurerColonneActions() {
        actionsColumn.setCellFactory(param -> new TableCell<Ticket, Void>() {
            private final Button modifierBtn = new Button("Modifier ma place");
            private final Button supprimerBtn = new Button("Annuler");

            {
                modifierBtn.getStyleClass().add("modifier-button");
                supprimerBtn.getStyleClass().add("supprimer-button");

                modifierBtn.setOnAction(event -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    ouvrirPopupModification(ticket);
                });

                supprimerBtn.setOnAction(event -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    supprimerTicket(ticket);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    // Créer un conteneur pour les boutons d'action standard
                    HBox buttonsBox = new HBox(5);
                    buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    @FXML
    public void ouvrirScannerQR() {
        Ticket ticketSelectionne = ticketTableView.getSelectionModel().getSelectedItem();

        if (ticketSelectionne == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun ticket sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un ticket dans la liste.");
            alert.showAndWait();
            return;
        }

        ouvrirScannerQRAvecTicket(ticketSelectionne);
    }


    public void rafraichirTableau() {
        try {
            // Charger les tickets
            List<Ticket> tickets = ticketDAO.lireTous();
            ticketsList.clear();
            ticketsList.addAll(tickets);

            // Définir les items
            ticketTableView.setItems(ticketsList);

            // Ajuster la hauteur
            ajusterHauteurTableau();

            // Forcer un rafraîchissement
            ticketTableView.refresh();

            // IMPORTANT: Restaurer la visibilité des boutons
            restaurerVisibiliteBoutons();

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de rafraîchir le tableau des tickets");
        }
    }

    private void afficherTousTickets() {
        try {
            List<Ticket> tickets = ticketDAO.lireTous();
            ticketsList.clear();
            ticketsList.addAll(tickets);
            ticketTableView.setItems(ticketsList);

            // Ajuster la hauteur du tableau
            ajusterHauteurTableau();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger les tickets");
        }
    }

    private void rechercherTicket() {
        try {
            String siegeStr = rechercheField.getText().trim();
            LocalDate localDate = rechercheDatePicker.getValue();
            Date date = null;

            if (localDate != null) {
                date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            if (siegeStr.isEmpty() && date == null) {
                afficherTousTickets();
                return;
            }

            List<Ticket> tickets = ticketDAO.rechercherParSiegeEtDate(siegeStr, date);
            ticketsList.clear();
            ticketsList.addAll(tickets);
            ticketTableView.setItems(ticketsList);

            // Ajuster la hauteur du tableau
            ajusterHauteurTableau();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'effectuer la recherche");
        }
    }

    private void supprimerTicket(Ticket ticket) {
        try {
            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation d'annulation");
            alert.setHeaderText("Annuler la réservation");
            alert.setContentText("Êtes-vous sûr de vouloir annuler votre réservation pour le siège " + ticket.getSiege() + " ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    ticketDAO.supprimer(ticket.getId());
                    rafraichirTableau();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'annuler la réservation");
        }
    }

    private void ouvrirPopupAjout() {
        try {
            URL fxmlUrl = getClass().getResource("/TicketPopup.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier TicketPopup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Obtenir le contrôleur correctement
            TicketPopupController controller = loader.getController();
            controller.setParentController(this);
            controller.setMode("ajouter");

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/Ticket-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Réserver ma place");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du fichier FXML: " + e.getMessage());
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de réservation");
        }
    }

    private void ouvrirPopupModification(Ticket ticket) {
        try {
            URL fxmlUrl = getClass().getResource("/TicketPopup.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier TicketPopup.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            TicketPopupController controller = loader.getController();
            controller.setTicket(ticket);
            controller.setParentController(this);
            controller.setMode("modifier");

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/Ticket-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier ma réservation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du fichier FXML: " + e.getMessage());
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de modification");
        }
    }

    @FXML
    public void ouvrirInterfaceReclamation() {
        try {
            // Vérifier si la colonne statut existe dans la table reclamation et l'ajouter si nécessaire
            try {
                ReclamationDAO reclamationDAO = new ReclamationDAO();
                reclamationDAO.ajouterColonneStatutSiNecessaire();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la vérification de la colonne statut: " + e.getMessage());
            }

            URL fxmlUrl = getClass().getResource("/ReclamationView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier ReclamationView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS programmatiquement
            String cssPath = "/styles/reclamation-style.css";
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS chargé avec succès: " + cssUrl.toExternalForm());
            } else {
                System.err.println("ERREUR: Impossible de trouver le fichier CSS: " + cssPath);
            }

            Stage stage = new Stage();
            stage.setTitle("Réclamations"); // Titre simplifié
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du fichier FXML: " + e.getMessage());
            afficherErreur("Erreur", "Impossible d'ouvrir l'interface de réclamation");
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Une erreur s'est produite: " + e.getMessage());
        }
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null); // Correction de setHeaderTex à setHeaderText
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void ajusterHauteurTableau() {
        // Calculer la hauteur nécessaire pour afficher toutes les lignes sans espace vide
        int nombreLignes = ticketTableView.getItems().size();
        double hauteurEntete = 30; // Hauteur approximative de l'en-tête
        double hauteurLigne = 40; // Hauteur de chaque ligne (doit correspondre à fixed-cell-size dans CSS)
        double hauteurTotale = hauteurEntete + (nombreLignes * hauteurLigne);

        // Définir une hauteur minimale
        double hauteurMinimale = hauteurEntete + (3 * hauteurLigne); // Au moins 3 lignes

        // Appliquer la hauteur calculée
        ticketTableView.setPrefHeight(Math.max(hauteurTotale, hauteurMinimale));
        ticketTableView.setMinHeight(Math.max(hauteurTotale, hauteurMinimale));
        ticketTableView.setMaxHeight(Math.max(hauteurTotale, hauteurMinimale));
    }

    // Nouvelle méthode pour restaurer la visibilité des boutons
    public void restaurerVisibiliteBoutons() {
        try {
            System.out.println("Début de restauration des boutons...");

            // Vérifier si les boutons existent
            if (gererReclamationsButton == null) {
                System.err.println("ERREUR: Le bouton Réclamation est null!");
            }
            if (ajouterTicketButton == null) {
                System.err.println("ERREUR: Le bouton Réserver ma place est null!");
            }

            // Restaurer la visibilité et l'état du bouton "Réclamation"
            if (gererReclamationsButton != null) {
                gererReclamationsButton.setVisible(true);
                gererReclamationsButton.setManaged(true);
                gererReclamationsButton.setDisable(false);
                System.out.println("Bouton Réclamation restauré - Visible: " + gererReclamationsButton.isVisible());
            }

            // Restaurer la visibilité et l'état du bouton "Réserver ma place"
            if (ajouterTicketButton != null) {
                ajouterTicketButton.setVisible(true);
                ajouterTicketButton.setManaged(true);
                ajouterTicketButton.setDisable(false);
                System.out.println("Bouton Réserver ma place restauré - Visible: " + ajouterTicketButton.isVisible());
            }

            // Forcer un rafraîchissement de l'interface
            if (ticketTableView.getScene() != null && ticketTableView.getScene().getWindow() != null) {
                ticketTableView.getScene().getWindow().sizeToScene();
                System.out.println("Redimensionnement de la fenêtre effectué");
            } else {
                System.err.println("ERREUR: Scene ou Window est null!");
            }

            System.out.println("Fin de restauration des boutons");
        } catch (Exception e) {
            System.err.println("Erreur lors de la restauration de la visibilité des boutons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void rechargerInterface() {
        try {
            // Rafraîchir le tableau
            rafraichirTableau();

            // Restaurer la visibilité des boutons
            restaurerVisibiliteBoutons();

            // Forcer un rafraîchissement de la scène
            if (ticketTableView.getScene() != null && ticketTableView.getScene().getWindow() != null) {
                Stage stage = (Stage) ticketTableView.getScene().getWindow();

                // Forcer un redimensionnement minime pour rafraîchir l'interface
                double width = stage.getWidth();
                double height = stage.getHeight();
                stage.setWidth(width + 0.1);
                stage.setHeight(height + 0.1);
                stage.setWidth(width);
                stage.setHeight(height);

                System.out.println("Interface principale rechargée");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rechargement de l'interface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirScannerQRAvecTicket(Ticket ticketSelectionne) {
        try {
            if (ticketSelectionne == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aucun ticket sélectionné");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez sélectionner un ticket pour scanner son code QR.");
                alert.showAndWait();
                return;
            }

            URL fxmlUrl = getClass().getResource("/QRScannerView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier QRScannerView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            QRScannerController controller = loader.getController();
            controller.setTicket(ticketSelectionne);

            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/styles/qrscanner-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Scanner de code QR");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir le scanner QR: " + e.getMessage());
        }
    }

    @FXML
    private void genererRapportPDF() {
        try {
            // Récupérer tous les tickets
            List<Ticket> tickets = ticketDAO.lireTous();

            if (tickets.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Aucun ticket");
                alert.setHeaderText(null);
                alert.setContentText("Il n'y a aucun ticket à inclure dans le rapport.");
                alert.showAndWait();
                return;
            }

            // Demander à l'utilisateur où enregistrer le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("rapport-tickets.pdf");

            File file = fileChooser.showSaveDialog(ticketTableView.getScene().getWindow());

            if (file != null) {
                // Générer le PDF
                utils.PDFGenerator.genererRapportTickets(tickets, file.getAbsolutePath());

                // Demander à l'utilisateur s'il souhaite ouvrir le PDF
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Rapport généré");
                alert.setHeaderText("Le rapport PDF a été généré avec succès.");
                alert.setContentText("Voulez-vous ouvrir le fichier maintenant?");

                if (alert.showAndWait().get() == ButtonType.OK) {
                    // Ouvrir le PDF avec l'application par défaut
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                        infoAlert.setTitle("Information");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText("Le rapport a été enregistré à: " + file.getAbsolutePath());
                        infoAlert.showAndWait();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de générer le rapport PDF: " + e.getMessage());
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
                    System.out.println("TicketController: Événement reçu - Nouveau siège ajouté: " + nouveauSiege.getId());

                    // Rafraîchir le tableau des tickets
                    rafraichirTableau();

                    // Afficher une notification
                    afficherNotification("Nouveau siège disponible",
                            "Un nouveau siège a été ajouté: " + nouveauSiege.getId(),
                            Alert.AlertType.INFORMATION);

                } else if (event.getType().equals(SiegeEvent.SIEGE_SUPPRIME)) {
                    Siege siegeSupprime = event.getSiege();
                    System.out.println("TicketController: Événement reçu - Siège supprimé: " + siegeSupprime.getId());

                    // Vérifier si le siège supprimé est utilisé dans un ticket
                    boolean siegeUtilise = false;
                    for (Ticket ticket : ticketTableView.getItems()) {
                        if (ticket.getSiege().equals(siegeSupprime.getId())) {
                            siegeUtilise = true;
                            break;
                        }
                    }

                    if (siegeUtilise) {
                        // Afficher un avertissement si le siège est utilisé
                        afficherNotification("Attention",
                                "Le siège " + siegeSupprime.getId() + " a été supprimé mais est utilisé dans un ou plusieurs tickets.",
                                Alert.AlertType.WARNING);
                    } else {
                        // Afficher une notification standard
                        afficherNotification("Siège supprimé",
                                "Le siège " + siegeSupprime.getId() + " a été supprimé",
                                Alert.AlertType.INFORMATION);
                    }

                    // Rafraîchir le tableau des tickets
                    rafraichirTableau();

                    // Si une fenêtre de réservation est ouverte, la rafraîchir également
                    rafraichirFenetresOuvertes();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement de l'événement de siège: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Ajouter cette nouvelle méthode pour rafraîchir les fenêtres de réservation ouvertes
    private void rafraichirFenetresOuvertes() {
        try {
            // Parcourir toutes les fenêtres ouvertes
            for (Window window : Stage.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    // Vérifier si c'est une fenêtre de réservation
                    if (stage.getTitle() != null &&
                            (stage.getTitle().contains("Réserver ma place") ||
                                    stage.getTitle().contains("Modifier ma réservation"))) {

                        System.out.println("Rafraîchissement d'une fenêtre de réservation ouverte: " + stage.getTitle());

                        // Récupérer la scène et le contrôleur
                        Scene scene = stage.getScene();
                        if (scene != null && scene.getRoot() != null) {
                            // Chercher le contrôleur TicketPopupController
                            for (Node node : scene.getRoot().lookupAll("*")) {
                                if (node.getUserData() instanceof TicketPopupController) {
                                    TicketPopupController controller = (TicketPopupController) node.getUserData();
                                    // Rafraîchir l'interface
                                    controller.rafraichirInterface();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement des fenêtres ouvertes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une notification à l'utilisateur
     */
    private void afficherNotification(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
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
}
