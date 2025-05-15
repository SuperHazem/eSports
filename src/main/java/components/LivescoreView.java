package components;

import components.LivescoreCard;
import enums.StatutMatch;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.Match;
import services.LivescoreService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LivescoreView extends VBox {
    
    private final Label titleLabel;
    private final Label lastUpdatedLabel;
    private final Button refreshButton;
    private final FlowPane cardsContainer;
    private final LivescoreService livescoreService;
    private final Map<Integer, LivescoreCard> cardMap;
    
    public LivescoreView() {
        // Initialize the service and card map
        livescoreService = new LivescoreService();
        cardMap = new HashMap<>();
        
        // Configure layout
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f0f0f0;"); // Light gray background for better contrast
        
        // Create header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        
        titleLabel = new Label("E-Sports Livescores");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0066cc;"); // Changed to blue for better visibility
        
        lastUpdatedLabel = new Label("Dernière mise à jour: --:--:--");
        lastUpdatedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;"); // Darker text for better readability
        
        refreshButton = new Button("Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> handleRefresh());
        
        header.getChildren().addAll(titleLabel, lastUpdatedLabel, refreshButton);
        
        // Create cards container
        cardsContainer = new FlowPane();
        cardsContainer.setHgap(15);
        cardsContainer.setVgap(15);
        cardsContainer.setPadding(new Insets(10));
        
        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f0f0; -fx-background: #f0f0f0;"); // Match background color
        cardsContainer.setStyle("-fx-background-color: #f0f0f0;"); // Match background color
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Add components to main container
        getChildren().addAll(header, scrollPane);
        
        // Set up listeners
        livescoreService.setMatchUpdateListener(this::updateLivescores);
        livescoreService.setErrorListener(this::handleError);
        
        // Start automatic updates
        livescoreService.startLivescoreUpdates(30);
    }
    
    private void handleRefresh() {
        refreshButton.setDisable(true);
        livescoreService.fetchLivescoresAsync();
    }
    
    public void updateLivescores(List<Match> matches) {
        Platform.runLater(() -> {
            // Sort matches: live first, then upcoming, then finished
            List<Match> sortedMatches = new ArrayList<>(matches);
            sortedMatches.sort(Comparator
                    .<Match>comparingInt(m -> {
                        switch (m.getStatutMatch()) {
                            case EN_COURS: return 0;
                            case EN_ATTENTE: return 1;
                            case TERMINE: return 2;
                            default: return 3;
                        }
                    })
                    .thenComparing(m -> m.getDateMatch()));
            
            // Track which cards are still in use
            Set<Integer> usedMatchIds = new HashSet<>();
            
            // Update or create cards for each match
            for (Match match : sortedMatches) {
                int matchId = match.getMatchId();
                usedMatchIds.add(matchId);
                
                LivescoreCard card = cardMap.get(matchId);
                if (card == null) {
                    // Create new card
                    card = new LivescoreCard();
                    cardMap.put(matchId, card);
                    cardsContainer.getChildren().add(card);
                }
                
                // Update card with match data
                card.updateWithMatch(match);
            }
            
            // Remove cards for matches that are no longer present
            List<Integer> toRemove = new ArrayList<>();
            for (Integer id : cardMap.keySet()) {
                if (!usedMatchIds.contains(id)) {
                    LivescoreCard card = cardMap.get(id);
                    cardsContainer.getChildren().remove(card);
                    card.dispose();
                    toRemove.add(id);
                }
            }
            for (Integer id : toRemove) {
                cardMap.remove(id);
            }
            
            // Update last updated label
            if (livescoreService.getLastUpdated() != null) {
                Instant instant = Instant.parse(livescoreService.getLastUpdated());
                String formattedTime = DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(instant);
                lastUpdatedLabel.setText("Dernière mise à jour: " + formattedTime);
            }
            
            refreshButton.setDisable(false);
        });
    }
    
    private void handleError(Throwable error) {
        Platform.runLater(() -> {
            System.err.println("Error fetching livescores: " + error.getMessage());
            error.printStackTrace();
            refreshButton.setDisable(false);
        });
    }
    
    public void shutdown() {
        // Stop the service
        if (livescoreService != null) {
            livescoreService.shutdown();
        }
        
        // Dispose all cards
        for (LivescoreCard card : cardMap.values()) {
            card.dispose();
        }
        cardMap.clear();
    }
}