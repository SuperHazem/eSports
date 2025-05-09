package controllers;

import dao.SponsorDAO;
import dao.SponsorDAOImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Sponsor;
import utils.validators.SponsorValidator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SponsorController {

    @FXML private TableView<Sponsor> sponsorTable;
    @FXML private TableColumn<Sponsor, String> nomColumn;
    @FXML private TableColumn<Sponsor, String> emailColumn;
    @FXML private TableColumn<Sponsor, String> phoneColumn;
    @FXML private TableColumn<Sponsor, String> addressColumn;
    @FXML private TableColumn<Sponsor, Double> montantColumn;
    @FXML private TableColumn<Sponsor, Void> actionsColumn;

    @FXML private TextField searchNameField;
    @FXML private TextField searchAmountField;

    private final SponsorDAO sponsorDAO;
    private final ObservableList<Sponsor> sponsorData = FXCollections.observableArrayList();

    public SponsorController() throws SQLException {
        this.sponsorDAO = new SponsorDAOImpl();
    }

    @FXML
    public void initialize() {
        try {
            // Initialize table columns
            nomColumn.setCellValueFactory(cellData -> {
                Sponsor sponsor = cellData.getValue();
                String fname = sponsor.getFname();
                String lname = sponsor.getLname();
                return new SimpleStringProperty((fname != null ? fname : "") + " " + (lname != null ? lname : ""));
            });

            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));

            // Add action buttons column
            actionsColumn.setCellFactory(param -> new TableCell<>() {
                private final Button modifierButton = new Button("Modifier");
                private final Button supprimerButton = new Button("Supprimer");

                {
                    modifierButton.getStyleClass().add("button-modifier");
                    supprimerButton.getStyleClass().add("button-supprimer");

                    modifierButton.setOnAction(event -> {
                        Sponsor selectedSponsor = getTableView().getItems().get(getIndex());
                        modifierSponsor(selectedSponsor);
                    });

                    supprimerButton.setOnAction(event -> {
                        Sponsor selectedSponsor = getTableView().getItems().get(getIndex());
                        supprimerSponsor(selectedSponsor);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox(modifierButton, supprimerButton);
                        hbox.setSpacing(10);
                        setGraphic(hbox);
                    }
                }
            });

            // Load all sponsors into the table
            loadSponsors();
            sponsorTable.setItems(sponsorData);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation du contrôleur.");
        }
    }

    @FXML
    public void ajouterSponsor() {
        openPopup(null);
    }

    public void modifierSponsor(Sponsor selectedSponsor) {
        if (selectedSponsor == null) {
            showError("Erreur de sélection", "Aucun sponsor sélectionné pour modification.");
            return;
        }
        openPopup(selectedSponsor);
    }

    public void supprimerSponsor(Sponsor selectedSponsor) {
        if (selectedSponsor == null) {
            showError("Erreur de sélection", "Aucun sponsor sélectionné pour suppression.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Suppression de sponsor");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce sponsor ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    sponsorDAO.supprimer(selectedSponsor.getId());
                    loadSponsors();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur de suppression", "Impossible de supprimer le sponsor.");
                }
            }
        });
    }

    private void openPopup(Sponsor sponsor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SponsorPopup.fxml"));
            VBox popupContent = loader.load();

            SponsorPopupController popupController = loader.getController();
            if (sponsor != null) {
                popupController.setSponsor(sponsor);
            }

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle(sponsor == null ? "Ajouter Sponsor" : "Modifier Sponsor");
            popupStage.setScene(new Scene(popupContent));

            popupStage.showAndWait();

            Sponsor updatedSponsor = popupController.getSponsor();
            if (updatedSponsor != null) {
                if (sponsor == null) {
                    sponsorDAO.ajouter(updatedSponsor);
                } else {
                    sponsorDAO.modifier(updatedSponsor);
                }
                loadSponsors();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la fenêtre pop-up.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue", "Une erreur inattendue est survenue.");
        }
    }

    @FXML
    public void rechercherSponsor() {
        try {
            String searchName = searchNameField.getText().trim();
            String searchAmount = searchAmountField.getText().trim();

            List<Sponsor> sponsors;
            if (!searchName.isEmpty() && !searchAmount.isEmpty()) {
                double montantMin = Double.parseDouble(searchAmount);
                sponsors = sponsorDAO.lireParNom(searchName).stream()
                        .filter(s -> s.getMontant() >= montantMin)
                        .toList();
            } else if (!searchName.isEmpty()) {
                sponsors = sponsorDAO.lireParNom(searchName);
            } else if (!searchAmount.isEmpty()) {
                double montantMin = Double.parseDouble(searchAmount);
                sponsors = sponsorDAO.lireParMontant(montantMin);
            } else {
                showError("Erreur de recherche", "Veuillez entrer un nom ou un montant minimum.");
                return;
            }

            if (!sponsors.isEmpty()) {
                sponsorData.clear();
                sponsorData.addAll(sponsors);
            } else {
                showError("Aucun résultat", "Aucun sponsor trouvé avec ces critères.");
            }
        } catch (NumberFormatException e) {
            showError("Erreur de format", "Le montant doit être un nombre valide.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de recherche", "Une erreur est survenue lors de la recherche.");
        }
    }

    @FXML
    public void afficherTousSponsors() {
        try {
            loadSponsors();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger tous les sponsors.");
        }
    }

    private void loadSponsors() {
        sponsorData.clear();
        sponsorData.addAll(sponsorDAO.lireTous());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 