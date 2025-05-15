package controllers;

import dao.ReclamationDAO;
import dao.TicketDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import enums.Statut;
import models.Ticket;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReclamationPopupController implements Initializable {

    @FXML
    private TextField txtObjet;

    @FXML
    private TextArea txtDescription;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> comboTicket;

    @FXML
    private Button btnAnnuler;

    @FXML
    private Button btnEnregistrer;

    private ReclamationDAO reclamationDAO;
    private TicketDAO ticketDAO;

    private String mode = "ajouter";
    private Reclamation reclamationAModifier;
    private ReclamationController reclamationController;

    private List<Ticket> tickets;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            reclamationDAO = new ReclamationDAO();
            ticketDAO = new TicketDAO();

            // Masquer le datePicker comme demandé
            if (datePicker != null) {
                datePicker.setVisible(false);
                datePicker.setManaged(false);
            }

            // Masquer également le label correspondant
            Label dateLabel = (Label) datePicker.getParent().lookup("#dateLabel");
            if (dateLabel != null) {
                dateLabel.setVisible(false);
                dateLabel.setManaged(false);
            }

            // Charger les tickets
            chargerTickets();

        } catch (SQLException e) {
            afficherErreur("Erreur de connexion", e.getMessage());
        } catch (Exception e) {
            afficherErreur("Erreur", "Une erreur s'est produite lors de l'initialisation");
        }
    }

    private void chargerTickets() {
        try {
            tickets = ticketDAO.lireTous();
            List<String> ticketsStr = tickets.stream()
                    .map(t -> "T-" + t.getId() + " - " + t.getSiege())
                    .collect(Collectors.toList());
            comboTicket.setItems(FXCollections.observableArrayList(ticketsStr));
        } catch (Exception e) {
            comboTicket.setItems(FXCollections.observableArrayList("T-2024-001 - A1", "T-2024-002 - B3"));
        }
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamationAModifier = reclamation;

        txtObjet.setText(reclamation.getObjet());
        txtDescription.setText(reclamation.getDescription());

        // Sélectionner le ticket
        if (reclamation.getTicket() != null) {
            for (int i = 0; i < tickets.size(); i++) {
                if (tickets.get(i).getId() == reclamation.getTicket().getId()) {
                    comboTicket.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    public void setReclamationController(ReclamationController controller) {
        this.reclamationController = controller;
    }

    @FXML
    private void annuler(ActionEvent event) {
        fermerFenetre();
    }

    @FXML
    private void enregistrer(ActionEvent event) {
        if (!validerFormulaire()) {
            return;
        }

        try {
            Reclamation reclamation;

            if (mode.equals("modifier") && reclamationAModifier != null) {
                reclamation = reclamationAModifier;
            } else {
                reclamation = new Reclamation();
                reclamation.setStatut(Statut.EN_COURS); // Par défaut, une nouvelle réclamation est "En cours"
            }

            reclamation.setObjet(txtObjet.getText());
            reclamation.setDescription(txtDescription.getText());

            // Utiliser la date du jour automatiquement
            reclamation.setDate(new Date());

            // Pour le moment, on utilise un utilisateur fictif ou null
            // Cela sera remplacé par l'utilisateur connecté après l'intégration
            reclamation.setUtilisateur(null);

            // Récupérer le ticket sélectionné
            int indexTicket = comboTicket.getSelectionModel().getSelectedIndex();
            if (indexTicket >= 0 && indexTicket < tickets.size()) {
                reclamation.setTicket(tickets.get(indexTicket));
            } else {
                Ticket ticket = new Ticket();
                ticket.setId(1); // ID par défaut
                reclamation.setTicket(ticket);
            }

            // Enregistrer la réclamation
            if (mode.equals("ajouter")) {
                reclamationDAO.ajouter(reclamation);
                afficherInfo("Succès", "La réclamation a été ajoutée avec succès");
            } else {
                reclamationDAO.modifier(reclamation);
                afficherInfo("Succès", "La réclamation a été modifiée avec succès");
            }

            // Rafraîchir le tableau des réclamations
            if (reclamationController != null) {
                reclamationController.rafraichirTableau();
            }

            fermerFenetre();

        } catch (Exception e) {
            afficherErreur("Erreur", "Une erreur s'est produite lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (txtObjet.getText().isEmpty()) {
            erreurs.append("- L'objet est obligatoire\n");
        }

        if (txtDescription.getText().isEmpty()) {
            erreurs.append("- La description est obligatoire\n");
        }

        if (comboTicket.getSelectionModel().isEmpty()) {
            erreurs.append("- Le ticket est obligatoire\n");
        }

        if (erreurs.length() > 0) {
            afficherErreur("Formulaire incomplet", erreurs.toString());
            return false;
        }

        return true;
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
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
}
