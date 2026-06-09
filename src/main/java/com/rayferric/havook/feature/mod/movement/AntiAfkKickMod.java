package com.rayferric.havook.feature.mod.movement;

import java.util.Random;

import org.lwjgl.input.Keyboard;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import com.rayferric.havook.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AntiAfkKickMod extends Mod {
    private static final long DIRECTION_DURATION_MS = 3000L;
    private static final int LIQUID_SCAN_RADIUS = 4;
    private static final int FALL_SCAN_DISTANCE = 4;
    private static final int FALL_SCAN_DEPTH = 6;

    public transient ModAttributeBoolean avoidLiquids = new ModAttributeBoolean("AvoidLiquids", false);
    public transient ModAttributeBoolean preventFalling = new ModAttributeBoolean("PreventFalling", false);
    public transient ModAttributeBoolean autoRespawn = new ModAttributeBoolean("AutoRespawn", false);

    private transient final Random random = new Random();
    private transient long phaseStartTime;
    private transient boolean walkingForward;
    private transient boolean respawnRequested;

    public AntiAfkKickMod() {
        super("antiafk", "Anti-AFK Kick", "Keeps you active with timed movement, jumps, and swings.",
                ModCategoryEnum.MOVEMENT);
        addAttrib(avoidLiquids);
        addAttrib(preventFalling);
        addAttrib(autoRespawn);
    }

    @Override
    public void onEnable() {
        phaseStartTime = System.currentTimeMillis();
        walkingForward = true;
        respawnRequested = false;
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(),
                Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode()));
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(),
                Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode()));
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(),
                Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode()));
    }

    @Override
    public void onLocalPlayerUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || Minecraft.getMinecraft().world == null)
            return;

        if (player.isDead) {
            if (autoRespawn.value && !respawnRequested) {
                Minecraft.getMinecraft().getConnection()
                        .sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
                respawnRequested = true;
            }
            return;
        }
        respawnRequested = false;

        long now = System.currentTimeMillis();
        if (now - phaseStartTime >= DIRECTION_DURATION_MS) {
            phaseStartTime = now;
            walkingForward = !walkingForward;
        }

        boolean liquidNearby = avoidLiquids.value && hasNearbyLiquid(player);
        boolean cliffAhead = preventFalling.value && isCliffAhead(player, walkingForward);
        boolean hazardDetected = liquidNearby || cliffAhead;

        if (liquidNearby && !cliffAhead) {
            walkingForward = !walkingForward;
            phaseStartTime = now;
        }

        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(),
                walkingForward && !hazardDetected);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(),
                !walkingForward && !hazardDetected);

        if (hazardDetected) {
            stopMovement(player);
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(),
                    Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode()));
            if (Minecraft.getMinecraft().currentScreen != null || !Minecraft.getMinecraft().inGameHasFocus)
                applyDirectMovement(player, walkingForward);
        }

        if (player.onGround && random.nextFloat() < 0.08f)
            player.jump();

        if (random.nextFloat() < 0.12f)
            player.swingArm(EnumHand.MAIN_HAND);
    }

    private boolean hasNearbyLiquid(EntityPlayerSP player) {
        int centerX = MathHelper.floor(player.posX);
        int centerY = MathHelper.floor(player.posY);
        int centerZ = MathHelper.floor(player.posZ);

        for (int x = -LIQUID_SCAN_RADIUS; x <= LIQUID_SCAN_RADIUS; x++) {
            for (int y = -LIQUID_SCAN_RADIUS; y <= 1; y++) {
                for (int z = -LIQUID_SCAN_RADIUS; z <= LIQUID_SCAN_RADIUS; z++) {
                    Block block = Minecraft.getMinecraft().world
                            .getBlockState(new BlockPos(centerX + x, centerY + y, centerZ + z)).getBlock();
                    if (block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.LAVA
                            || block == Blocks.FLOWING_LAVA)
                        return true;
                }
            }
        }

        return false;
    }

    private boolean isCliffAhead(EntityPlayerSP player, boolean forward) {
        double direction = forward ? 1.0d : -1.0d;
        double yawRadians = Math.toRadians(player.rotationYaw);
        double stepX = -MathHelper.sin((float) yawRadians) * direction;
        double stepZ = MathHelper.cos((float) yawRadians) * direction;
        int playerY = MathHelper.floor(player.posY) - 1;

        for (int distance = 0; distance <= FALL_SCAN_DISTANCE; distance++) {
            int x = MathHelper.floor(player.posX + stepX * distance);
            int z = MathHelper.floor(player.posZ + stepZ * distance);
            for (int depth = 0; depth <= FALL_SCAN_DEPTH; depth++) {
                Block block = Minecraft.getMinecraft().world.getBlockState(new BlockPos(x, playerY - depth, z))
                        .getBlock();
                if (BlockUtil.isCollidable(block))
                    return false;
            }
        }

        return true;
    }

    private static void stopMovement(EntityPlayerSP player) {
        player.motionX = 0;
        player.motionZ = 0;
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
    }

    private static void applyDirectMovement(EntityPlayerSP player, boolean forward) {
        double speed = 0.11;
        float yaw = player.rotationYaw;
        float direction = forward ? 1.0f : -1.0f;
        player.motionX = -MathHelper.sin((float) Math.toRadians(yaw)) * speed * direction;
        player.motionZ = MathHelper.cos((float) Math.toRadians(yaw)) * speed * direction;
    }
}