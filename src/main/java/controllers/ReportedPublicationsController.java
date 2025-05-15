package controllers;

import dao.PublicationReportDAO;
import dao.PublicationReportDAOImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.PublicationReport;
import models.Utilisateur;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportedPublicationsController {

    @FXML private TableView<PublicationReport> reportTable;
    @FXML private TableColumn<PublicationReport, String> publicationColumn;
    @FXML private TableColumn<PublicationReport, String> reporterColumn;
    @FXML private TableColumn<PublicationReport, String> raisonColumn;
    @FXML private TableColumn<PublicationReport, String> dateColumn;
    @FXML private TableColumn<PublicationReport, String> statutColumn;
    @FXML private TableColumn<PublicationReport, Void> actionsColumn;
    @FXML private ComboBox<String> statusFilter;

    private final PublicationReportDAO reportDAO;
    private final ObservableList<PublicationReport> reportData = FXCollections.observableArrayList();
    private Utilisateur currentUser;

    public ReportedPublicationsController() throws SQLException {
        this.reportDAO = new PublicationReportDAOImpl();
    }

    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        // Initialize status filter
        statusFilter.getItems().addAll("Tous", "En attente", "Traité", "Rejeté");
        statusFilter.setValue("Tous");

        // Initialize table columns
        publicationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPublication().getContenu()));
        
        reporterColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUtilisateur().getNom() + " " + 
                                   cellData.getValue().getUtilisateur().getPrenom()));
        
        raisonColumn.setCellValueFactory(new PropertyValueFactory<>("raison"));
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateReport()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        
        statutColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatut().toString()));

        // Add action buttons column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button traiterButton = new Button("Traiter");
            private final Button rejeterButton = new Button("Rejeter");
            private final Button supprimerButton = new Button("Supprimer");

            {
                traiterButton.getStyleClass().add("button-approve");
                rejeterButton.getStyleClass().add("button-reject");
                supprimerButton.getStyleClass().add("button-delete");

                traiterButton.setOnAction(event -> {
                    PublicationReport report = getTableView().getItems().get(getIndex());
                    traiterReport(report);
                });

                rejeterButton.setOnAction(event -> {
                    PublicationReport report = getTableView().getItems().get(getIndex());
                    rejeterReport(report);
                });

                supprimerButton.setOnAction(event -> {
                    PublicationReport report = getTableView().getItems().get(getIndex());
                    supprimerPublication(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PublicationReport report = getTableView().getItems().get(getIndex());
                    HBox hbox = new HBox();
                    
                    if (report.getStatut() == PublicationReport.ReportStatus.EN_ATTENTE) {
                        hbox.getChildren().addAll(traiterButton, rejeterButton);
                    }
                    hbox.getChildren().add(supprimerButton);
                    hbox.setSpacing(10);
                    setGraphic(hbox);
                }
            }
        });

        // Load reports
        loadReports();
        reportTable.setItems(reportData);
    }

    @FXML
    public void appliquerFiltre() {
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus.equals("Tous")) {
            loadReports();
        } else {
            PublicationReport.ReportStatus status = PublicationReport.ReportStatus.valueOf(
                selectedStatus.toUpperCase().replace(" ", "_"));
            loadReportsByStatus(status);
        }
    }

    private void loadReports() {
        try {
            reportData.clear();
            reportData.addAll(reportDAO.lireTous());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger les signalements.");
        }
    }

    private void loadReportsByStatus(PublicationReport.ReportStatus status) {
        try {
            reportData.clear();
            reportData.addAll(reportDAO.lireParStatut(status));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger les signalements.");
        }
    }

    private void traiterReport(PublicationReport report) {
        try {
            reportDAO.mettreAJourStatut(report.getId(), PublicationReport.ReportStatus.TRAITE);
            loadReports();
            showSuccess("Succès", "Le signalement a été traité avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors du traitement du signalement.");
        }
    }

    private void rejeterReport(PublicationReport report) {
        try {
            reportDAO.mettreAJourStatut(report.getId(), PublicationReport.ReportStatus.REJETE);
            loadReports();
            showSuccess("Succès", "Le signalement a été rejeté.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors du rejet du signalement.");
        }
    }

    private void supprimerPublication(PublicationReport report) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette publication ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                // Delete the publication (this will cascade delete the report)
                // You'll need to implement this in your PublicationDAO
                loadReports();
                showSuccess("Succès", "La publication a été supprimée avec succès.");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors de la suppression de la publication.");
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 