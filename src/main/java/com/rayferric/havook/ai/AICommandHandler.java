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

        String command = args[0].toLowerCase();

        switch (command) {
            case "on":
                AIManager.enable();
                return true;

            case "off":
                AIManager.disable();
                return true;

            case "api":
                if (args.length < 2) {
                    ChatUtil.syntax("Usage: .ai api=provider:key:model");
                    return true;
                }
                String apiArg = args[1];
                if (apiArg.startsWith("api=")) {
                    apiArg = apiArg.substring(4);
                }
                if (APIKeyManager.setAPIKey(apiArg)) {
                    ChatUtil.info("AI API configured: " + APIKeyManager.getConfig());
                } else {
                    ChatUtil.error("Failed to set API key. Format: provider:key:model");
                }
                return true;

            case "model":
                if (args.length < 2) {
                    ChatUtil.syntax("Usage: .ai model=model_name");
                    return true;
                }
                String modelArg = args[1];
                if (modelArg.startsWith("model=")) {
                    modelArg = modelArg.substring(6);
                }
                if (APIKeyManager.setModel(modelArg)) {
                    ChatUtil.info("AI Model changed to: " + modelArg);
                } else {
                    ChatUtil.error("Failed to change model");
                }
                return true;

            case "personality":
                if (args.length < 2) {
                    ChatUtil.syntax("Usage: .ai personality=friendly|neutral|cautious|aggressive|silent|sarcastic");
                    return true;
                }
                String personalityArg = args[1];
                if (personalityArg.startsWith("personality=")) {
                    personalityArg = personalityArg.substring(12);
                }
                try {
                    SystemPromptBuilder.AIPersonality personality = 
                        SystemPromptBuilder.AIPersonality.valueOf(personalityArg.toUpperCase());
                    SystemPromptBuilder.setPersonality(personality);
                    ChatUtil.info("AI Personality set to: " + personality);
                } catch (IllegalArgumentException e) {
                    ChatUtil.error("Unknown personality: " + personalityArg);
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
