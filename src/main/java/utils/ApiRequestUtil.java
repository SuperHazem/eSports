package utils;

import config.ApiConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling API requests with proper error handling and retry logic
 */
public class ApiRequestUtil {
    private static final Logger LOGGER = Logger.getLogger(ApiRequestUtil.class.getName());
    private final HttpClient client;
    private final ApiConfig config;
    
    public ApiRequestUtil() {
        this.config = ApiConfig.getInstance();
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .build();
    }
    
    /**
     * Sends an HTTP request with retry logic and proper error handling
     * 
     * @param request The HTTP request to send
     * @return The HTTP response
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ApiRequestException If the request fails after all retries
     */
    public HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException, ApiRequestException {
        int retries = 0;
        int maxRetries = config.getMaxRetries();
        int retryDelay = config.getRetryDelay();
        
        while (true) {
            try {
                LOGGER.info("Sending request to: " + request.uri());
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                int statusCode = response.statusCode();
                LOGGER.info("Received response with status code: " + statusCode);
                
                // Log response if enabled
                if (Boolean.parseBoolean(System.getProperty("api.log_responses", "false"))) {
                    LOGGER.info("Response body: " + response.body());
                }
                
                // Handle different status codes
                if (statusCode >= 200 && statusCode < 300) {
                    // Success
                    return response;
                } else if (statusCode == 429) {
                    // Rate limiting - always retry with exponential backoff
                    LOGGER.warning("Rate limit exceeded (429). Retrying after delay...");
                    if (retries >= maxRetries) {
                        throw new ApiRequestException("Rate limit exceeded and max retries reached", statusCode, response.body());
                    }
                    // Exponential backoff
                    Thread.sleep(retryDelay * (long)Math.pow(2, retries));
                    retries++;
                } else if (statusCode >= 500) {
                    // Server error - retry
                    LOGGER.warning("Server error (" + statusCode + "). Retrying...");
                    if (retries >= maxRetries) {
                        throw new ApiRequestException("Server error after max retries", statusCode, response.body());
                    }
                    Thread.sleep(retryDelay);
                    retries++;
                } else if (statusCode == 401 || statusCode == 403) {
                    // Authentication error
                    throw new ApiRequestException("Authentication error", statusCode, response.body());
                } else {
                    // Other client errors
                    throw new ApiRequestException("API request failed", statusCode, response.body());
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Request error: " + e.getMessage(), e);
                if (retries >= maxRetries) {
                    throw e;
                }
                Thread.sleep(retryDelay);
                retries++;
            }
        }
    }
    
    /**
     * Creates an authenticated request builder with proper headers
     * 
     * @param uri The URI for the request
     * @return A request builder with authentication headers
     */
    public HttpRequest.Builder createAuthenticatedRequestBuilder(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .timeout(Duration.ofMillis(config.getTimeout()));
    }
    
    /**
     * Exception class for API request errors with status code and response body
     */
    public static class ApiRequestException extends Exception {
        private final int statusCode;
        private final String responseBody;
        
        public ApiRequestException(String message, int statusCode, String responseBody) {
            super(message + " (Status code: " + statusCode + ")");
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getResponseBody() {
            return responseBody;
        }
    }
}