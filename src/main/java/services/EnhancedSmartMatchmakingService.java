package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApiConfig;
import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import enums.StatutMatch;
import models.Equipe;
import models.Match;
import models.Tournoi;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced Smart Matchmaking Service with improved error handling, authentication, and fallback mechanisms
 */
public class EnhancedSmartMatchmakingService {
    private static final Logger LOGGER = Logger.getLogger(EnhancedSmartMatchmakingService.class.getName());
    
    private final ObjectMapper mapper;
    private final EquipeDAO equipeDAO;
    private final MatchDAO matchDAO;
    private final TournoiDAO tournoiDAO;
    private final ApiConfig apiConfig;
    
    // Primary AI service
    private final AIService primaryService;
    // Fallback AI service
    private final AIService fallbackService;
    
    public EnhancedSmartMatchmakingService() throws SQLException {
        this.mapper = new ObjectMapper();
        this.equipeDAO = new EquipeDAO();
        this.matchDAO = new MatchDAO();
        this.tournoiDAO = new TournoiDAO();
        this.apiConfig = ApiConfig.getInstance();

        // Initialize services
        this.primaryService = new NexraAIService();
        this.fallbackService = new FallbackAIService();

        LOGGER.info("EnhancedSmartMatchmakingService initialized with primary service: " + primaryService.getServiceName());
    }

