package com.rayferric.havook.feature.mod.combat;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeDouble;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class FurtherReachMod extends Mod {
    public transient ModAttributeDouble reach = new ModAttributeDouble("Reach", 6.0);

    private double previousReach;

    public FurtherReachMod() {
        super("furtherreach", "Further Reach", "Extends your attack distance.", ModCategoryEnum.COMBAT);
        addAttrib(reach);
    }

    @Override
    public void onEnable() {
        if (Minecraft.getMinecraft().player != null) {
            previousReach = Minecraft.getMinecraft().playerController.getBlockReachDistance();
        }
    }

    @Override
    public void onDisable() {
        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().playerController.blockReachDistance = previousReach;
        }
    }

    @Override
    public void onLocalPlayerUpdate() {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().playerController != null) {
            Minecraft.getMinecraft().playerController.blockReachDistance = (float) reach.value;
        }
    }
}