package com.rayferric.havook.ai;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.*;

/**
 * Collects current game state to build context for AI
 */
public class GameStateCollector {
    private static final float NEARBY_PLAYER_DISTANCE = 50f;
    
    public static class GameState {
        public float health;
        public float food;
        public int x, y, z;
        public String dimension;
        public String biome;
        public List<NearbyPlayer> nearbyPlayers;
        public String currentItem;
        public String inventorySummary;
        public String lastDamageSource;
        public List<String> recentChat; // Last 30 messages
        public String[] recentActions; // Last 5 actions
        public long timestamp;

        @Override
        public String toString() {
            return String.format(
                "GameState{health=%.1f, food=%.1f, pos=[%d,%d,%d], dim=%s, biome=%s, nearby=%d}",
                health, food, x, y, z, dimension, biome, nearbyPlayers.size()
            );
        }
    }

    public static class NearbyPlayer {
        public String name;
        public float distance;
        public float healthPercent;
        public boolean isArmored;

        public NearbyPlayer(String name, float distance, float healthPercent, boolean isArmored) {
            this.name = name;
            this.distance = distance;
            this.healthPercent = healthPercent;
            this.isArmored = isArmored;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1fm, HP:%.0f%%)", name, distance, healthPercent);
        }
    }

    private static Minecraft mc = Minecraft.getMinecraft();
    private static final Deque<String> chatHistory = new LinkedList<>();
    private static final Deque<String> actionHistory = new LinkedList<>();

    public static GameState collectState() {
        GameState state = new GameState();
        state.timestamp = System.currentTimeMillis();

        EntityPlayer player = mc.player;
        if (player == null) return state;

        // Basic stats
        state.health = player.getHealth();
        state.food = player.getFoodStats().getFoodLevel();
        state.x = (int) player.posX;
        state.y = (int) player.posY;
        state.z = (int) player.posZ;

        // World info
        World world = player.world;
        state.dimension = getDimensionName(world.provider.getDimension());
        state.biome = world.getBiome(player.getPosition()).getBiomeName();

        // Nearby players
        state.nearbyPlayers = getNearbyPlayers(player);

        // Inventory
        state.currentItem = player.getHeldItemMainhand().getDisplayName();
        state.inventorySummary = getInventorySummary(player);

        // Damage tracking
        state.lastDamageSource = player.getLastDamageSource() != null 
            ? player.getLastDamageSource().damageType 
            : "none";

        // Chat and actions
        state.recentChat = new ArrayList<>(chatHistory);
        state.recentActions = actionHistory.toArray(new String[0]);

        return state;
    }

    private static String getDimensionName(int dim) {
        switch (dim) {
            case -1: return "nether";
            case 0: return "overworld";
            case 1: return "end";
            default: return "dimension_" + dim;
        }
    }

    private static List<NearbyPlayer> getNearbyPlayers(EntityPlayer player) {
        List<NearbyPlayer> nearby = new ArrayList<>();
        List<EntityPlayer> allPlayers = mc.world.playerEntities;

        for (EntityPlayer other : allPlayers) {
            if (other == player) continue;
            
            float dist = player.getDistance(other);
            if (dist > NEARBY_PLAYER_DISTANCE) continue;

            float health = other.getHealth() / other.getMaxHealth() * 100;
            boolean armored = other.getTotalArmorValue() > 0;
            nearby.add(new NearbyPlayer(other.getName(), dist, health, armored));
        }

        // Sort by distance
        nearby.sort((a, b) -> Float.compare(a.distance, b.distance));
        return nearby;
    }

    private static String getInventorySummary(EntityPlayer player) {
        int totalStacks = 0;
        int filledSlots = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (!player.inventory.getStackInSlot(i).isEmpty()) {
                filledSlots++;
            }
        }
        totalStacks = player.inventory.getSizeInventory();
        return String.format("%d/%d slots", filledSlots, totalStacks);
    }

    public static void logChat(String message) {
        chatHistory.addLast(message);
        if (chatHistory.size() > 30) {
            chatHistory.removeFirst();
        }
    }

    public static void logAction(String actionName) {
        actionHistory.addLast(actionName);
        if (actionHistory.size() > 5) {
            actionHistory.removeFirst();
        }
    }
}
