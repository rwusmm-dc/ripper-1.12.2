package com.rayferric.havook.feature.mod.render;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class NoHurtCamMod extends Mod {
    public NoHurtCamMod() {
        super("nohurtcam", "No Hurt Camera", "Removes screen shake when hit.", ModCategoryEnum.RENDER);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.START) return;
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().player == null) return;
        
        if (Minecraft.getMinecraft().player.hurtTime > 0) {
            Minecraft.getMinecraft().player.hurtTime = 0;
        }
    }
}