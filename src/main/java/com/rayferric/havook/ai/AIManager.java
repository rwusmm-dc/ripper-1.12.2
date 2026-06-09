package com.rayferric.havook.ai;

import com.rayferric.havook.ai.llm.APIKeyManager;
import com.rayferric.havook.ai.llm.LLMConnector;
import com.rayferric.havook.ai.memory.PlayerMemory;
import com.rayferric.havook.ai.reflex.ReflexLayer;
import com.rayferric.havook.manager.ModManager;
import com.rayferric.havook.util.ChatUtil;
import net.minecraft.client.Minecraft;

/**
 * Main AI orchestrator - coordinates all AI systems
 */
public class AIManager {
    public static final int TICK_INTERVAL = 5; // Run AI every N ticks (5 ticks = ~250ms)

    private static boolean enabled = false;
    private static int tickCounter = 0;
    private static long lastAICall = 0;
    private static final int AI_COOLDOWN = 3000; // Min 3 seconds between full AI calls

    private static PlayerMemory memory = new PlayerMemory();
    private static String lastChatMessage = "";

    // Cooldowns for actions
    private static long lastChatTime = 0;
    private static long lastActionTime = 0;
    private static final int CHAT_COOLDOWN = 2000;
    private static final int ACTION_COOLDOWN = 500;

    public static void enable() {
        if (!APIKeyManager.isConfigured()) {
            ChatUtil.error("AI No API configured. Use .ai api=provider:key:model");
            return;
        }
        enabled = true;
        ChatUtil.info("AI Enabled with " + APIKeyManager.getConfig());
    }

    public static void disable() {
        enabled = false;
        ChatUtil.info("AI Disabled");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void tick() {
        if (!enabled) return;

        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        // Collect game state
        GameStateCollector.GameState state = GameStateCollector.collectState();

        // 1. CHECK REFLEX LAYER FIRST (sub-100ms)
        AIAction reflexAction = ReflexLayer.evaluateReflex(state);
        if (reflexAction != null) {
            String reason = ReflexLayer.getReflexReason(reflexAction, state);
            ChatUtil.info("AI Reflex: " + reason);
            executeAction(reflexAction, "REFLEX");
            return; // Don't call LLM if reflex kicked in
        }

        // 2. CHECK IF AI CALL IS NEEDED
        long timeSinceLastCall = System.currentTimeMillis() - lastAICall;
        if (timeSinceLastCall < AI_COOLDOWN) {
            return; // Too soon, wait for cooldown
        }

        // 3. CALL LLM (async would be ideal, but doing sync for now)
        try {
            String systemPrompt = SystemPromptBuilder.buildSystemPrompt(state, memory);
            
            // User message is context about what just happened
            String userMessage = buildUserMessage(state);

            long startTime = System.currentTimeMillis();
            String aiResponse = LLMConnector.queryAI(systemPrompt, userMessage);
            long responseTime = System.currentTimeMillis() - startTime;
            lastAICall = System.currentTimeMillis();

            // 4. PARSE RESPONSE
            AIResponse response = ResponseParser.parse(aiResponse, responseTime);

            // 5. VALIDATE RESPONSE
            if (!ResponseParser.isValidAction(response.action, state)) {
                AIMonologueLogger.logError("Invalid action", 
                    new Exception("AI tried: " + response.action));
                return;
            }

            // 6. EXECUTE RESPONSE
            if (response.shouldSendChat()) {
                sendChat(response.chatMessage);
            }
            if (response.shouldExecuteAction()) {
                executeAction(response.action, "AI");
            }

            // 7. LOG MONOLOGUE
            AIMonologueLogger.logDecision(state, response.action, 
                "AI response", response.chatMessage, responseTime);

        } catch (Exception e) {
            ChatUtil.error("AI Error: " + e.getMessage());
            AIMonologueLogger.logError("LLM Query", e);
        }
    }

    private static String buildUserMessage(GameStateCollector.GameState state) {
        StringBuilder msg = new StringBuilder();
        msg.append("What should I do? ");
        
        if (state.health < 10) {
            msg.append("I'm low on health. ");
        }
        if (state.food < 8) {
            msg.append("I'm hungry. ");
        }
        if (!state.nearbyPlayers.isEmpty()) {
            msg.append("There are ").append(state.nearbyPlayers.size()).append(" nearby players. ");
        }
        if (!state.lastDamageSource.equals("none")) {
            msg.append("I was just attacked!");
        }
        
        return msg.toString();
    }

    public static void onChatReceived(String playerName, String message) {
        if (!enabled) return;

        // Update memory with who said what
        GameStateCollector.logChat(playerName + ": " + message);
        memory.recordInteraction(playerName, new PlayerMemory.Interaction(
            PlayerMemory.Interaction.Type.CHAT, message, true
        ));

        // Don't respond to every message - that's not human-like
        // Let the main tick() decide via LLM if we should respond
    }

    public static void onPlayerDamaged(String attackerName) {
        if (!enabled) return;

        // Update memory: someone attacked us
        memory.recordInteraction(attackerName, new PlayerMemory.Interaction(
            PlayerMemory.Interaction.Type.COMBAT, "attacked me", false
        ));
    }

    public static void onPlayerDeath(String killerName) {
        if (!enabled) return;

        // Significant memory update
        memory.markBetrayal(killerName);
    }

    private static void sendChat(String message) {
        long now = System.currentTimeMillis();
        if (now - lastChatTime < CHAT_COOLDOWN) {
            return;
        }
        lastChatTime = now;

        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.sendChatMessage(message);
        }
        lastChatMessage = message;
        GameStateCollector.logChat("Vibebot: " + message);
    }

    private static void executeAction(AIAction action, String source) {
        long now = System.currentTimeMillis();
        if (now - lastActionTime < ACTION_COOLDOWN) {
            return;
        }
        lastActionTime = now;

        GameStateCollector.logAction(action.type.toString());

        switch (action.type) {
            case ENABLE_KILL_AURA:
                setMod("killaura", true);
                break;
            case DISABLE_KILL_AURA:
                setMod("killaura", false);
                break;
            case ENABLE_AUTO_EAT:
                setMod("autoeat", true);
                break;
            case DISABLE_AUTO_EAT:
                setMod("autoeat", false);
                break;
            case ENABLE_ANTI_FALL:
                setMod("safewalk", true);
                break;
            case DISABLE_ANTI_FALL:
                setMod("safewalk", false);
                break;
            case FLEE:
                setMod("autosprint", true);
                setMod("autowalk", true);
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.player != null) {
                    mc.player.rotationYaw += 180;
                }
                break;
            case STOP_MOVING:
                setMod("autowalk", false);
                setMod("autosprint", false);
                break;
            case FOLLOW:
                setMod("autowalk", true);
                setMod("autosprint", true);
                break;
            case TARGET:
                setMod("killaura", true);
                break;
            case GO_TO:
            case RUN_AWAY_FROM:
            case MOVE_TO_LIGHT:
            case HIDE:
            case FOLLOW_PATH:
            case NONE:
                break;
        }
    }

    private static void setMod(String modId, boolean enabled) {
        com.rayferric.havook.feature.Mod mod = ModManager.getModById(modId);
        if (mod != null && mod.isEnabled() != enabled) {
            mod.setEnabled(enabled);
        }
    }

    public static PlayerMemory getMemory() {
        return memory;
    }

    public static String getLastChatMessage() {
        return lastChatMessage;
    }
}
