package com.rayferric.havook.ai.memory;

import java.util.*;

/**
 * Tracks reputation, interactions, and memory of other players
 */
public class PlayerMemory {
    public enum Reputation {
        HOSTILE(-2), SUSPICIOUS(-1), NEUTRAL(0), FRIENDLY(1), TRUSTED(2);
        
        public final int value;
        Reputation(int value) { this.value = value; }
    }

    public static class PlayerRecord {
        public String name;
        public Reputation reputation;
        public long lastInteraction;
        public List<Interaction> interactions;
        public boolean betrayedMe;
        public String lastLocationSeen;

        public PlayerRecord(String name) {
            this.name = name;
            this.reputation = Reputation.NEUTRAL;
            this.lastInteraction = System.currentTimeMillis();
            this.interactions = new ArrayList<>();
            this.betrayedMe = false;
            this.lastLocationSeen = "unknown";
        }

        @Override
        public String toString() {
            return String.format("PlayerRecord{name=%s, rep=%s, betrayed=%b, interactions=%d}",
                name, reputation, betrayedMe, interactions.size());
        }
    }

    public static class Interaction {
        public enum Type {
            CHAT, COMBAT, TRADE, FOLLOW, FLEE, TRAP, HELP, HARM
        }

        public Type type;
        public String content;
        public long timestamp;
        public boolean positive;

        public Interaction(Type type, String content, boolean positive) {
            this.type = type;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
            this.positive = positive;
        }
    }

    private final Map<String, PlayerRecord> playerRecords = new HashMap<>();
    private final Map<String, String> locations = new HashMap<>(); // Safe spots

    public PlayerRecord getOrCreate(String playerName) {
        return playerRecords.computeIfAbsent(playerName, PlayerRecord::new);
    }

    public void recordInteraction(String playerName, Interaction interaction) {
        PlayerRecord record = getOrCreate(playerName);
        record.interactions.add(interaction);
        record.lastInteraction = System.currentTimeMillis();

        // Update reputation based on interaction
        if (interaction.positive) {
            if (record.reputation.value < Reputation.TRUSTED.value) {
                record.reputation = Reputation.values()[record.reputation.value + 1];
            }
        } else {
            if (record.reputation.value > Reputation.HOSTILE.value) {
                record.reputation = Reputation.values()[record.reputation.value - 1];
            }
        }
    }

    public void markBetrayal(String playerName) {
        PlayerRecord record = getOrCreate(playerName);
        record.betrayedMe = true;
        record.reputation = Reputation.HOSTILE;
        recordInteraction(playerName, new Interaction(
            Interaction.Type.TRAP, "Attempted to trap/kill me", false
        ));
    }

    public void recordLocation(String name, String description) {
        locations.put(name, description);
    }

    public String getLocation(String name) {
        return locations.getOrDefault(name, "unknown");
    }

    public Reputation getReputation(String playerName) {
        PlayerRecord record = playerRecords.get(playerName);
        return record != null ? record.reputation : Reputation.NEUTRAL;
    }

    public boolean isTrusted(String playerName) {
        return getReputation(playerName).value >= Reputation.FRIENDLY.value;
    }

    public boolean isHostile(String playerName) {
        return getReputation(playerName).value <= Reputation.SUSPICIOUS.value;
    }

    public Collection<PlayerRecord> getAllPlayers() {
        return playerRecords.values();
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PLAYER MEMORY ===\n");
        for (PlayerRecord record : playerRecords.values()) {
            sb.append(String.format("%s: %s\n", record.name, record.reputation));
        }
        return sb.toString();
    }
}
