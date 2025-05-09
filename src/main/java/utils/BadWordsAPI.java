package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class BadWordsAPI {
    private static final String API_KEY = "cUAfZ3Wr3udlXdgwD7qmJig0HHrNt8Y1";
    private static final String API_URL = "https://api.apilayer.com/bad_words";

    public static boolean containsBadWords(String text) {
        try {
            URL url = new URL(API_URL);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("apikey", API_KEY);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setDoOutput(true);

            // Envoyer le texte à vérifier
            connection.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parser la réponse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                
                // Vérifier le nombre de mots interdits
                int badWordsTotal = jsonResponse.optInt("bad_words_total", 0);
                return badWordsTotal > 0;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des mots interdits: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
} 