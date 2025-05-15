package controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import dao.MatchDAO;
import enums.StatutMatch;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import models.Match;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class CalendarTabController {

    @FXML
    private VBox calendarContainer;
    
    private CalendarView calendarView;
    private MatchDAO matchDAO;
    
    @FXML
    public void initialize() {
        try {
            // Initialize MatchDAO
            matchDAO = new MatchDAO();
            
            // Create calendar view
            calendarView = new CalendarView();
            
            // Configure calendar view
            configureCalendarView();
            
            // Load matches into calendar
            loadMatchesIntoCalendar();
            
            // Add calendar view to container
            calendarContainer.getChildren().add(calendarView);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Consider adding error handling UI here
        }
    }
    
    private void configureCalendarView() {
        // Set today as the current date
        calendarView.setToday(LocalDate.now());
        calendarView.setTime(LocalTime.now());
        
        // Show the current week
        calendarView.setShowToday(true);
        
        // Make the calendar read-only
        calendarView.setEntryEditPolicy(param -> false);
        calendarView.setEntryContextMenuCallback(param -> null);
        
        // Disable features that might cause errors
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSourceTray(false);
        
        // Fix for week view - disable problematic components
        calendarView.setShowSearchField(false);
        calendarView.setShowSearchResultsTray(false);
        
        // Set the requested view
        calendarView.showWeekPage();
        
        // Set the requested time to 8 AM
        calendarView.setRequestedTime(LocalTime.of(8, 0));
        
        // Add click handler for entries
        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            if (entry != null && entry.getUserObject() instanceof String) {
                // The user object contains the match ID as a String
                String matchIdStr = (String) entry.getUserObject();
                try {
                    int matchId = Integer.parseInt(matchIdStr);
                    showMatchDetails(matchId);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
        
        // Start the thread that updates the time
        Thread updateTimeThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> calendarView.setTime(LocalTime.now()));
                try {
                    // Update every minute
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        // Set as daemon thread to not prevent JVM shutdown
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }
    
    private void loadMatchesIntoCalendar() {
        try {
            // Create calendars for different match statuses
            Calendar pendingMatches = new Calendar("Matches en attente");
            pendingMatches.setStyle(Calendar.Style.STYLE1);
            
            Calendar inProgressMatches = new Calendar("Matches en cours");
            inProgressMatches.setStyle(Calendar.Style.STYLE2);
            
            Calendar completedMatches = new Calendar("Matches terminés");
            completedMatches.setStyle(Calendar.Style.STYLE3);
            
            // Create a calendar source and add the calendars
            CalendarSource matchSource = new CalendarSource("E-Sports Matches");
            matchSource.getCalendars().add(pendingMatches);
            matchSource.getCalendars().add(inProgressMatches);
            matchSource.getCalendars().add(completedMatches);
            
            // Add the source to the calendar view
            calendarView.getCalendarSources().add(matchSource);
            
            // Load all matches
            List<Match> matches = matchDAO.lireTous();
            
            // Convert matches to calendar entries
            for (Match match : matches) {
                Calendar targetCalendar;
                
                // Select the appropriate calendar based on match status
                switch (match.getStatutMatch()) {
                    case EN_ATTENTE:
                        targetCalendar = pendingMatches;
                        break;
                    case EN_COURS:
                        targetCalendar = inProgressMatches;
                        break;
                    case TERMINE:
                        targetCalendar = completedMatches;
                        break;
                    default:
                        targetCalendar = pendingMatches;
                        break;
                }
                
                Entry<String> entry = createEntryFromMatch(match, targetCalendar);
                if (entry != null) {
                    targetCalendar.addEntry(entry);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Entry<String> createEntryFromMatch(Match match, Calendar calendar) {
        try {
            // Create a new calendar entry
            Entry<String> entry = new Entry<>();
            
            // Convert Timestamp to LocalDateTime
            LocalDateTime startDateTime = match.getDateMatch()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            // Set start time
            entry.changeStartDate(startDateTime.toLocalDate());
            entry.changeStartTime(startDateTime.toLocalTime());
            
            // If the match has a duration, set the end time
            if (match.getDuree() != null) {
                LocalDateTime endDateTime = startDateTime.plusMinutes(match.getDuree());
                entry.changeEndDate(endDateTime.toLocalDate());
                entry.changeEndTime(endDateTime.toLocalTime());
            } else {
                // Default duration of 1 hour if not specified
                LocalDateTime endDateTime = startDateTime.plusHours(1);
                entry.changeEndDate(endDateTime.toLocalDate());
                entry.changeEndTime(endDateTime.toLocalTime());
            }
            
            // Set the title (teams)
            String title = match.getEquipe1Nom() + " vs " + match.getEquipe2Nom();
            entry.setTitle(title);
            
            // Set location (arena)
            entry.setLocation(match.getAreneNom());
            // Store the match ID in the user object for later retrieval
            entry.setUserObject(String.valueOf(match.getMatchId()));
            
            // Configure entry display
            entry.setFullDay(false);
            entry.setCalendar(calendar);
            
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null on error
        }
    }
    
    // Add a method to refresh the calendar data
    public void refreshCalendar() {
        // Clear existing entries
        calendarView.getCalendarSources().clear();
        
        // Reload matches
        loadMatchesIntoCalendar();
    }
    
    // Method to show match details when an entry is clicked
    private void showMatchDetails(int matchId) {
        try {
            // Get the match from the database
            Match match = matchDAO.lire(matchId);
            if (match == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Match non trouvé.");
                return;
            }
            
            // Display match details in an alert for now (since DetailMatch.fxml is missing)
            StringBuilder details = new StringBuilder();
            details.append("Date: ").append(match.getDateMatch()).append("\n");
            details.append("Équipes: ").append(match.getEquipe1Nom()).append(" vs ").append(match.getEquipe2Nom()).append("\n");
            details.append("Score: ").append(match.getScoreEquipe1()).append(" - ").append(match.getScoreEquipe2()).append("\n");
            details.append("Tournoi: ").append(match.getTournoiNom()).append("\n");
            details.append("Arène: ").append(match.getAreneNom()).append("\n");
            details.append("Statut: ").append(match.getStatutMatch()).append("\n");
            
            if (match.getVainqueur() != null) {
                String vainqueurNom = match.getVainqueur().equals(match.getIdEquipe1()) ? 
                        match.getEquipe1Nom() : match.getEquipe2Nom();
                details.append("Vainqueur: ").append(vainqueurNom).append("\n");
            }
            
            if (match.getDuree() != null) {
                details.append("Durée: ").append(match.getDuree()).append(" minutes\n");
            }
            
            if (match.getNomJeu() != null) {
                details.append("Jeu: ").append(match.getNomJeu());
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Détails du Match", details.toString());
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible d'afficher les détails du match: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}