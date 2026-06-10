package com.rayferric.havook.feature.mod.misc;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModAttributeDouble;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class DerpMod extends Mod {
    public transient ModAttributeBoolean spinHead = new ModAttributeBoolean("SpinHead", true);
    public transient ModAttributeDouble spinSpeed = new ModAttributeDouble("SpinSpeed", 360.0);

    private float headRotation = 0;

    public DerpMod() {
        super("derp", "Derp", "Spins head in 360 continuously fast (makes you look derpy).", ModCategoryEnum.MISC);
        addAttrib(spinHead);
        addAttrib(spinSpeed);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.START) return;
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().player == null) return;

        if (spinHead.value) {
            headRotation += spinSpeed.value / 20.0f; // per tick
            if (headRotation >= 360) headRotation -= 360;
            
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            player.rotationYawHead = headRotation;
            player.renderYawOffset = headRotation;
            player.rotationYaw = headRotation;
        }
    }
}