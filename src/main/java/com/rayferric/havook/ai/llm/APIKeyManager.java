package com.rayferric.havook.ai.llm;

import java.util.*;
import java.io.*;

/**
 * Manages API keys and model selection
 * Supports: Gemini, ChatGPT, OpenRouter, Claude
 */
public class APIKeyManager {
    public enum APIProvider {
        GEMINI("Gemini", "sk-", "gemini-"),
        CHATGPT("ChatGPT", "sk-", "gpt-"),
        OPENROUTER("OpenRouter", "sk-", "openrouter/"),
        CLAUDE("Claude", "sk-", "claude-"),
        CUSTOM("Custom", "", "");

        public final String displayName;
        public final String keyPrefix;
        public final String modelPrefix;

        APIProvider(String displayName, String keyPrefix, String modelPrefix) {
            this.displayName = displayName;
            this.keyPrefix = keyPrefix;
            this.modelPrefix = modelPrefix;
        }
    }

    public static class APIConfig {
        public String provider;
        public String apiKey;
        public String model;
        public String endpoint;

        public APIConfig(String provider, String apiKey, String model) {
            this.provider = provider;
            this.apiKey = apiKey;
            this.model = model;
        }

        @Override
        public String toString() {
            return String.format("APIConfig{provider=%s, model=%s, keySet=%b}",
                provider, model, apiKey != null && !apiKey.isEmpty());
        }
    }

    private static APIConfig currentConfig = null;
    private static final File CONFIG_FILE = new File("havook_ai_config.txt");

    public static boolean setAPIKey(String keyWithProvider) {
        // Format: "provider:key:model"
        String[] parts = keyWithProvider.split(":");
        if (parts.length < 2) return false;

        String provider = parts[0].toUpperCase();
        String key = parts[1];
        String model = parts.length > 2 ? parts[2] : getDefaultModel(provider);

        try {
            APIProvider.valueOf(provider);
            currentConfig = new APIConfig(provider, key, model);
            saveConfig();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean setModel(String modelName) {
        if (currentConfig == null) return false;
        currentConfig.model = modelName;
        saveConfig();
        return true;
    }

    public static APIConfig getConfig() {
        if (currentConfig == null) {
            loadConfig();
        }
        return currentConfig;
    }

    public static boolean isConfigured() {
        return currentConfig != null && currentConfig.apiKey != null && !currentConfig.apiKey.isEmpty();
    }

    private static String getDefaultModel(String provider) {
        switch (provider.toUpperCase()) {
            case "GEMINI": return "gemini-pro";
            case "CHATGPT": return "gpt-4";
            case "CLAUDE": return "claude-3-opus";
            case "OPENROUTER": return "openrouter/auto";
            default: return "auto";
        }
    }

    private static void saveConfig() {
        try (PrintWriter writer = new PrintWriter(CONFIG_FILE)) {
            writer.println("provider=" + currentConfig.provider);
            writer.println("model=" + currentConfig.model);
            writer.println("key=" + maskKey(currentConfig.apiKey));
            // Don't actually save the key - it's masked for security
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        if (!CONFIG_FILE.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            String provider = null, model = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("provider=")) {
                    provider = line.substring(9);
                } else if (line.startsWith("model=")) {
                    model = line.substring(6);
                }
            }
            if (provider != null && model != null) {
                currentConfig = new APIConfig(provider, "", model);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String maskKey(String key) {
        if (key == null || key.length() < 8) return "***";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    public static List<String> getSupportedModels(String provider) {
        switch (provider.toUpperCase()) {
            case "GEMINI":
                return Arrays.asList("gemini-pro", "gemini-1.5-pro", "gemini-1.5-flash");
            case "CHATGPT":
                return Arrays.asList("gpt-4", "gpt-4-turbo", "gpt-3.5-turbo");
            case "CLAUDE":
                return Arrays.asList("claude-3-opus", "claude-3-sonnet", "claude-3-haiku");
            case "OPENROUTER":
                return Arrays.asList("openrouter/auto", "openrouter/anthropic/claude-opus", "openrouter/openai/gpt-4");
            default:
                return Arrays.asList("auto");
        }
    }
}
