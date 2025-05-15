package controllers;

import dao.UtilisateurDAO;
import enums.Role;
import enums.UserStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import models.Utilisateur;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

public class DashboardController implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label suspendedUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label coachCountLabel;
    @FXML private Label playerCountLabel;
    @FXML private Label spectatorCountLabel;
    @FXML private Label lastRegisteredLabel;
    @FXML private Label avgWinRateLabel;

    @FXML private PieChart userRoleChart;
    @FXML private PieChart userStatusChart;
    @FXML private BarChart<String, Number> userActivityChart;
    @FXML private GridPane statsGridPane;
    @FXML private Button exportPdfButton;

    private UtilisateurDAO utilisateurDAO;
    private List<Utilisateur> allUsers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            utilisateurDAO = new UtilisateurDAO();

            // Check if UI elements are properly initialized
            if (totalUsersLabel == null || activeUsersLabel == null || suspendedUsersLabel == null ||
                    bannedUsersLabel == null || adminCountLabel == null || coachCountLabel == null ||
                    playerCountLabel == null || spectatorCountLabel == null || lastRegisteredLabel == null ||
                    avgWinRateLabel == null || userRoleChart == null || userStatusChart == null ||
                    userActivityChart == null || exportPdfButton == null) {

                showError("UI Initialization Error", "Some UI elements were not properly initialized. Please check your FXML file.");
                return;
            }

            loadData();
            setupCharts();

            exportPdfButton.setOnAction(event -> exportToPdf());
        } catch (SQLException e) {
            showError("Database Error", "Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Initialization Error", "An error occurred during dashboard initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            // Load all users
            allUsers = utilisateurDAO.lireTous();

            // Count users by role
            int adminCount = 0;
            int coachCount = 0;
            int playerCount = 0;
            int spectatorCount = 0;

            // Count users by status
            int activeCount = 0;
            int suspendedCount = 0;
            int bannedCount = 0;

            // Track newest user and calculate average win rate
            LocalDate newestUserDate = null;
            String newestUserName = "";
            double totalWinRate = 0;
            int playerWithWinRateCount = 0;

            for (Utilisateur user : allUsers) {
                // Count by role
                if (user.getRole() != null) {
                    switch (user.getRole()) {
                        case ADMIN:
                            adminCount++;
                            break;
                        case COACH:
                            coachCount++;
                            break;
                        case JOUEUR:
                            playerCount++;
                            // Calculate win rate for players
                            if (user instanceof models.Joueur) {
                                models.Joueur player = (models.Joueur) user;
                                if (player.getWinRate() > 0) {
                                    totalWinRate += player.getWinRate();
                                    playerWithWinRateCount++;
                                }
                            }
                            break;
                        case SPECTATEUR:
                            spectatorCount++;
                            break;
                    }
                }

                // Count by status
                if (user.getStatus() != null) {
                    switch (user.getStatus()) {
                        case ACTIF:
                            activeCount++;
                            break;
                        case SUSPENDU:
                            suspendedCount++;
                            break;
                        case BANNI:
                            bannedCount++;
                            break;
                    }
                } else {
                    // Default to active if status is null
                    activeCount++;
                }

                // Check if this is the newest user
                if (user.getDateNaissance() != null) {
                    if (newestUserDate == null || user.getDateNaissance().isAfter(newestUserDate)) {
                        newestUserDate = user.getDateNaissance();
                        newestUserName = user.getPrenom() + " " + user.getNom();
                    }
                }
            }

            // Calculate average win rate
            double avgWinRate = playerWithWinRateCount > 0 ? totalWinRate / playerWithWinRateCount : 0;

            // Update UI labels
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(allUsers.size()));
            if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(activeCount));
            if (suspendedUsersLabel != null) suspendedUsersLabel.setText(String.valueOf(suspendedCount));
            if (bannedUsersLabel != null) bannedUsersLabel.setText(String.valueOf(bannedCount));

            if (adminCountLabel != null) adminCountLabel.setText(String.valueOf(adminCount));
            if (coachCountLabel != null) coachCountLabel.setText(String.valueOf(coachCount));
            if (playerCountLabel != null) playerCountLabel.setText(String.valueOf(playerCount));
            if (spectatorCountLabel != null) spectatorCountLabel.setText(String.valueOf(spectatorCount));

            if (lastRegisteredLabel != null) {
                if (newestUserDate != null) {
                    lastRegisteredLabel.setText(newestUserName + " (" +
                            newestUserDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
                } else {
                    lastRegisteredLabel.setText("Aucun");
                }
            }

            if (avgWinRateLabel != null) {
                avgWinRateLabel.setText(String.format("%.2f%%", avgWinRate));
            }

        } catch (Exception e) {
            showError("Data Loading Error", "Failed to load user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCharts() {
        try {
            if (userRoleChart != null) setupRoleChart();
            if (userStatusChart != null) setupStatusChart();
            if (userActivityChart != null) setupActivityChart();
        } catch (Exception e) {
            showError("Chart Setup Error", "Failed to set up charts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupRoleChart() {
        // Count users by role
        Map<Role, Integer> roleCounts = new HashMap<>();
        for (Role role : Role.values()) {
            roleCounts.put(role, 0);
        }

        for (Utilisateur user : allUsers) {
            if (user.getRole() != null) {
                Role role = user.getRole();
                roleCounts.put(role, roleCounts.getOrDefault(role, 0) + 1);
            }
        }

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<Role, Integer> entry : roleCounts.entrySet()) {
            if (entry.getValue() > 0) {
                String roleLabel = getRoleFrenchName(entry.getKey());
                pieChartData.add(new PieChart.Data(roleLabel + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }

        userRoleChart.setData(pieChartData);
        userRoleChart.setTitle("Répartition par Rôle");
        userRoleChart.setLegendVisible(true);
        userRoleChart.setLabelsVisible(true);
    }

    private void setupStatusChart() {
        // Count users by status
        int activeCount = 0;
        int suspendedCount = 0;
        int bannedCount = 0;

        for (Utilisateur user : allUsers) {
            if (user.getStatus() != null) {
                switch (user.getStatus()) {
                    case ACTIF:
                        activeCount++;
                        break;
                    case SUSPENDU:
                        suspendedCount++;
                        break;
                    case BANNI:
                        bannedCount++;
                        break;
                }
            } else {
                // Default to active if status is null
                activeCount++;
            }
        }

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        if (activeCount > 0) {
            pieChartData.add(new PieChart.Data("Actif (" + activeCount + ")", activeCount));
        }
        if (suspendedCount > 0) {
            pieChartData.add(new PieChart.Data("Suspendu (" + suspendedCount + ")", suspendedCount));
        }
        if (bannedCount > 0) {
            pieChartData.add(new PieChart.Data("Banni (" + bannedCount + ")", bannedCount));
        }

        userStatusChart.setData(pieChartData);
        userStatusChart.setTitle("Statut des Utilisateurs");
        userStatusChart.setLegendVisible(true);
        userStatusChart.setLabelsVisible(true);
    }

    private void setupActivityChart() {
        try {
            // Get monthly registration counts for the last 6 months
            Connection conn = utilisateurDAO.getConnection();
            Map<String, Integer> monthlyRegistrations = new HashMap<>();

            // Define the last 6 months
            LocalDate now = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                String monthKey = monthDate.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                monthlyRegistrations.put(monthKey, 0);
            }

            // Query to get registration counts by month
            String query = "SELECT MONTH(date_inscription) as month, YEAR(date_inscription) as year, COUNT(*) as count " +
                    "FROM spectateur " +
                    "WHERE date_inscription >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                    "GROUP BY YEAR(date_inscription), MONTH(date_inscription) " +
                    "ORDER BY year, month";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int month = rs.getInt("month");
                    int year = rs.getInt("year");
                    int count = rs.getInt("count");

                    String monthKey = String.format("%02d/%d", month, year);
                    monthlyRegistrations.put(monthKey, count);
                }
            } catch (SQLException e) {
                // Handle SQL exception but continue with default values
                System.err.println("Error querying monthly registrations: " + e.getMessage());
            }

            // Create bar chart series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nouveaux Utilisateurs");

            // Add data points in chronological order
            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = now.minusMonths(i);
                String monthKey = monthDate.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                String displayMonth = monthDate.format(DateTimeFormatter.ofPattern("MMM yy"));

                series.getData().add(new XYChart.Data<>(displayMonth, monthlyRegistrations.getOrDefault(monthKey, 0)));
            }

            userActivityChart.getData().add(series);
            userActivityChart.setTitle("Inscriptions Mensuelles");

        } catch (Exception e) {
            // Handle exception but continue
            System.err.println("Error setting up activity chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToPdf() {
        try {
            // Create file chooser for saving the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("eSportsArena_Dashboard_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

            File file = fileChooser.showSaveDialog(exportPdfButton.getScene().getWindow());
            if (file == null) {
                return; // User cancelled the save
            }

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Create content stream for writing to the PDF
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Add title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("eSportsArena - Tableau de Bord");
            contentStream.endText();

            // Add date
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("Généré le: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            contentStream.endText();

            // Add statistics section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, 690);
            contentStream.showText("Statistiques des Utilisateurs");
            contentStream.endText();

            // Add user counts
            int yPosition = 670;
            addTextToPdf(contentStream, "Nombre total d'utilisateurs: " +
                    (totalUsersLabel != null ? totalUsersLabel.getText() : "N/A"), 50, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "Utilisateurs actifs: " +
                    (activeUsersLabel != null ? activeUsersLabel.getText() : "N/A"), 50, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "Utilisateurs suspendus: " +
                    (suspendedUsersLabel != null ? suspendedUsersLabel.getText() : "N/A"), 50, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "Utilisateurs bannis: " +
                    (bannedUsersLabel != null ? bannedUsersLabel.getText() : "N/A"), 50, yPosition);
            yPosition -= 30;

            // Add role counts
            addTextToPdf(contentStream, "Répartition par Rôle:", 50, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "- Administrateurs: " +
                    (adminCountLabel != null ? adminCountLabel.getText() : "N/A"), 70, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "- Coachs: " +
                    (coachCountLabel != null ? coachCountLabel.getText() : "N/A"), 70, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "- Joueurs: " +
                    (playerCountLabel != null ? playerCountLabel.getText() : "N/A"), 70, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "- Spectateurs: " +
                    (spectatorCountLabel != null ? spectatorCountLabel.getText() : "N/A"), 70, yPosition);
            yPosition -= 30;

            // Add other stats
            addTextToPdf(contentStream, "Dernier utilisateur inscrit: " +
                    (lastRegisteredLabel != null ? lastRegisteredLabel.getText() : "N/A"), 50, yPosition);
            yPosition -= 20;
            addTextToPdf(contentStream, "Taux de victoire moyen des joueurs: " +
                    (avgWinRateLabel != null ? avgWinRateLabel.getText() : "N/A"), 50, yPosition);
            yPosition -= 40;

            // Take screenshots of charts and add them to the PDF if they exist
            // First, create a temporary directory to store the chart images
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "esportsarena_charts");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Capture and add charts only if they exist
            if (userRoleChart != null) {
                try {
                    // Capture role chart
                    File roleChartFile = new File(tempDir, "role_chart.png");
                    captureNodeToFile(userRoleChart, roleChartFile);

                    // Add role chart to PDF
                    PDImageXObject roleChartImage = PDImageXObject.createFromFile(roleChartFile.getAbsolutePath(), document);
                    float roleChartWidth = 250;
                    float roleChartHeight = roleChartWidth * roleChartImage.getHeight() / roleChartImage.getWidth();
                    contentStream.drawImage(roleChartImage, 50, yPosition - roleChartHeight, roleChartWidth, roleChartHeight);

                    // Clean up
                    roleChartFile.delete();
                } catch (Exception e) {
                    System.err.println("Error capturing role chart: " + e.getMessage());
                }
            }

            if (userStatusChart != null) {
                try {
                    // Capture status chart
                    File statusChartFile = new File(tempDir, "status_chart.png");
                    captureNodeToFile(userStatusChart, statusChartFile);

                    // Add status chart to PDF
                    PDImageXObject statusChartImage = PDImageXObject.createFromFile(statusChartFile.getAbsolutePath(), document);
                    float statusChartWidth = 250;
                    float statusChartHeight = statusChartWidth * statusChartImage.getHeight() / statusChartImage.getWidth();
                    contentStream.drawImage(statusChartImage, 300, yPosition - statusChartHeight, statusChartWidth, statusChartHeight);

                    // Clean up
                    statusChartFile.delete();
                } catch (Exception e) {
                    System.err.println("Error capturing status chart: " + e.getMessage());
                }
            }

            // Move down for activity chart
            yPosition -= 300; // Adjust based on previous charts

            if (userActivityChart != null) {
                try {
                    // Capture activity chart
                    File activityChartFile = new File(tempDir, "activity_chart.png");
                    captureNodeToFile(userActivityChart, activityChartFile);

                    // Add activity chart to PDF
                    PDImageXObject activityChartImage = PDImageXObject.createFromFile(activityChartFile.getAbsolutePath(), document);
                    float activityChartWidth = 400;
                    float activityChartHeight = activityChartWidth * activityChartImage.getHeight() / activityChartImage.getWidth();
                    contentStream.drawImage(activityChartImage, 100, yPosition - activityChartHeight, activityChartWidth, activityChartHeight);

                    // Clean up
                    activityChartFile.delete();
                } catch (Exception e) {
                    System.err.println("Error capturing activity chart: " + e.getMessage());
                }
            }

            // Add footer
            contentStream.beginText();
            //contentStream.setFont(PDType1Font.HELVETICA_ITALIC, 10);
            contentStream.newLineAtOffset(50, 50);
            contentStream.showText("eSportsArena - Rapport généré automatiquement");
            contentStream.endText();

            // Close the content stream
            contentStream.close();

            // Save the document
            document.save(file);
            document.close();

            // Clean up temporary directory
            tempDir.delete();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export réussi");
            alert.setHeaderText(null);
            alert.setContentText("Le rapport a été exporté avec succès vers " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (IOException e) {
            showError("Export Error", "Failed to export dashboard to PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addTextToPdf(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void captureNodeToFile(javafx.scene.Node node, File file) throws IOException {
        WritableImage image = node.snapshot(new SnapshotParameters(), null);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }

    private String getRoleFrenchName(Role role) {
        switch (role) {
            case ADMIN: return "Administrateur";
            case COACH: return "Coach";
            case JOUEUR: return "Joueur";
            case SPECTATEUR: return "Spectateur";
            default: return role.toString();
        }
    }

    private void showError(String title, String message) {
        System.err.println(title + ": " + message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}