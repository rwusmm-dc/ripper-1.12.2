package com.rayferric.havook.ai;

/**
 * Parses AI response in format:
 * CHAT: [message]
 * ACTION: [type] [target] [params]
 */
public class ResponseParser {

    public static AIResponse parse(String aiOutput, long responseTime) {
        if (aiOutput == null || aiOutput.isEmpty()) {
            return new AIResponse("", new AIAction(AIAction.ActionType.NONE), responseTime);
        }

        String chatMessage = extractSection(aiOutput, "CHAT:");
        String actionLine = extractSection(aiOutput, "ACTION:");

        AIAction action = parseAction(actionLine);

        return new AIResponse(chatMessage, action, responseTime);
    }

    private static String extractSection(String text, String prefix) {
        int startIdx = text.indexOf(prefix);
        if (startIdx == -1) return "";

        startIdx += prefix.length();
        int endIdx = text.indexOf("\n", startIdx);
        if (endIdx == -1) {
            endIdx = text.length();
        }

        return text.substring(startIdx, endIdx).trim();
    }

    private static AIAction parseAction(String actionLine) {
        if (actionLine == null || actionLine.isEmpty() || actionLine.equalsIgnoreCase("NONE")) {
            return new AIAction(AIAction.ActionType.NONE);
        }

        String[] parts = actionLine.split("\\s+");
        if (parts.length == 0) {
            return new AIAction(AIAction.ActionType.NONE);
        }

        try {
            AIAction.ActionType type = AIAction.ActionType.valueOf(parts[0].toUpperCase());
            String target = parts.length > 1 ? parts[1] : null;
            String[] params = new String[parts.length - 2];
            System.arraycopy(parts, 2, params, 0, params.length);

            return new AIAction(type, target, params);
        } catch (IllegalArgumentException e) {
            // Invalid action type
            return new AIAction(AIAction.ActionType.NONE);
        }
    }

    /**
     * Validates if action is semantically valid for current context
     */
    public static boolean isValidAction(AIAction action, GameStateCollector.GameState state) {
        switch (action.type) {
            case FOLLOW:
            case TARGET:
            case RUN_AWAY_FROM:
                // Must have a target player
                if (action.target == null) return false;
                // Player must be nearby
                return state.nearbyPlayers.stream()
                    .anyMatch(p -> p.name.equalsIgnoreCase(action.target));

            case GO_TO:
                // Must have coordinates
                return action.parameters.length >= 3;

            case FOLLOW_PATH:
            case MOVE_TO_LIGHT:
            case HIDE:
            case FLEE:
            case STOP_MOVING:
            case ENABLE_KILL_AURA:
            case DISABLE_KILL_AURA:
            case ENABLE_AUTO_EAT:
            case DISABLE_AUTO_EAT:
            case ENABLE_ANTI_FALL:
            case DISABLE_ANTI_FALL:
                return true;

            case NONE:
                return true;

            default:
                return false;
        }
    }
}
