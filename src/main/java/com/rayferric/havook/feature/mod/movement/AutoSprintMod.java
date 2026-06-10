package com.rayferric.havook.feature.mod.movement;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;

public class AutoSprintMod extends Mod {
    public AutoSprintMod() {
        super("autosprint", "Auto Sprint", "Makes you sprint automatically.", ModCategoryEnum.MOVEMENT);
    }

    @Override
    public void onLocalPlayerUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return;

        if (player.collidedHorizontally || player.isSneaking() || player.isInWater() || player.isInLava())
            return;

        if (player.getFoodStats().getFoodLevel() <= 6)
            return;

        if (player.moveForward > 0 && !player.isSprinting()) {
            player.setSprinting(true);
            Minecraft.getMinecraft().getConnection().sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }

    @Override
    public void onDisable() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && player.isSprinting()) {
            player.setSprinting(false);
            if (Minecraft.getMinecraft().getConnection() != null) {
                Minecraft.getMinecraft().getConnection().sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
    }
}