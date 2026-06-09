package com.rayferric.havook.ai;

/**
 * Represents an action that the AI decides to take
 */
public class AIAction {
    public enum ActionType {
        // Movement
        FOLLOW, GO_TO, RUN_AWAY_FROM, STOP_MOVING,
        // Combat
        ENABLE_KILL_AURA, DISABLE_KILL_AURA, TARGET, FLEE,
        // Features
        ENABLE_AUTO_EAT, DISABLE_AUTO_EAT, ENABLE_ANTI_FALL, DISABLE_ANTI_FALL,
        // Utility
        MOVE_TO_LIGHT, HIDE, FOLLOW_PATH,
        // Special
        NONE
    }

    public final ActionType type;
    public final String target;
    public final String[] parameters;

    public AIAction(ActionType type) {
        this(type, null, new String[0]);
    }

    public AIAction(ActionType type, String target) {
        this(type, target, new String[0]);
    }

    public AIAction(ActionType type, String target, String[] parameters) {
        this.type = type;
        this.target = target;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "AIAction{" + type + (target != null ? " " + target : "") + "}";
    }
}
