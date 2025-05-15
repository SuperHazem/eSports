package services;

import config.ApiConfig;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple fallback AI service implementation
 * This is used when the primary AI service is unavailable
 */
public class FallbackAIService implements AIService {
    private static final Logger LOGGER = Logger.getLogger(FallbackAIService.class.getName());
    private final ApiConfig config;
    
    public FallbackAIService() {
        this.config = ApiConfig.getInstance();
    }
    
    @Override
    public String generateResponse(String prompt, Map<String, Object> options) throws Exception {
        LOGGER.info("Using fallback AI service for prompt: " + prompt.substring(0, Math.min(50, prompt.length())) + "...");
        
        // This is where you would implement the actual fallback service
        // For now, we'll just return a simple response
        if (config.getFallbackApiUrl() != null && !config.getFallbackApiUrl().isEmpty()) {
            LOGGER.info("Would connect to fallback API at: " + config.getFallbackApiUrl());
            // Here you would implement the actual API call to the fallback service
        }
        
        // For demonstration purposes, generate a simple response based on the prompt
        if (prompt.toLowerCase().contains("match") || prompt.toLowerCase().contains("équipe")) {
            return generateSimpleMatchmaking(prompt);
        }
        
        return "Fallback AI service response: Unable to process the request with primary AI service. " +
               "This is a fallback response.";
    }
    
    @Override
    public CompletableFuture<String> generateResponseAsync(String prompt, Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generateResponse(prompt, options);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating fallback response asynchronously: " + e.getMessage(), e);
                return "Error generating fallback response: " + e.getMessage();
            }
        });
    }
    
    @Override
    public boolean isAvailable() {
        // The fallback service is always considered available
        return true;
    }
    
    @Override
    public String getServiceName() {
        return "Fallback AI Service";
    }
    
    /**
     * Generates a simple matchmaking response for demonstration purposes
     */
    private String generateSimpleMatchmaking(String prompt) {
        // Extract team names if present in the prompt
        String[] lines = prompt.split("\\n");
        StringBuilder response = new StringBuilder();
        
        // Look for team names in the prompt
        for (String line : lines) {
            if (line.contains("Équipe") || line.contains("équipe") || 
                line.contains("Team") || line.contains("team")) {
                // Extract team name
                String teamName = line.replaceAll(".*?[Éé]quipe[s]?:?\\s*|.*?[Tt]eam[s]?:?\\s*", "").trim();
                if (!teamName.isEmpty()) {
                    response.append(teamName).append(" vs ");
                }
            }
        }
        
        // If we found teams, create matches
        if (response.length() > 0) {
            // Remove trailing " vs "
            response.setLength(response.length() - 4);
            return response.toString();
        }
        
        // Default response if no teams found
        return "Team A vs Team B\nTeam C vs Team D";
    }
}