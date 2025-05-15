package controllers;

import dao.ReclamationDAO;
import dao.ReponseDAO;
import dao.UtilisateurDAO;
import enums.Role;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Reclamation;
import models.Reponse;
import models.Utilisateur;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminNouvelleReponseController {
    @FXML
    private ComboBox<Reclamation> reclamationComboBox;

    @FXML
    private TextArea contenuTextArea;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Utilisateur> adminComboBox;

    @FXML
    private Button annulerButton;

    @FXML
    private Button enregistrerButton;

    private ReponseDAO reponseDAO;
    private ReclamationDAO reclamationDAO;
    private UtilisateurDAO utilisateurDAO;
    private AdminReponseController parentController;
    private Reponse reponseAModifier;
    private boolean modeModification = false;

    public void initialize() {
        try {
            reponseDAO = new ReponseDAO();
            reclamationDAO = new ReclamationDAO();
            utilisateurDAO = new UtilisateurDAO();

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

            // Masquer le combobox administrateur comme demandé
            adminComboBox.setVisible(false);
            adminComboBox.setManaged(false);

            // Masquer également le label correspondant si vous avez un label
            Label adminLabel = (Label) adminComboBox.getParent().lookup("#adminLabel");
            if (adminLabel != null) {
                adminLabel.setVisible(false);
                adminLabel.setManaged(false);
            }

            chargerReclamations();

            annulerButton.setOnAction(event -> fermerFenetre());
            enregistrerButton.setOnAction(event -> enregistrerReponse());

            // Modifier la vérification des champs pour ne pas inclure adminComboBox et datePicker
            contenuTextArea.textProperty().addListener((obs, oldVal, newVal) -> verifierChampsSansAdminEtDate());
            reclamationComboBox.valueProperty().addListener((obs, oldVal, newVal) -> verifierChampsSansAdminEtDate());

        } catch (SQLException e) {
            e.printStackTrace();
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données");
        }
    }

    // Nouvelle méthode pour vérifier les champs sans l'admin et sans la date
    private void verifierChampsSansAdminEtDate() {
        boolean champsRemplis = contenuTextArea.getText() != null && !contenuTextArea.getText().trim().isEmpty()
                && reclamationComboBox.getValue() != null;

        enregistrerButton.setDisable(!champsRemplis);
    }

    public void setParentController(AdminReponseController controller) {
        this.parentController = controller;
    }

    public void setReponse(Reponse reponse) {
        this.reponseAModifier = reponse;
        this.modeModification = true;

        contenuTextArea.setText(reponse.getContenu());

        try {
            Reclamation reclamation = reclamationDAO.lireParId(reponse.getReclamationId());
            reclamationComboBox.setValue(reclamation);

            Utilisateur admin = utilisateurDAO.lire(reponse.getAdminId());
            adminComboBox.setValue(admin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerReclamations() {
        try {
            List<Reclamation> reclamations = reclamationDAO.getReclamationsEnAttente();
            reclamationComboBox.getItems().clear();
            reclamationComboBox.getItems().addAll(reclamations);

            reclamationComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Reclamation reclamation, boolean empty) {
                    super.updateItem(reclamation, empty);
                    if (empty || reclamation == null) {
                        setText(null);
                    } else {
                        setText("Ticket " + reclamation.getTicketId() + " - " + reclamation.getObjet());
                    }
                }
            });

            reclamationComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Reclamation reclamation, boolean empty) {
                    super.updateItem(reclamation, empty);
                    if (empty || reclamation == null) {
                        setText("Sélectionner une réclamation");
                    } else {
                        setText("Ticket " + reclamation.getTicketId() + " - " + reclamation.getObjet());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible de charger les réclamations");
        }
    }

    private void enregistrerReponse() {
        try {
            if (contenuTextArea.getText().trim().isEmpty()
                    || reclamationComboBox.getValue() == null) {

                afficherErreur("Erreur", "Veuillez remplir tous les champs");
                return;
            }

            Reponse reponse;
            if (modeModification) {
                reponse = reponseAModifier;
            } else {
                reponse = new Reponse();
            }

            reponse.setContenu(contenuTextArea.getText().trim());
            reponse.setReclamationId(reclamationComboBox.getValue().getId());

            // Utiliser un admin par défaut (le premier admin trouvé)
            List<Utilisateur> admins = new ArrayList<>();
            for (Utilisateur utilisateur : utilisateurDAO.lireTous()) {
                if (utilisateur.getRole().equals(Role.ADMIN.name())) {
                    admins.add(utilisateur);
                    break; // Prendre le premier admin
                }
            }

            if (!admins.isEmpty()) {
                Utilisateur admin = admins.get(0);
                reponse.setAdminId(admin.getId());
            } else {
                // Fallback si aucun admin n'est trouvé
                reponse.setAdminId(1); // ID par défaut
            }

            // Utiliser la date du jour automatiquement
            reponse.setDate(new Date());

            if (modeModification) {
                reponseDAO.modifier(reponse);
            } else {
                reponseDAO.ajouter(reponse);

                Reclamation reclamation = reclamationComboBox.getValue();
                reclamation.setStatus("Répondu");
                reclamationDAO.updateStatus(reclamation.getId(), "Répondu");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(modeModification ? "Réponse modifiée" : "Réponse ajoutée");
            alert.setHeaderText(null);
            alert.setContentText(modeModification ?
                    "La réponse a été modifiée avec succès." :
                    "La réponse a été ajoutée avec succès.");
            alert.showAndWait();

            if (parentController != null) {
                parentController.rafraichirTableau();
            }

            fermerFenetre();

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'enregistrer la réponse: " + e.getMessage());
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
