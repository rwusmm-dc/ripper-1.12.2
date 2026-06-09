package com.rayferric.havook.ai;

import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs AI monologue - internal reasoning that's never sent to chat
 * Used for debugging and understanding AI decision-making
 */
public class AIMonologueLogger {
    private static final File LOG_DIR = new File("havook_ai_logs");
    private static final boolean ENABLED = true;

    static {
        if (ENABLED && !LOG_DIR.exists()) {
            LOG_DIR.mkdirs();
        }
    }

    public static void logDecision(GameStateCollector.GameState state, AIAction action, 
                                   String reasoning, String chatResponse, long responseTime) {
        if (!ENABLED) return;

        try {
            JsonObject entry = new JsonObject();
            entry.addProperty("timestamp", new SimpleDateFormat("HH:mm:ss").format(new Date()));
            entry.addProperty("reasoning", reasoning);
            
            // Game state summary
            JsonObject stateJson = new JsonObject();
            stateJson.addProperty("health", state.health);
            stateJson.addProperty("food", state.food);
            stateJson.addProperty("position", String.format("[%d,%d,%d]", state.x, state.y, state.z));
            stateJson.addProperty("nearby_players", state.nearbyPlayers.size());
            entry.add("state", stateJson);

            // Action decision
            entry.addProperty("action", action.type.toString());
            if (action.target != null) {
                entry.addProperty("target", action.target);
            }

            // Response
            entry.addProperty("chat", chatResponse);
            entry.addProperty("response_time_ms", responseTime);

            // Write to file
            File todayLog = new File(LOG_DIR, "ai_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".jsonl");
            try (FileWriter fw = new FileWriter(todayLog, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(entry.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logError(String context, Exception e) {
        if (!ENABLED) return;

        try {
            JsonObject entry = new JsonObject();
            entry.addProperty("timestamp", new SimpleDateFormat("HH:mm:ss").format(new Date()));
            entry.addProperty("type", "ERROR");
            entry.addProperty("context", context);
            entry.addProperty("error", e.getMessage());

            File todayLog = new File(LOG_DIR, "ai_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".jsonl");
            try (FileWriter fw = new FileWriter(todayLog, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(entry.toString());
                bw.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static File getLatestLog() {
        if (!LOG_DIR.exists()) return null;
        File[] files = LOG_DIR.listFiles();
        if (files == null || files.length == 0) return null;
        
        // Get most recent file
        File latest = files[0];
        for (File f : files) {
            if (f.lastModified() > latest.lastModified()) {
                latest = f;
            }
        }
        return latest;
    }
}
