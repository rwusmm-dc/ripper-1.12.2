package com.rayferric.havook.ai.reflex;

import com.rayferric.havook.ai.AIAction;
import com.rayferric.havook.ai.GameStateCollector;

/**
 * Reflex layer: Immediate, sub-100ms reactions that don't require LLM
 * These bypass the AI and trigger instantly based on game state
 */
public class ReflexLayer {

    /**
     * Check if we need immediate reflex action
     * @return null if no reflex needed, otherwise the action to take immediately
     */
    public static AIAction evaluateReflex(GameStateCollector.GameState state) {
        // PRIORITY 1: Critical health - FLEE immediately
        if (state.health < 4) {
            return new AIAction(AIAction.ActionType.FLEE);
        }

        // PRIORITY 2: Falling with anti-fall enabled
        // (This is typically handled by the Anti-Fall feature automatically)

        // PRIORITY 3: Attacked by hostile player - fight back
        if (state.lastDamageSource != null && !state.lastDamageSource.equals("none")) {
            // If we were just attacked, enable kill aura against nearby players
            if (!state.nearbyPlayers.isEmpty()) {
                GameStateCollector.NearbyPlayer closest = state.nearbyPlayers.get(0);
                return new AIAction(AIAction.ActionType.ENABLE_KILL_AURA, closest.name);
            }
        }

        // PRIORITY 4: Hungry while safe
        if (state.food < 4) {
            return new AIAction(AIAction.ActionType.ENABLE_AUTO_EAT);
        }

        // PRIORITY 5: Multiple hostile players nearby - run
        long hostileCount = state.nearbyPlayers.stream()
            .filter(p -> p.distance < 15 && p.isArmored)
            .count();
        if (hostileCount >= 2) {
            return new AIAction(AIAction.ActionType.FLEE);
        }

        // No reflex needed
        return null;
    }

    /**
     * Get reflex action description for logging
     */
    public static String getReflexReason(AIAction reflex, GameStateCollector.GameState state) {
        switch (reflex.type) {
            case FLEE:
                if (state.health < 4) return "Critical health";
                if (state.nearbyPlayers.size() >= 2) return "Multiple threats";
                return "Fleeing";
            case ENABLE_KILL_AURA:
                return "Attacked - defending";
            case ENABLE_AUTO_EAT:
                return "Critical hunger";
            default:
                return "Reflex action";
        }
    }

    /**
     * Emergency stops - override any other action
     */
    public static boolean shouldAbortAIAction(GameStateCollector.GameState state) {
        // Abort if we're critically low health and need to flee
        if (state.health < 3) return true;
        
        // Abort if we're falling and anti-fall is off
        // (would be handled by feature, but good to double-check)
        
        return false;
    }
}