    /**
     * Generates matches for a tournament using AI services with fallback support
     * 
     * @param tournoiId The tournament ID
     * @return List of generated matches
     */
    public List<Match> generateMatchesForTournament(int tournoiId) {
        LOGGER.info("Starting match generation for tournament ID: " + tournoiId);
        
        try {
            // Retrieve tournament information
            Tournoi tournoi = tournoiDAO.lire(tournoiId);
            if (tournoi == null) {
                LOGGER.severe("Tournament not found with ID: " + tournoiId);
                throw new IllegalArgumentException("Tournament not found with ID: " + tournoiId);
            }
            
            // Extract team IDs associated with the tournament
            String equipesStr = tournoi.getEquipes();
            if (equipesStr == null || equipesStr.trim().isEmpty()) {
                LOGGER.severe("No teams associated with this tournament");
                throw new IllegalArgumentException("No teams associated with this tournament");
            }
            
            // Parse team IDs (expected format: "1,2,3,4,5")
            List<Integer> equipeIds = parseTeamIds(equipesStr);
            if (equipeIds.isEmpty()) {
                LOGGER.severe("No valid team IDs found in the tournament");
                throw new IllegalArgumentException("No valid team IDs found in the tournament");
            }
            
            // Retrieve only teams associated with the tournament
            List<Equipe> equipes = retrieveTeams(equipeIds);
            if (equipes.isEmpty()) {
                LOGGER.severe("No teams available for matchmaking");
                throw new IllegalArgumentException("No teams available for matchmaking");
            }
            
            LOGGER.info("Found " + equipes.size() + " teams for tournament");
            logTeams(equipes);
            
            // Build team data for the AI
            String teamData = buildTeamData(equipes);
            
            // Build the prompt for the AI
            String prompt = buildPrompt(teamData, tournoi.getNom());
            
            // Generate matches using AI with fallback support
            String matchesText = generateMatchesWithFallback(prompt);
            LOGGER.info("AI Response: " + matchesText);
            
            // Process the response to create matches
            return createMatchesFromResponse(matchesText, tournoiId, equipes);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating matches: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Generates matches asynchronously
     * 
     * @param tournoiId The tournament ID
     * @return CompletableFuture with the list of generated matches
     */
    public CompletableFuture<List<Match>> generateMatchesAsync(int tournoiId) {
        return CompletableFuture.supplyAsync(() -> generateMatchesForTournament(tournoiId));
    }
    
    /**
     * Parses team IDs from a comma-separated string
     */
    private List<Integer> parseTeamIds(String equipesStr) {
        List<Integer> equipeIds = new ArrayList<>();
        String[] idStrings = equipesStr.split(",");
        
        for (String idStr : idStrings) {
            try {
                equipeIds.add(Integer.parseInt(idStr.trim()));
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid team ID format: " + idStr);
            }
        }
        
        return equipeIds;
    }
    
    /**
     * Retrieves teams by their IDs
     */
    private List<Equipe> retrieveTeams(List<Integer> equipeIds) {
        List<Equipe> equipes = new ArrayList<>();
        
        for (Integer equipeId : equipeIds) {
            Equipe equipe = null;
            try {
                equipe = equipeDAO.lire(equipeId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (equipe != null) {
                equipes.add(equipe);
            }
        }
        
        return equipes;
    }
    
    /**
     * Logs team information for debugging
     */
    private void logTeams(List<Equipe> equipes) {
        for (Equipe e : equipes) {
            LOGGER.info("Team: " + e.getNom() + ", ID: " + e.getId() + ", Winrate: " + e.getWinRate() + "%");
        }
    }
    
    /**
     * Builds team data string for the AI prompt
     */
    private String buildTeamData(List<Equipe> equipes) {
        StringBuilder teamData = new StringBuilder();
        
        for (Equipe equipe : equipes) {
            teamData.append(equipe.getNom())
                    .append(" - Winrate: ")
                    .append(equipe.getWinRate())
                    .append("%")
                    .append("\n");
        }
        
        return teamData.toString();
    }
    
    /**
     * Builds the prompt for the AI
     */
    private String buildPrompt(String teamData, String tournamentName) {
        return "Équipes dans le tournoi:\n" + teamData + 
               "\nCrée des matchs équilibrés pour le tournoi '" + tournamentName + "' basés sur le winrate des équipes. " +
               "Réponds uniquement avec une liste de matchs au format 'Équipe A vs Équipe B' (un match par ligne).";
    }
    
    /**
     * Generates matches using AI with fallback support
     */
    private String generateMatchesWithFallback(String prompt) throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("model", "GPT-4");
        options.put("markdown", false);
        
        try {
            // Try primary service first
            if (primaryService.isAvailable()) {
                LOGGER.info("Using primary AI service: " + primaryService.getServiceName());
                return primaryService.generateResponse(prompt, options);
            } else {
                LOGGER.warning("Primary AI service unavailable, falling back to: " + fallbackService.getServiceName());
                return fallbackService.generateResponse(prompt, options);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error with primary AI service: " + e.getMessage(), e);
            
            // If fallback is enabled, try the fallback service
            if (apiConfig.isFallbackEnabled()) {
                LOGGER.info("Attempting fallback to: " + fallbackService.getServiceName());
                return fallbackService.generateResponse(prompt, options);
            } else {
                throw e; // Re-throw if fallback is disabled
            }
        }
    }
    
    /**
     * Creates matches from the AI response
     */
    private List<Match> createMatchesFromResponse(String response, int tournoiId, List<Equipe> equipes) {
        LOGGER.info("Processing AI response: " + response);
        List<Match> matches = new ArrayList<>();
        
        // More flexible pattern to handle various response formats
        Pattern pattern = Pattern.compile("([\\w\\s\\-\\.]+)[\\s]*(?:vs|versus|contre)[\\s]*([\\w\\s\\-\\.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);
        
        // Default arena (use the first available)
        int defaultAreneId = 1; // Default ID, adjust according to your database
        
        while (matcher.find()) {
            String equipe1Nom = matcher.group(1).trim();
            String equipe2Nom = matcher.group(2).trim();
            
            LOGGER.info("Extracted match: " + equipe1Nom + " vs " + equipe2Nom);
            
            // Find team IDs
            Equipe equipe1 = findEquipeByName(equipes, equipe1Nom);
            Equipe equipe2 = findEquipeByName(equipes, equipe2Nom);
            
            if (equipe1 != null && equipe2 != null) {
                // Create a new match
                Match match = createMatch(equipe1, equipe2, tournoiId, defaultAreneId);

                // Add to database
                matchDAO.ajouter(match);
                matches.add(match);
                LOGGER.info("Created match: " + equipe1.getNom() + " vs " + equipe2.getNom());
            } else {
                if (equipe1 == null) {
                    LOGGER.warning("Team not found: " + equipe1Nom);
                }
                if (equipe2 == null) {
                    LOGGER.warning("Team not found: " + equipe2Nom);
                }
            }
        }
        
        LOGGER.info("Created " + matches.size() + " matches");
        return matches;
    }
    
    /**
     * Creates a match object
     */
    private Match createMatch(Equipe equipe1, Equipe equipe2, int tournoiId, int areneId) {
        Match match = new Match();
        match.setIdEquipe1(equipe1.getId());
        match.setIdEquipe2(equipe2.getId());
        match.setIdTournoi(tournoiId);
        match.setIdArene(areneId);
        match.setScoreEquipe1(0);
        match.setScoreEquipe2(0);
        match.setDateMatch(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        match.setStatutMatch(StatutMatch.EN_ATTENTE);
        match.setNomJeu("Match automatique");
        return match;
    }
    
    /**
     * Finds a team by name with flexible matching
     */
    private Equipe findEquipeByName(List<Equipe> equipes, String nom) {
        // First try exact match
        for (Equipe equipe : equipes) {
            if (equipe.getNom().equalsIgnoreCase(nom.trim())) {
                return equipe;
            }
        }
        
        // If no exact match, try partial match
        for (Equipe equipe : equipes) {
            if (equipe.getNom().toLowerCase().contains(nom.toLowerCase().trim()) ||
                nom.toLowerCase().trim().contains(equipe.getNom().toLowerCase())) {
                LOGGER.info("Partial match found: '" + nom + "' matches with '" + equipe.getNom() + "'");
                return equipe;
            }
        }
        
        LOGGER.warning("No match found for team: '" + nom + "'");
        return null;
    }
}