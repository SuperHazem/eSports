package controllers;

import dao.EquipeDAO;
import dao.RecompenseDAOImpl;
import enums.TypeRecompense;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Equipe;
import models.Recompense;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecompensePopupController {

    @FXML private ComboBox<Equipe> equipeComboBox;
    @FXML private ComboBox<TypeRecompense> typeComboBox;
    @FXML private TextField valeurField;
    @FXML private DatePicker dateAttribution;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private RecompenseDAOImpl recompenseDAO;
    private EquipeDAO equipeDAO;
    private Recompense recompense;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        // Initialize date picker with current date
        dateAttribution.setValue(LocalDate.now());

        // Initialize type combo box
        typeComboBox.setItems(FXCollections.observableArrayList(TypeRecompense.values()));

        // Add listener to type combo box to handle value field formatting
        typeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                // For TROPHEE type, set value to 0 and disable the field
                if (newValue == TypeRecompense.TROPHEE) {
                    valeurField.setText("0");
                    valeurField.setDisable(true);
                } else {
                    valeurField.setDisable(false);
                }
            }
        });
    }

    public void setRecompenseDAO(RecompenseDAOImpl recompenseDAO) {
        this.recompenseDAO = recompenseDAO;
    }

    public void setEquipeDAO(EquipeDAO equipeDAO) {
        this.equipeDAO = equipeDAO;
        loadEquipes();
    }

    public void setRecompense(Recompense recompense) {
        this.recompense = recompense;
        populateFields();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void loadEquipes() {
        List<Equipe> equipes = equipeDAO.lireTous();
        equipeComboBox.setItems(FXCollections.observableArrayList(equipes));

        // Set a custom cell factory to display team names
        equipeComboBox.setCellFactory(param -> new ListCell<Equipe>() {
            @Override
            protected void updateItem(Equipe item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });

        // Set a custom string converter for the selected value
        equipeComboBox.setConverter(new javafx.util.StringConverter<Equipe>() {
            @Override
            public String toString(Equipe equipe) {
                return equipe == null ? "" : equipe.getNom();
            }

            @Override
            public Equipe fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
    }

    private void populateFields() {
        if (recompense != null) {
            // Set the equipe
            equipeComboBox.getSelectionModel().select(recompense.getEquipe());

            // Set the type
            typeComboBox.setValue(recompense.getType());

            // Set the value
            valeurField.setText(String.valueOf(recompense.getValeur()));

            // Set the description
            descriptionArea.setText(recompense.getDescription());

            // Set the date - Fixed to handle java.sql.Date properly
            if (recompense.getDateAttribution() != null) {
                // Convert java.util.Date to LocalDate safely
                Date date = recompense.getDateAttribution();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                LocalDate localDate = LocalDate.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1, // Month is 0-based in Calendar
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                dateAttribution.setValue(localDate);
            }
        }
    }

    @FXML
    public void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            Equipe selectedEquipe = equipeComboBox.getValue();
            TypeRecompense selectedType = typeComboBox.getValue();
            double valeur = Double.parseDouble(valeurField.getText().trim());
            String description = descriptionArea.getText();

            // Convert LocalDate to java.util.Date
            LocalDate localDate = dateAttribution.getValue();
            Calendar calendar = Calendar.getInstance();
            calendar.set(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            Date date = calendar.getTime();

            if (recompense == null) {
                // Create new reward
                recompense = new Recompense(0, selectedType, valeur, selectedEquipe, description, date);
                recompenseDAO.ajouter(recompense);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Récompense ajoutée avec succès.");
            } else {
                // Update existing reward
                recompense.setEquipe(selectedEquipe);
                recompense.setType(selectedType);
                recompense.setValeur(valeur);
                recompense.setDescription(description);
                recompense.setDateAttribution(date);
                recompenseDAO.modifier(recompense);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Récompense modifiée avec succès.");
            }

            // Call the callback to refresh the main table
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            // Close the window
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "La valeur doit être un nombre valide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (equipeComboBox.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une équipe.\n");
        }

        if (typeComboBox.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner un type de récompense.\n");
        }

        if (valeurField.getText().trim().isEmpty() && typeComboBox.getValue() != TypeRecompense.TROPHEE) {
            errorMessage.append("- Veuillez entrer une valeur.\n");
        } else if (!valeurField.getText().trim().isEmpty()) {
            try {
                double value = Double.parseDouble(valeurField.getText().trim());
                if (value < 0) {
                    errorMessage.append("- La valeur ne peut pas être négative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("- La valeur doit être un nombre valide.\n");
            }
        }

        if (dateAttribution.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une date d'attribution.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs suivantes:\n" + errorMessage.toString());
            return false;
        }

        return true;
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            dialogPane.getStyleClass().add("alert-dialog");
        } catch (Exception e) {
            System.err.println("Could not load CSS for alert: " + e.getMessage());
        }

        alert.showAndWait();
    }
}