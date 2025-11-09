package com.example.jargon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
public class JargonService {

    // We'll load the key manually now
    private String apiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";


    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        // ✅ Try both methods to read the key
        apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("⚠️ GEMINI_API_KEY not found — running in OFFLINE mode.");
        } else {
            System.out.println("✅ GEMINI_API_KEY detected — running in ONLINE mode.");
        }
    }

    public String translateToJargon(String sentence) throws Exception {

        // Offline fallback (funny responses)
        if (apiKey == null || apiKey.isEmpty()) {
            String[] backups = {
                    "Let's leverage cross-functional synergies to touch base next quarter 🚀 (Offline mode)",
                    "Let's realign our strategic vision offline 🤝 (No API key found)",
                    "Let's circle back asynchronously when Gemini is online ☕",
                    "Let's ideate and revisit this when bandwidth permits 📊",
                    "Let's optimize stakeholder engagement offline until we reconnect 🌐"
            };
            return backups[new Random().nextInt(backups.length)];
        }

        // ✅ Gemini prompt
        String prompt = "Convert this sentence into funny over-the-top corporate jargon. Keep the meaning same. " +
                "Just return the converted text.\n\nSentence: " + sentence;

        // Build request JSON
        String requestBody = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                escapeJson(prompt)
        );

        // Send request
        URL url = new URL(GEMINI_API_URL + "?key=" + apiKey);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
                return responseJson
                        .path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text")
                        .asText();
            } else {
                JsonNode errorJson = objectMapper.readTree(connection.getErrorStream());
                throw new Exception("API Error: " + errorJson.toString());
            }

        } finally {
            connection.disconnect();
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
