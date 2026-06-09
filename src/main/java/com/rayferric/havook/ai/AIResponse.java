package com.rayferric.havook.ai;

/**
 * Represents the AI's decision: what to say and what to do
 */
public class AIResponse {
    public final String chatMessage;
    public final AIAction action;
    public final long responseTime; // ms taken to generate

    public AIResponse(String chatMessage, AIAction action, long responseTime) {
        this.chatMessage = chatMessage != null ? chatMessage.trim() : "";
        this.action = action != null ? action : new AIAction(AIAction.ActionType.NONE);
        this.responseTime = responseTime;
    }

    public boolean shouldSendChat() {
        return !chatMessage.isEmpty() && !chatMessage.equals("NONE");
    }

    public boolean shouldExecuteAction() {
        return action.type != AIAction.ActionType.NONE;
    }

    @Override
    public String toString() {
        return String.format("AIResponse{chat='%s', action=%s, time=%dms}", 
            chatMessage, action, responseTime);
    }
}
