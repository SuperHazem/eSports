package services;

import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import dao.AreneDAO;
import enums.StatutMatch;
import javafx.application.Platform;
import models.Match;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LivescoreService {
    private final MatchDAO matchDAO;
    private final ScheduledExecutorService scheduler;
    private Consumer<List<Match>> matchUpdateListener;
    private Consumer<Throwable> errorListener;
    private String lastUpdated;

    public LivescoreService() {
        try {
            this.matchDAO = new MatchDAO();
            this.scheduler = Executors.newScheduledThreadPool(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize LivescoreService: " + e.getMessage(), e);
        }
    }
    
    public void setMatchUpdateListener(Consumer<List<Match>> listener) {
        this.matchUpdateListener = listener;
    }
    
    public void setErrorListener(Consumer<Throwable> listener) {
        this.errorListener = listener;
    }
    
    public String getLastUpdated() {
        return lastUpdated;
    }
    
    public void startLivescoreUpdates(int seconds) {
        scheduler.scheduleAtFixedRate(this::fetchLivescoresAsync, 0, seconds, TimeUnit.SECONDS);
    }
    
    public void fetchLivescoresAsync() {
        try {
            // Fetch all matches from the local database
            List<Match> allMatches = matchDAO.lireTous();
            
            // Update timestamp
            lastUpdated = Instant.now().toString();
            
            // Notify listener on JavaFX thread
            if (matchUpdateListener != null) {
                Platform.runLater(() -> matchUpdateListener.accept(allMatches));
            }
        } catch (Exception e) {
            if (errorListener != null) {
                Platform.runLater(() -> errorListener.accept(e));
            } else {
                System.err.println("Error fetching livescores: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
    


