package components;

import enums.StatutMatch;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import models.Match;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class LivescoreCard extends VBox {
    
    private final Label gameLabel;
    private final Label tournamentLabel;
    private final Label statusLabel;
    private final Label team1Label;
    private final Label team2Label;
    private final Label scoreLabel;
    private final Label timeLabel;
    private final Circle statusIndicator;
    private final Timeline blinkTimeline;
    private Match match;
    
    public LivescoreCard() {
        // Configure card layout
        setSpacing(8);
        setPadding(new Insets(15));
        setMinWidth(280);
        setMaxWidth(280);
        setPrefWidth(280);
        setMinHeight(200);
        
        // Apply card styling
        setStyle("-fx-background-color: #ffffff; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #dddddd; " +
                "-fx-border-radius: 8;");
        
        // Game and tournament info
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);
        
        gameLabel = new Label();
        gameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");
        
        tournamentLabel = new Label();
        tournamentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #444444;");
        
        headerBox.getChildren().addAll(gameLabel, tournamentLabel);
        
        // Status indicator
        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setSpacing(8);
        
        statusIndicator = new Circle(5);
        statusIndicator.setFill(Color.GRAY);
        
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        statusBox.getChildren().addAll(statusIndicator, statusLabel);
        
        // Teams and scores
        VBox teamsBox = new VBox();
        teamsBox.setAlignment(Pos.CENTER);
        teamsBox.setSpacing(5);
        teamsBox.setPadding(new Insets(10, 0, 10, 0));
        
        team1Label = new Label();
        team1Label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");
        
        team2Label = new Label();
        team2Label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #000000;");
        
        scoreLabel = new Label();
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        teamsBox.getChildren().addAll(team1Label, scoreLabel, team2Label);
        
        // Time/date info
        timeLabel = new Label();
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #444444;");
        timeLabel.setAlignment(Pos.CENTER);
        
        // Add all components to the card
        getChildren().addAll(headerBox, statusBox, teamsBox, timeLabel);
        
        // Create blinking animation for live matches
        blinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> statusIndicator.setFill(Color.RED)),
                new KeyFrame(Duration.seconds(0.5), e -> statusIndicator.setFill(Color.TRANSPARENT)),
                new KeyFrame(Duration.seconds(1))
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    public void updateWithMatch(Match match) {
        if (match == null) {
            return;
        }
        
        this.match = match;
        
        // Update game and tournament
        gameLabel.setText(match.getNomJeu());
        tournamentLabel.setText(match.getTournoiNom());
        
        // Update teams
        team1Label.setText(match.getEquipe1Nom());
        team2Label.setText(match.getEquipe2Nom());
        
        // Update score
        updateScore(match.getScoreEquipe1(), match.getScoreEquipe2());
        
        // Update status
        updateStatus(match.getStatutMatch() != null ? match.getStatutMatch() : StatutMatch.EN_ATTENTE);
        
        // Update time
        if (match.getDateMatch() != null) {
            try {
                LocalDateTime dateTime = match.getDateMatch()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                
                DateTimeFormatter formatter;
                if (match.getStatutMatch() == StatutMatch.EN_ATTENTE) {
                    formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                } else {
                    formatter = DateTimeFormatter.ofPattern("HH:mm");
                }
                
                timeLabel.setText(dateTime.format(formatter));
            } catch (Exception e) {
                timeLabel.setText("");
                System.err.println("Error formatting date: " + e.getMessage());
            }
        } else {
            timeLabel.setText("");
        }
        
        // Apply appropriate styling based on match status
        applyCardStyling(match.getStatutMatch());
    }
    
    public void updateScore(int score1, int score2) {
        scoreLabel.setText(score1 + " - " + score2);
        
        // Apply styling based on match status
        if (match != null) {
            switch (match.getStatutMatch()) {
                case EN_COURS:
                    scoreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #d40000;");
                    break;
                case TERMINE:
                    scoreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #000000;");
                    break;
                case EN_ATTENTE:
                    scoreLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #555555;");
                    break;
            }
        }
    }
    
    private void updateStatus(StatutMatch status) {
        // Stop any existing animation
        blinkTimeline.stop();
        
        // Update status text and color
        switch (status) {
            case EN_COURS:
                statusLabel.setText("LIVE");
                statusLabel.setTextFill(Color.web("#d40000"));
                statusIndicator.setFill(Color.web("#d40000"));
                blinkTimeline.play(); // Start blinking for live matches
                break;
            case TERMINE:
                statusLabel.setText("Terminé");
                statusLabel.setTextFill(Color.web("#007700"));
                statusIndicator.setFill(Color.web("#007700"));
                break;
            case EN_ATTENTE:
                statusLabel.setText("À venir");
                statusLabel.setTextFill(Color.web("#0055aa"));
                statusIndicator.setFill(Color.web("#0055aa"));
                break;
        }
    }
    
    private void applyCardStyling(StatutMatch status) {
        if (status == null) {
            status = StatutMatch.EN_ATTENTE;
        }
        
        // Apply different background and border styles based on match status
        switch (status) {
            case EN_COURS:
                // Highlight live matches with a red shadow and border
                setStyle("-fx-background-color: #ffffff; " +
                        "-fx-effect: dropshadow(gaussian, rgba(212,0,0,0.4), 10, 0, 0, 3); " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #ff6666; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;");
                break;
            case TERMINE:
                // Completed matches with green tint for better visibility
                setStyle("-fx-background-color: #f8fff8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,119,0,0.3), 10, 0, 0, 3); " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #dddddd; " +
                        "-fx-border-radius: 8;");
                break;
            case EN_ATTENTE:
                // Upcoming matches with blue tint for better visibility
                setStyle("-fx-background-color: #f8f8ff; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,85,170,0.3), 10, 0, 0, 3); " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #dddddd; " +
                        "-fx-border-radius: 8;");
                break;
            default:
                // Default styling
                setStyle("-fx-background-color: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); " +
                        "-fx-background-radius: 8;");
                break;
        }
    }
    
    public Match getMatch() {
        return match;
    }
    
    public void dispose() {
        // Stop the animation to prevent memory leaks
        blinkTimeline.stop();
    }
}