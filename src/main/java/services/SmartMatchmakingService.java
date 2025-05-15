package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import enums.StatutMatch;
import models.Equipe;
import models.Match;
import models.Tournoi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartMatchmakingService {
    // OpenAI API configuration
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "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";

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final EquipeDAO equipeDAO;
    private final MatchDAO matchDAO;
    private final TournoiDAO tournoiDAO;

    public SmartMatchmakingService() throws SQLException {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.equipeDAO = new EquipeDAO();
        this.matchDAO = new MatchDAO();
        this.tournoiDAO = new TournoiDAO();
    }

    /**
     * Generates balanced initial matchups for a tournament using AI.
     * @param tournoiId The tournament ID
     * @return List of created matches
     * @throws SQLException if database operations fail
     * @throws Exception if API call fails
     */
    public List<Match> generateMatchesForTournament(int tournoiId) {
        try {
            System.out.println("Starting match generation for tournament ID: " + tournoiId);

            // Validate and retrieve tournament
            Tournoi tournoi = tournoiDAO.lire(tournoiId);
            if (tournoi == null) {
                System.err.println("Error: Tournament with ID " + tournoiId + " not found");
                throw new IllegalArgumentException("Tournoi non trouvé avec l'ID: " + tournoiId);
            }

            // Retrieve and validate teams
            List<Equipe> equipes = getTeamsForTournament(tournoi);
            if (equipes.isEmpty()) {
                System.err.println("Error: No teams available for tournament ID " + tournoiId);
                throw new IllegalArgumentException("Aucune équipe disponible pour le matchmaking");
            }
            System.out.println("Found " + equipes.size() + " teams for tournament");

            // Ensure even number of teams for pairing (add bye if odd)
            if (equipes.size() % 2 != 0) {
                System.out.println("Odd number of teams detected, adding a bye");}

            // Build team data for AI prompt
            StringBuilder teamData = new StringBuilder();
            for (Equipe equipe : equipes) {
                teamData.append(equipe.getNom())
                        .append(" - Winrate: ")
                        .append(equipe.getWinRate())
                        .append("%\n");
            }

            // Construct API prompt
            String prompt = "Équipes dans le tournoi:\n" + teamData.toString() +
                    "\nCrée des matchs équilibrés pour le tournoi '" + tournoi.getNom() + "' basés sur le winrate des équipes. " +
                    "Réponds uniquement avec une liste de matchs au format 'Équipe A vs Équipe B' (un match par ligne). " +
                    "Assure-toi que chaque équipe n'apparaît qu'une seule fois, et gère les byes si nécessaire.";
            System.out.println("Sending prompt to API: " + prompt);

            // Call OpenAI API
            String matchesText = callOpenAIAPI(prompt);
            System.out.println("API Response: " + matchesText);

            // Process API response into matches
            List<Match> matches = createMatchesFromResponse(matchesText, tournoi.getId(), equipes);
            System.out.println("Total matches created: " + matches.size());

            return matches;
        } catch (Exception e) {
            System.err.println("Error during match generation: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all teams associated with a tournament.
     */
    private List<Equipe> getTeamsForTournament(Tournoi tournoi) throws SQLException {
        String equipesStr = tournoi.getEquipes();
        if (equipesStr == null || equipesStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Aucune équipe n'est associée à ce tournoi");
        }

        List<Integer> equipeIds = new ArrayList<>();
        for (String idStr : equipesStr.split(",")) {
            try {
                equipeIds.add(Integer.parseInt(idStr.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Invalid team ID format: " + idStr);
            }
        }

        if (equipeIds.isEmpty()) {
            throw new IllegalArgumentException("Aucun ID d'équipe valide trouvé");
        }

        List<Equipe> equipes = new ArrayList<>();
        for (int equipeId : equipeIds) {
            Equipe equipe = equipeDAO.lire(equipeId);
            if (equipe != null) {
                equipes.add(equipe);
                System.out.println("Team loaded: " + equipe.getNom() + ", ID: " + equipe.getId());
            } else {
                System.err.println("Team with ID " + equipeId + " not found in database");
            }
        }
        return equipes;
    }

    /**
     * Calls the OpenAI API to generate match suggestions.
     */
    private String callOpenAIAPI(String prompt) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o");
        payload.put("messages", List.of(message));
        payload.put("temperature", 0.7);

        String jsonRequest = mapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("API Error: " + response.statusCode() + " - " + response.body());
            throw new Exception("Failed API call: " + response.statusCode());
        }

        Map<String, Object> responseMap = mapper.readValue(response.body(), Map.class);
        List<?> choices = (List<?>) responseMap.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> messageObj = (Map<?, ?>) choice.get("message");
            String content = (String) messageObj.get("content");
            return content != null && !content.trim().isEmpty() ? content : "No matches generated";
        }
        throw new Exception("Invalid API response");
    }

    /**
     * Creates matches from the API response.
     */
    private List<Match> createMatchesFromResponse(String response, int tournoiId, List<Equipe> equipes) {
        System.out.println("Processing API response: " + response);
        List<Match> matches = new ArrayList<>();
        String[] matchLines = response.split("\\n");
        Set<Integer> matchedTeamIds = new HashSet<>();
        int defaultAreneId = 1;

        for (String line : matchLines) {
            if (line.trim().isEmpty()) continue;
            Pattern pattern = Pattern.compile("([\\w\\s\\-\\.]+)[\\s]*(?:vs|versus|contre)[\\s]*([\\w\\s\\-\\.]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String equipe1Name = matcher.group(1).trim();
                String equipe2Name = matcher.group(2).trim();
                System.out.println("Parsed match: '" + equipe1Name + "' vs '" + equipe2Name + "'");

                Equipe equipe1 = findEquipeByName(equipes, equipe1Name);
                Equipe equipe2 = findEquipeByName(equipes, equipe2Name);

                if (equipe1 != null && equipe2 != null) {
                    if (matchedTeamIds.contains(equipe1.getId()) || matchedTeamIds.contains(equipe2.getId())) {
                        System.out.println("Skipping duplicate: " + equipe1Name + " vs " + equipe2Name);
                        continue;
                    }

                    Match match = new Match();
                    match.setIdEquipe1(equipe1.getId());
                    match.setIdEquipe2(equipe2.getId());
                    match.setIdTournoi(tournoiId);
                    match.setIdArene(defaultAreneId);
                    match.setScoreEquipe1(0);
                    match.setScoreEquipe2(0);
                    match.setDateMatch(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
                    match.setStatutMatch(StatutMatch.EN_ATTENTE);

                    matchDAO.ajouter(match);
                    matches.add(match);
                    matchedTeamIds.add(equipe1.getId());
                    matchedTeamIds.add(equipe2.getId());
                    System.out.println("Created match ID: " + match.getMatchId() + " - " + match.getNomJeu());
                } else {
                    System.err.println("Team not found: " + (equipe1 == null ? equipe1Name : equipe2Name));
                }
            }
        }
        return matches;
    }

    /**
     * Finds a team by name with exact or partial matching.
     */
    private Equipe findEquipeByName(List<Equipe> equipes, String name) {
        for (Equipe equipe : equipes) {
            if (equipe.getNom().equalsIgnoreCase(name.trim())) return equipe;
        }
        for (Equipe equipe : equipes) {
            if (equipe.getNom().toLowerCase().contains(name.toLowerCase().trim()) ||
                    name.toLowerCase().trim().contains(equipe.getNom().toLowerCase())) {
                System.out.println("Partial match: '" + name + "' to '" + equipe.getNom() + "'");
                return equipe;
            }
        }
        System.out.println("No match for: '" + name + "'");
        return null;
    }
}