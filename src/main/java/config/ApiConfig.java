package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for API settings
 * Handles loading and providing access to API credentials and configuration
 */
public class ApiConfig {
    private static final String CONFIG_FILE = "/api.properties";
    private static Properties properties;
    private static ApiConfig instance;
    
    // Default values
    private static final String DEFAULT_API_KEY = "";
    private static final String DEFAULT_API_URL = "https://nexra.aryahcr.cc/api/chat";
    private static final int DEFAULT_TIMEOUT = 30000; // 30 seconds
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_RETRY_DELAY = 1000; // 1 second
    private static final String DEFAULT_FALLBACK_API_URL = "";
    
    private ApiConfig() {
        loadProperties();
    }
    
    public static synchronized ApiConfig getInstance() {
        if (instance == null) {
            instance = new ApiConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("API configuration loaded successfully");
            } else {
                System.out.println("API configuration file not found, using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error loading API configuration: " + e.getMessage());
        }
    }
    
    public String getApiKey() {
        return properties.getProperty("api.key", DEFAULT_API_KEY);
    }
    
    public String getApiUrl() {
        return properties.getProperty("api.url", DEFAULT_API_URL);
    }
    
    public String getGptEndpoint() {
        return getApiUrl() + "/gpt";
    }
    
    public String getTaskEndpoint() {
        return getApiUrl() + "/task";
    }
    
    public int getTimeout() {
        try {
            return Integer.parseInt(properties.getProperty("api.timeout", String.valueOf(DEFAULT_TIMEOUT)));
        } catch (NumberFormatException e) {
            return DEFAULT_TIMEOUT;
        }
    }
    
    public int getMaxRetries() {
        try {
            return Integer.parseInt(properties.getProperty("api.max_retries", String.valueOf(DEFAULT_MAX_RETRIES)));
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_RETRIES;
        }
    }
    
    public int getRetryDelay() {
        try {
            return Integer.parseInt(properties.getProperty("api.retry_delay", String.valueOf(DEFAULT_RETRY_DELAY)));
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_DELAY;
        }
    }
    
    public String getFallbackApiUrl() {
        return properties.getProperty("api.fallback_url", DEFAULT_FALLBACK_API_URL);
    }
    
    public boolean isFallbackEnabled() {
        return Boolean.parseBoolean(properties.getProperty("api.fallback_enabled", "false"));
    }
}