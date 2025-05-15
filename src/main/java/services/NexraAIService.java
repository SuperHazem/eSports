package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApiConfig;
import utils.ApiRequestUtil;
import utils.ApiRequestUtil.ApiRequestException;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of AIService using the Nexra API
 */
public class NexraAIService implements AIService {
    private static final Logger LOGGER = Logger.getLogger(NexraAIService.class.getName());
    private final ApiRequestUtil apiRequestUtil;
    private final ObjectMapper mapper;
    private final ApiConfig config;
    
    public NexraAIService() {
        this.apiRequestUtil = new ApiRequestUtil();
        this.mapper = new ObjectMapper();
        this.config = ApiConfig.getInstance();
    }
    
    @Override
    public String generateResponse(String prompt, Map<String, Object> options) throws Exception {
        LOGGER.info("Generating response with Nexra API for prompt: " + prompt.substring(0, Math.min(50, prompt.length())) + "...");
        
        // Prepare the request payload
        Map<String, Object> payload = preparePayload(prompt, options);
        String jsonRequest = mapper.writeValueAsString(payload);
        
        // Create and send the request
        HttpRequest request = apiRequestUtil.createAuthenticatedRequestBuilder(URI.create(config.getGptEndpoint()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();
        
        HttpResponse<String> response = apiRequestUtil.sendRequest(request);
        Map<String, Object> responseMap = mapper.readValue(response.body(), Map.class);
        
        // Get the task ID and poll for the result
        String id = responseMap.get("id").toString();
        return pollForResult(id);
    }
    
    @Override
    public CompletableFuture<String> generateResponseAsync(String prompt, Map<String, Object> options) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generateResponse(prompt, options);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating response asynchronously: " + e.getMessage(), e);
                throw new RuntimeException("Failed to generate response: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // Simple health check
            HttpRequest request = apiRequestUtil.createAuthenticatedRequestBuilder(URI.create(config.getApiUrl()))
                    .GET()
                    .build();
            
            HttpResponse<String> response = apiRequestUtil.sendRequest(request);
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Nexra API is not available: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getServiceName() {
        return "Nexra AI";
    }
    
    private Map<String, Object> preparePayload(String prompt, Map<String, Object> options) {
        Map<String, Object> payload = new HashMap<>();
        
        // Set default model if not provided
        payload.put("model", options.getOrDefault("model", "GPT-4"));
        payload.put("prompt", prompt);
        payload.put("markdown", options.getOrDefault("markdown", false));
        
        // Add messages array if not provided
        if (!options.containsKey("messages")) {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            payload.put("messages", messages);
        } else {
            payload.put("messages", options.get("messages"));
        }
        
        // Add any additional options
        options.forEach((key, value) -> {
            if (!payload.containsKey(key)) {
                payload.put(key, value);
            }
        });
        
        return payload;
    }
    
    private String pollForResult(String id) throws Exception {
        LOGGER.info("Polling for result with task ID: " + id);
        boolean polling = true;
        String content = "";
        int attempts = 0;
        int maxAttempts = config.getMaxRetries() * 2; // More attempts for polling
        
        while (polling && attempts < maxAttempts) {
            try {
                HttpRequest pollRequest = apiRequestUtil.createAuthenticatedRequestBuilder(
                        URI.create(config.getTaskEndpoint() + "/" + id))
                        .GET()
                        .build();
                
                HttpResponse<String> pollResponse = apiRequestUtil.sendRequest(pollRequest);
                Map<String, Object> pollMap = mapper.readValue(pollResponse.body(), Map.class);
                String status = pollMap.get("status").toString();
                
                LOGGER.info("Poll status: " + status);
                
                switch (status) {
                    case "pending":
                        Thread.sleep(1000);
                        attempts++;
                        break;
                    case "completed":
                        if (pollMap.containsKey("response")) {
                            Map<String, Object> responseData = (Map<String, Object>) pollMap.get("response");
                            if (responseData.containsKey("content")) {
                                content = responseData.get("content").toString();
                                LOGGER.info("Received content: " + content.substring(0, Math.min(50, content.length())) + "...");
                            }
                        }
                        polling = false;
                        break;
                    case "error":
                        String errorMessage = "API returned error status";
                        if (pollMap.containsKey("error")) {
                            errorMessage = pollMap.get("error").toString();
                        }
                        LOGGER.severe("Error from API: " + errorMessage);
                        throw new ApiRequestException("Error from API: " + errorMessage, 500, pollResponse.body());
                    case "not_found":
                        LOGGER.severe("Task not found: " + id);
                        throw new ApiRequestException("Task not found: " + id, 404, pollResponse.body());
                    default:
                        LOGGER.warning("Unknown status: " + status);
                        attempts++;
                        Thread.sleep(1000);
                        break;
                }
            } catch (ApiRequestException e) {
                throw e; // Rethrow API exceptions
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error polling for result: " + e.getMessage(), e);
                attempts++;
                Thread.sleep(1000);
            }
        }
        
        if (polling) {
            throw new ApiRequestException("Polling timed out after " + attempts + " attempts", 408, "");
        }
        
        if (content == null || content.trim().isEmpty()) {
            LOGGER.warning("Empty response received from API");
            content = "No content generated";
        }
        
        return content;
    }
}