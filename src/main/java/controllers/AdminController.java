package controllers;

import controllers.AdminAjoutSiegeController;
import dao.ReponseDAO;
import dao.TicketDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Ticket;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

public class AdminController {
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
    private Button ajouterSiegeButton;

    @FXML
    private Button reponseButton;

    @FXML
    private Button retourButton;

    @FXML
    private PieChart ventesCategorieChart;

    @FXML
    private BarChart<String, Number> revenusChart;

    @FXML
    private Label totalTicketsLabel;

    @FXML
    private Label ticketsPayesLabel;

    @FXML
    private Label ticketsNonPayesLabel;

    @FXML
    private Label revenusTotauxLabel;
    @FXML
    private Button genererRapportButton;

    private TicketDAO ticketDAO;
    private ObservableList<Ticket> ticketsList = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public void initialize() {
        try {
            System.out.println("Initialisation du AdminController");
            ticketDAO = new TicketDAO();

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
            ajouterSiegeButton.setOnAction(event -> ouvrirPopupAjoutSiege());
            reponseButton.setOnAction(event -> ouvrirInterfaceReponse());
            retourButton.setOnAction(event -> retourAccueil());
            if (genererRapportButton != null) {
                genererRapportButton.setOnAction(event -> genererRapportPDF());
            }
            // Charger tous les tickets au démarrage
            afficherTousTickets();

            // Initialiser les statistiques
            initialiserStatistiques();

            System.out.println("Initialisation du AdminController terminée");

        } catch (Exception e) { // Changé de SQLException à Exception
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données: " + e.getMessage());
        }
    }

