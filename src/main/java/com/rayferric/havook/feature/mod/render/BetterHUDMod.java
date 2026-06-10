package com.rayferric.havook.feature.mod.render;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BetterHUDMod extends Mod {
    public transient ModAttributeBoolean showCoords = new ModAttributeBoolean("ShowCoords", true);

    public BetterHUDMod() {
        super("betterhud", "Better HUD", "Shows coordinates on screen.", ModCategoryEnum.RENDER);
        addAttrib(showCoords);
    }

    @Override
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!isEnabled()) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;
        
        FontRenderer fontRenderer = mc.fontRenderer;
        int y = 2;
        int color = 0xFFFFFFFF;
        
        if (showCoords.value) {
            BlockPos pos = mc.player.getPosition();
            String coords = String.format("XYZ: %d %d %d", pos.getX(), pos.getY(), pos.getZ());
            fontRenderer.drawStringWithShadow("\247bCoords: \247f" + coords, 2, y, color);
            y += 10;
        }
    }
}