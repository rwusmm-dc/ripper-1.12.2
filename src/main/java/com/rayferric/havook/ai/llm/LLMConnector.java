package com.rayferric.havook.ai.llm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Connects to LLM APIs and handles requests
 * Supports: Gemini, ChatGPT, OpenRouter, Claude
 */
public class LLMConnector {
    private static final int TIMEOUT = 5000; // 5 second timeout
    private static final String USER_AGENT = "Havook-AI/1.0";

    public static String queryAI(String systemPrompt, String userMessage) throws Exception {
        APIKeyManager.APIConfig config = APIKeyManager.getConfig();
        if (!APIKeyManager.isConfigured()) {
            throw new IllegalStateException("AI not configured. Use .ai api=<key> first");
        }

        long startTime = System.currentTimeMillis();
        String response;

        switch (config.provider.toUpperCase()) {
            case "GEMINI":
                response = queryGemini(config, systemPrompt, userMessage);
                break;
            case "CHATGPT":
                response = queryChatGPT(config, systemPrompt, userMessage);
                break;
            case "OPENROUTER":
                response = queryOpenRouter(config, systemPrompt, userMessage);
                break;
            case "CLAUDE":
                response = queryClaude(config, systemPrompt, userMessage);
                break;
            default:
                throw new IllegalArgumentException("Unknown provider: " + config.provider);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > 3000) {
            System.out.println("[AI] Slow response: " + elapsed + "ms");
        }

        return response;
    }

    private static String queryGemini(APIKeyManager.APIConfig config, String systemPrompt, String userMessage) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + config.model + ":generateContent?key=" + config.apiKey;

        JsonObject requestBody = new JsonObject();
        JsonObject contents = new JsonObject();
        
        // System prompt as first message
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt);
        
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", userMessage);

        // Build request
        String payload = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"},{\"text\":\"%s\"}]}]}",
            escapeJson(systemPrompt), escapeJson(userMessage)
        );

        return makeRequest(url, payload, config.apiKey, "gemini");
    }

    private static String queryChatGPT(APIKeyManager.APIConfig config, String systemPrompt, String userMessage) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";

        String payload = String.format(
            "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.7,\"max_tokens\":200}",
            config.model, escapeJson(systemPrompt), escapeJson(userMessage)
        );

        String response = makeRequest(url, payload, config.apiKey, "chatgpt");
        try {
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            return obj.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private static String queryOpenRouter(APIKeyManager.APIConfig config, String systemPrompt, String userMessage) throws Exception {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        String payload = String.format(
            "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.7}",
            config.model, escapeJson(systemPrompt), escapeJson(userMessage)
        );

        String response = makeRequest(url, payload, config.apiKey, "openrouter");
        try {
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            return obj.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private static String queryClaude(APIKeyManager.APIConfig config, String systemPrompt, String userMessage) throws Exception {
        String url = "https://api.anthropic.com/v1/messages";

        String payload = String.format(
            "{\"model\":\"%s\",\"max_tokens\":200,\"system\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
            config.model, escapeJson(systemPrompt), escapeJson(userMessage)
        );

        String response = makeRequest(url, payload, config.apiKey, "claude");
        try {
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            return obj.getAsJsonArray("content").get(0).getAsJsonObject()
                .get("text").getAsString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private static String makeRequest(String urlString, String payload, String apiKey, String provider) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", USER_AGENT);

        if (provider.equals("chatgpt")) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        } else if (provider.equals("claude")) {
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
        } else if (provider.equals("openrouter")) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        }

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();
        InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        if (status >= 400) {
            throw new IOException("API Error (" + status + "): " + response.toString());
        }

        return response.toString();
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
