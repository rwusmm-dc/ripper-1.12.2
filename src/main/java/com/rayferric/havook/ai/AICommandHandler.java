package com.rayferric.havook.ai;

import com.rayferric.havook.ai.llm.APIKeyManager;
import com.rayferric.havook.util.ChatUtil;

/**
 * Handles CLI commands for AI control
 * Usage:
 *   .ai on - Enable AI
 *   .ai off - Disable AI  
 *   .ai api=provider:key:model - Set API key and model
 *   .ai model=model_name - Change model
 *   .ai personality=friendly|neutral|cautious|aggressive|silent|sarcastic - Set personality
 *   .ai status - Show current status
 *   .ai logs - Show AI decision logs
 */
public class AICommandHandler {

    public static boolean handleCommand(String[] args) {
        if (args.length == 0) {
            return false;
        }

        String raw = args[0];
        String command = raw.toLowerCase();

        // Handle key=value pattern in args[0] (e.g. "api=gemini:key:model")
        if (command.contains("=")) {
            String[] parts = command.split("=", 2);
            command = parts[0];
            args[0] = raw; // keep original for value extraction
        }

        switch (command) {
            case "on":
                AIManager.enable();
                return true;

            case "off":
                AIManager.disable();
                return true;

            case "api":
                String apiVal = raw.startsWith("api=") ? raw.substring(4) : "";
                if (apiVal.isEmpty()) {
                    ChatUtil.syntax("Usage: .ai api=provider:key:model");
                    return true;
                }
                if (APIKeyManager.setAPIKey(apiVal)) {
                    ChatUtil.info("AI API configured: " + APIKeyManager.getConfig());
                } else {
                    ChatUtil.error("Failed to set API key. Format: provider:key:model");
                }
                return true;

            case "model":
                String modelVal = raw.startsWith("model=") ? raw.substring(6) : "";
                if (modelVal.isEmpty()) {
                    ChatUtil.syntax("Usage: .ai model=model_name");
                    return true;
                }
                if (APIKeyManager.setModel(modelVal)) {
                    ChatUtil.info("AI Model changed to: " + modelVal);
                } else {
                    ChatUtil.error("Failed to change model");
                }
                return true;

            case "personality":
                String persVal = raw.startsWith("personality=") ? raw.substring(12) : "";
                if (persVal.isEmpty()) {
                    ChatUtil.syntax("Usage: .ai personality=friendly|neutral|cautious|aggressive|silent|sarcastic");
                    return true;
                }
                try {
                    SystemPromptBuilder.AIPersonality personality = 
                        SystemPromptBuilder.AIPersonality.valueOf(persVal.toUpperCase());
                    SystemPromptBuilder.setPersonality(personality);
                    ChatUtil.info("AI Personality set to: " + personality);
                } catch (IllegalArgumentException e) {
                    ChatUtil.error("Unknown personality: " + persVal);
                }
                return true;

            case "status":
                printStatus();
                return true;

            case "logs":
                showLogs();
                return true;

            case "memory":
                if (args.length > 1 && args[1].equalsIgnoreCase("clear")) {
                    ChatUtil.info("AI Player memory cleared");
                } else {
                    ChatUtil.info(AIManager.getMemory().getSummary());
                }
                return true;

            default:
                return false;
        }
    }

    private static void printStatus() {
        ChatUtil.info("=== AI STATUS ===");
        ChatUtil.info("Enabled: " + AIManager.isEnabled());
        
        APIKeyManager.APIConfig config = APIKeyManager.getConfig();
        if (config != null) {
            ChatUtil.info("Provider: " + config.provider);
            ChatUtil.info("Model: " + config.model);
            ChatUtil.info("API Key: " + (config.apiKey.isEmpty() ? "NOT SET" : "SET"));
        } else {
            ChatUtil.warning("API: NOT CONFIGURED - use .ai api=provider:key:model");
        }
    }

    private static void showLogs() {
        java.io.File logFile = AIMonologueLogger.getLatestLog();
        if (logFile == null) {
            ChatUtil.info("AI No logs found");
            return;
        }

        ChatUtil.info("AI Latest logs from: " + logFile.getName());
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(logFile))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 10) {
                ChatUtil.info(line);
                count++;
            }
            if (count >= 10) {
                ChatUtil.info("... (and more)");
            }
        } catch (java.io.IOException e) {
            ChatUtil.error("AI Error reading logs: " + e.getMessage());
        }
    }
}