    private void initialiserStatistiques() {
        try {
            System.out.println("Initialisation des statistiques");
            List<Ticket> tickets = ticketDAO.lireTous();

            // Statistiques de base
            int totalTickets = tickets.size();
            int ticketsPayes = 0;
            double revenuTotal = 0.0;

            // Compteurs par catégorie
            Map<String, Integer> ventesParCategorie = new HashMap<>();
            ventesParCategorie.put("Catégorie A", 0);
            ventesParCategorie.put("Catégorie B", 0);
            ventesParCategorie.put("Catégorie C", 0);

            // Revenus par jour
            Map<String, Double> revenusParJour = new HashMap<>();

            for (Ticket ticket : tickets) {
                // Compter les tickets payés
                if ("Payé".equals(ticket.getStatutPaiement())) {
                    ticketsPayes++;
                    revenuTotal += ticket.getPrix();
                }

                // Compter par catégorie
                String categorie = ticket.getSiege().substring(0, 1);
                switch (categorie) {
                    case "A":
                        ventesParCategorie.put("Catégorie A", ventesParCategorie.get("Catégorie A") + 1);
                        break;
                    case "B":
                        ventesParCategorie.put("Catégorie B", ventesParCategorie.get("Catégorie B") + 1);
                        break;
                    case "C":
                        ventesParCategorie.put("Catégorie C", ventesParCategorie.get("Catégorie C") + 1);
                        break;
                }

                // Regrouper par jour
                String jour = dateFormat.format(ticket.getDateAchat());
                if (!revenusParJour.containsKey(jour)) {
                    revenusParJour.put(jour, 0.0);
                }

                if ("Payé".equals(ticket.getStatutPaiement())) {
                    revenusParJour.put(jour, revenusParJour.get(jour) + ticket.getPrix());
                }
            }

            // Mettre à jour les labels
            totalTicketsLabel.setText(String.valueOf(totalTickets));
            ticketsPayesLabel.setText(String.valueOf(ticketsPayes));
            ticketsNonPayesLabel.setText(String.valueOf(totalTickets - ticketsPayes));
            revenusTotauxLabel.setText(String.format("%.2f dt", revenuTotal));

            // Mettre à jour le graphique en camembert
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : ventesParCategorie.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
            ventesCategorieChart.setData(pieChartData);

            // Mettre à jour le graphique à barres
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenus");

            // Limiter à 7 jours pour la lisibilité
            int count = 0;
            for (Map.Entry<String, Double> entry : revenusParJour.entrySet()) {
                if (count < 7) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    count++;
                } else {
                    break;
                }
            }

            revenusChart.getData().clear();
            revenusChart.getData().add(series);

            System.out.println("Statistiques initialisées avec succès");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des statistiques: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void configurerColonneActions() {
        actionsColumn.setCellFactory(param -> new TableCell<Ticket, Void>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Supprimer");

            {
                modifierBtn.getStyleClass().add("modifier-button");
                supprimerBtn.getStyleClass().add("supprimer-button");

                modifierBtn.setStyle("-fx-background-color: #00b8d9; -fx-text-fill: white;");
                supprimerBtn.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white;");

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
                    // Créer un conteneur pour les boutons d'action
                    HBox buttonsBox = new HBox(5);
                    buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);
                    setGraphic(buttonsBox);
                }
            }
        });
    }


    private void afficherTousTickets() {
        try {
            System.out.println("Affichage de tous les tickets");
            List<Ticket> tickets = ticketDAO.lireTous();
            ticketsList.clear();
            ticketsList.addAll(tickets);
            ticketTableView.setItems(ticketsList);

            // Mettre à jour les statistiques
            initialiserStatistiques();

            System.out.println("Affichage de tous les tickets terminé");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des tickets: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger les tickets: " + e.getMessage());
        }
    }

    private void rechercherTicket() {
        try {
            System.out.println("Recherche de tickets");
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

            System.out.println("Recherche terminée, " + tickets.size() + " tickets trouvés");
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'effectuer la recherche: " + e.getMessage());
        }
    }

    private void supprimerTicket(Ticket ticket) {
        try {
            System.out.println("Suppression du ticket ID=" + ticket.getId() + ", Siège=" + ticket.getSiege());

            // Demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le ticket");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer le ticket pour le siège " + ticket.getSiege() + " ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        ticketDAO.supprimer(ticket.getId());
                        rafraichirTableau();
                        System.out.println("Ticket supprimé avec succès");
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la suppression du ticket: " + e.getMessage());
                        e.printStackTrace();
                        Platform.runLater(() -> afficherErreur("Erreur", "Impossible de supprimer le ticket: " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du ticket: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de supprimer le ticket: " + e.getMessage());
        }
    }

    private void ouvrirPopupAjoutSiege() {
        try {
            System.out.println("Ouverture du popup d'ajout de siège");
            URL fxmlUrl = getClass().getResource("/AdminAjoutSiegeView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AdminAjoutSiegeView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            AdminAjoutSiegeController controller = loader.getController();
            controller.setParentController(this);

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/admin-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Ajouter un siège");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            System.out.println("Popup d'ajout de siège fermé");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du popup d'ajout de siège: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre d'ajout de siège: " + e.getMessage());
        }
    }

    private void ouvrirPopupModification(Ticket ticket) {
        try {
            System.out.println("Ouverture du popup de modification pour le ticket ID=" + ticket.getId() + ", Siège=" + ticket.getSiege());
            URL fxmlUrl = getClass().getResource("/AdminModificationTicketView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AdminModificationTicketView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Correction: Décommentez et corrigez ces lignes
            //AdminModificationTicketController controller = loader.getController();
            //controller.setTicket(ticket);
            //controller.setParentController(this);

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/admin-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Modifier un ticket");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            System.out.println("Popup de modification fermé");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du popup de modification: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    private void ouvrirInterfaceReponse() {
        try {
            System.out.println("Ouverture de l'interface de réponse");
            URL fxmlUrl = getClass().getResource("/AdminReponseView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AdminReponseView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Ajouter les styles CSS
            URL cssUrl = getClass().getResource("/styles/admin-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Réponses aux Réclamations");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();

            System.out.println("Interface de réponse ouverte");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de l'interface de réponse: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir l'interface de réponse: " + e.getMessage());
        }
    }

    private void retourAccueil() {
        try {
            System.out.println("Retour à l'accueil");
            // Charger l'interface d'accueil
            URL fxmlUrl = getClass().getResource("/AccueilView.fxml");
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Impossible de trouver le fichier AccueilView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("E-Sport Manager");
            stage.setScene(scene);

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) retourButton.getScene().getWindow();
            currentStage.close();

            // Afficher l'interface d'accueil
            stage.show();

            System.out.println("Interface d'accueil affichée");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface d'accueil: " + e.getMessage());
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

    public void rafraichirTableau() {
        try {
            System.out.println("Rafraîchissement du tableau");
            // Charger les tickets
            List<Ticket> tickets = ticketDAO.lireTous();
            ticketsList.clear();
            ticketsList.addAll(tickets);

            // Définir les items
            ticketTableView.setItems(ticketsList);

            // Forcer un rafraîchissement
            ticketTableView.refresh();

            // Mettre à jour les statistiques
            initialiserStatistiques();

            System.out.println("Tableau rafraîchi avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement du tableau: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de rafraîchir le tableau des tickets: " + e.getMessage());
        }
    }

    @FXML
    private void genererRapportPDF() {
        try {
            System.out.println("Génération du rapport PDF");
            // Récupérer les données pour le rapport
            List<Ticket> tickets = ticketDAO.lireTous();

            // Calculer les statistiques
            int totalTickets = tickets.size();
            int ticketsPayes = 0;
            double revenuTotal = 0.0;

            Map<String, Integer> ventesParCategorie = new HashMap<>();
            ventesParCategorie.put("Catégorie A", 0);
            ventesParCategorie.put("Catégorie B", 0);
            ventesParCategorie.put("Catégorie C", 0);

            for (Ticket ticket : tickets) {
                if ("Payé".equals(ticket.getStatutPaiement())) {
                    ticketsPayes++;
                    revenuTotal += ticket.getPrix();
                }

                String categorie = ticket.getSiege().substring(0, 1);
                switch (categorie) {
                    case "A":
                        ventesParCategorie.put("Catégorie A", ventesParCategorie.get("Catégorie A") + 1);
                        break;
                    case "B":
                        ventesParCategorie.put("Catégorie B", ventesParCategorie.get("Catégorie B") + 1);
                        break;
                    case "C":
                        ventesParCategorie.put("Catégorie C", ventesParCategorie.get("Catégorie C") + 1);
                        break;
                }
            }

            // Créer le document PDF
            Document document = new Document();
            String fileName = "rapport_statistiques_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            // Ajouter le titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Rapport de Statistiques", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Espace

            // Ajouter la date du rapport
            document.add(new Paragraph("Date du rapport: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())));
            document.add(new Paragraph(" ")); // Espace

            // Ajouter les statistiques générales
            document.add(new Paragraph("Statistiques Générales:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("Nombre total de tickets: " + totalTickets));
            document.add(new Paragraph("Tickets payés: " + ticketsPayes));
            document.add(new Paragraph("Tickets non payés: " + (totalTickets - ticketsPayes)));
            document.add(new Paragraph("Revenus totaux: " + String.format("%.2f dt", revenuTotal)));
            document.add(new Paragraph(" ")); // Espace

            // Ajouter les statistiques par catégorie
            document.add(new Paragraph("Ventes par Catégorie:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            for (Map.Entry<String, Integer> entry : ventesParCategorie.entrySet()) {
                document.add(new Paragraph(entry.getKey() + ": " + entry.getValue() + " tickets"));
            }

            document.close();

            System.out.println("Rapport PDF généré: " + fileName);

            // Ouvrir le fichier PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(fileName));
                System.out.println("Rapport PDF ouvert");
            }

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rapport généré");
            alert.setHeaderText(null);
            alert.setContentText("Le rapport PDF a été généré avec succès: " + fileName);
            alert.showAndWait();

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du rapport PDF: " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de générer le rapport PDF: " + e.getMessage());
        }
    }
}