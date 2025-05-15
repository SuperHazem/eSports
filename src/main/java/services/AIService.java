package services;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for AI service providers
 * Allows for easy switching between different AI APIs
 */
public interface AIService {
    /**
     * Sends a prompt to the AI service and returns the response
     * 
     * @param prompt The prompt to send
     * @param options Additional options for the request
     * @return The AI response
     * @throws Exception If an error occurs
     */
    String generateResponse(String prompt, Map<String, Object> options) throws Exception;
    
    /**
     * Sends a prompt to the AI service asynchronously
     * 
     * @param prompt The prompt to send
     * @param options Additional options for the request
     * @return A CompletableFuture that will complete with the AI response
     */
    CompletableFuture<String> generateResponseAsync(String prompt, Map<String, Object> options);
    
    /**
     * Checks if the service is available
     * 
     * @return true if the service is available, false otherwise
     */
    boolean isAvailable();
    
    /**
     * Gets the name of the service
     * 
     * @return The service name
     */
    String getServiceName();
}