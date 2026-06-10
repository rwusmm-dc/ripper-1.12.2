package com.rayferric.havook.feature.mod.combat;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class AutoCriticalHitMod extends Mod {
    public transient ModAttributeBoolean autoJump = new ModAttributeBoolean("AutoJump", true);

    public AutoCriticalHitMod() {
        super("autocriticalhit", "Auto Critical Hit", "Automatically jumps before attacking for critical hits on every hit.", ModCategoryEnum.COMBAT);
        addAttrib(autoJump);
    }

    @Override
    public void onLocalPlayerUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || Minecraft.getMinecraft().world == null)
            return;

        if (player.isSwingInProgress && player.swingProgress > 0.1f && player.swingProgress < 0.5f) {
            if (autoJump.value && player.onGround && !player.isSneaking() && !player.isInWater() && !player.isInLava()) {
                player.jump();
            }
        }
    }
}