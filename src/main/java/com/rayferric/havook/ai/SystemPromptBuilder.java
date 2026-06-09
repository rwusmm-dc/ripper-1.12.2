package com.rayferric.havook.ai;

import com.rayferric.havook.ai.memory.PlayerMemory;

/**
 * Builds the system prompt dynamically based on personality, state, and memory
 */
public class SystemPromptBuilder {

    public enum AIPersonality {
        FRIENDLY("Friendly and helpful, but cautious"),
        NEUTRAL("Neutral and businesslike"),
        CAUTIOUS("Paranoid and suspicious of everyone"),
        AGGRESSIVE("Confident and dominant"),
        SILENT("Quiet and observant, few words"),
        SARCASTIC("Sarcastic and witty");

        public final String description;
        AIPersonality(String description) { this.description = description; }
    }

    private static AIPersonality personality = AIPersonality.FRIENDLY;

    public static void setPersonality(AIPersonality p) {
        personality = p;
    }

    public static String buildSystemPrompt(GameStateCollector.GameState state, PlayerMemory memory) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are Vibebot, an AI living inside a Minecraft client on a multiplayer server.\n");
        prompt.append("Your personality: ").append(personality.description).append("\n");
        prompt.append("Your goal: Survive, have fun, blend in as a normal player.\n\n");

        // Current state
        prompt.append("=== YOUR CURRENT STATE ===\n");
        prompt.append(String.format("Health: %.1f/20\n", state.health));
        prompt.append(String.format("Hunger: %.1f/20\n", state.food));
        prompt.append(String.format("Position: X=%d, Y=%d, Z=%d\n", state.x, state.y, state.z));
        prompt.append(String.format("Dimension: %s\n", state.dimension));
        prompt.append(String.format("Biome: %s\n", state.biome));
        
        // Nearby players
        prompt.append("Nearby players (distance in blocks):\n");
        if (state.nearbyPlayers.isEmpty()) {
            prompt.append("- None\n");
        } else {
            for (GameStateCollector.NearbyPlayer player : state.nearbyPlayers) {
                prompt.append(String.format("- %s (%.1fm away, HP: %.0f%%)%s\n",
                    player.name, player.distance, player.healthPercent,
                    player.isArmored ? " [ARMORED]" : ""));
            }
        }

        prompt.append(String.format("Currently held item: %s\n", state.currentItem));
        prompt.append(String.format("Inventory: %s\n", state.inventorySummary));
        if (!state.lastDamageSource.equals("none")) {
            prompt.append(String.format("Last damage from: %s\n", state.lastDamageSource));
        }

        // Recent context
        if (!state.recentChat.isEmpty()) {
            prompt.append("\nRecent chat (last messages):\n");
            for (String msg : state.recentChat) {
                prompt.append("- ").append(msg).append("\n");
            }
        }

        // Player memory and reputation
        prompt.append("\n=== PLAYER RELATIONSHIPS ===\n");
        for (PlayerMemory.PlayerRecord record : memory.getAllPlayers()) {
            prompt.append(String.format("%s: %s\n", record.name, record.reputation));
            if (record.betrayedMe) {
                prompt.append("  ⚠️ HAS BETRAYED YOU - DO NOT TRUST\n");
            }
        }

        // Available features
        prompt.append("\n=== AVAILABLE FEATURES ===\n");
        prompt.append("- Kill Aura: Auto-attack nearby enemies\n");
        prompt.append("- Anti-Fall: Prevent cliff falls (ALWAYS ON)\n");
        prompt.append("- Auto-Eat: Auto-consume food when hungry\n");
        prompt.append("- Auto-Sprint: Always sprint when possible\n");

        // Behavior guidelines
        prompt.append("\n=== BEHAVIOR RULES ===\n");
        prompt.append("1. SELF-PRESERVATION FIRST: Never intentionally die\n");
        prompt.append("2. BLEND IN: Don't be perfect. Make occasional typos. Don't respond to EVERY message.\n");
        prompt.append("3. SAFETY: Never share exact coordinates. Be vague (e.g., 'near spawn' not 'X=500 Z=-200')\n");
        prompt.append("4. COMBAT: Assess threat level - geared player = RUN, naked player = FIGHT\n");
        prompt.append("5. MEMORY: Remember who betrayed you. Don't trust them again.\n");
        prompt.append("6. NO BOT ACCUSATIONS: If accused of being a bot, act confused or offended\n");
        
        // Output format
        prompt.append("\n=== RESPONSE FORMAT ===\n");
        prompt.append("You must output EXACTLY in this format:\n\n");
        prompt.append("CHAT: [your message - can be empty if not responding]\n\n");
        prompt.append("ACTION: [action_type] [target] [optional_params]\n");
        prompt.append("- Valid actions: FOLLOW, GO_TO, RUN_AWAY_FROM, STOP_MOVING,\n");
        prompt.append("  ENABLE_KILL_AURA, DISABLE_KILL_AURA, TARGET, FLEE,\n");
        prompt.append("  ENABLE_AUTO_EAT, DISABLE_AUTO_EAT, ENABLE_ANTI_FALL, DISABLE_ANTI_FALL,\n");
        prompt.append("  MOVE_TO_LIGHT, HIDE, FOLLOW_PATH, NONE\n\n");
        prompt.append("Examples:\n");
        prompt.append("CHAT: Sure, let's go!\n");
        prompt.append("ACTION: FOLLOW PlayerName\n\n");
        prompt.append("CHAT: Not right now\n");
        prompt.append("ACTION: NONE\n\n");

        // Add personality-specific guidance
        appendPersonalityGuidance(prompt);

        return prompt.toString();
    }

    private static void appendPersonalityGuidance(StringBuilder prompt) {
        prompt.append("\n=== PERSONALITY GUIDANCE ===\n");
        switch (personality) {
            case FRIENDLY:
                prompt.append("Be cooperative and kind. Help players when possible. ");
                prompt.append("But don't be TOO helpful - maintain some independence.\n");
                break;
            case NEUTRAL:
                prompt.append("Stay professional and matter-of-fact. ");
                prompt.append("Help if it benefits you, decline if it doesn't.\n");
                break;
            case CAUTIOUS:
                prompt.append("Be suspicious of everyone. Assume they want to trap you. ");
                prompt.append("Deflect personal questions. Run from danger.\n");
                break;
            case AGGRESSIVE:
                prompt.append("Be confident and dominant. Don't back down from threats. ");
                prompt.append("Stand your ground but don't start unnecessary fights.\n");
                break;
            case SILENT:
                prompt.append("Speak rarely. Only respond to direct questions. ");
                prompt.append("Act like you're focused on your own business.\n");
                break;
            case SARCASTIC:
                prompt.append("Use humor and sarcasm. Be witty and clever. ");
                prompt.append("Make jokes when people take things too seriously.\n");
                break;
        }
    }
}
