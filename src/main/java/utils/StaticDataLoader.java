package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticDataLoader {

    public static Map<String, List<String>> loadCountryArenaMap() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Load the JSON file from resources
            InputStream inputStream = StaticDataLoader.class.getResourceAsStream("/arenas.json");
            if (inputStream == null) {
                System.err.println("ERROR: Could not find arenas.json in resources");
                return new HashMap<>();
            }

            // Parse JSON to Map
            return mapper.readValue(inputStream, new TypeReference<Map<String, List<String>>>() {});
        } catch (IOException e) {
            System.err.println("ERROR loading arena data: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}