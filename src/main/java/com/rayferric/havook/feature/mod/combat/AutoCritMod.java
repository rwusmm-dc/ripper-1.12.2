package com.rayferric.havook.feature.mod.combat;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class AutoCritMod extends Mod {
    public transient ModAttributeBoolean onlyWhenFalling = new ModAttributeBoolean("OnlyWhenFalling", false);
    public transient ModAttributeBoolean autoJump = new ModAttributeBoolean("AutoJump", true);

    public AutoCritMod() {
        super("autocrit", "Auto Crit", "Automatically jumps before attacking for critical hits.", ModCategoryEnum.COMBAT);
        addAttrib(onlyWhenFalling);
        addAttrib(autoJump);
    }

    @Override
    public void onLocalPlayerUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || Minecraft.getMinecraft().world == null)
            return;

        if (player.isSwingInProgress && player.swingProgress > 0.1f && player.swingProgress < 0.5f) {
            if (autoJump.value && player.onGround && !player.isSneaking() && !player.isInWater() && !player.isInLava()) {
                if (!onlyWhenFalling.value || player.fallDistance > 0) {
                    player.jump();
                }
            }
        }
    }
}