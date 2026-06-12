package com.rayferric.havook.feature.mod.render;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModAttributeDouble;
import com.rayferric.havook.feature.mod.ModAttributeInteger;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomHandMod extends Mod {
    public transient ModAttributeDouble posX = new ModAttributeDouble("PosX", 0.0);
    public transient ModAttributeDouble posY = new ModAttributeDouble("PosY", 0.0);
    public transient ModAttributeDouble posZ = new ModAttributeDouble("PosZ", 0.0);
    public transient ModAttributeDouble scale = new ModAttributeDouble("Scale", 1.0);
    public transient ModAttributeBoolean enableAnimations = new ModAttributeBoolean("EnableAnimations", true);
    public transient ModAttributeBoolean fastAnimations = new ModAttributeBoolean("FastAnimations", false);
    public transient ModAttributeBoolean rainbowColor = new ModAttributeBoolean("RainbowColor", false);
    public transient ModAttributeInteger colorR = new ModAttributeInteger("ColorR", 255);
    public transient ModAttributeInteger colorG = new ModAttributeInteger("ColorG", 255);
    public transient ModAttributeInteger colorB = new ModAttributeInteger("ColorB", 255);
    public transient ModAttributeBoolean enchantmentRGB = new ModAttributeBoolean("EnchantmentRGB", false);

    private long animationStartTime;

    public CustomHandMod() {
        super("customhand", "Custom Hand", "Configure your right hand position, scale, animations, and colors.", ModCategoryEnum.RENDER);
        addAttrib(posX);
        addAttrib(posY);
        addAttrib(posZ);
        addAttrib(scale);
        addAttrib(enableAnimations);
        addAttrib(fastAnimations);
        addAttrib(rainbowColor);
        addAttrib(colorR);
        addAttrib(colorG);
        addAttrib(colorB);
        addAttrib(enchantmentRGB);
    }

    @Override
    public void onEnable() {
        animationStartTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (!isEnabled() || Minecraft.getMinecraft().player == null) return;
        
        event.setCanceled(true);
        
        GlStateManager.pushMatrix();
        
        // Position
        GlStateManager.translate((float)posX.value / 10f, (float)posY.value / 10f, (float)posZ.value / 10f);
        
        // Scale
        GlStateManager.scale((float)scale.value, (float)scale.value, (float)scale.value);
        
        // Animation speed
        if (!enableAnimations.value) {
            // Reset arm rotation to default idle position
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            player.swingProgress = 0;
        }
        
        if (fastAnimations.value) {
            // Speed up swing animation
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player.swingProgress > 0) {
                player.swingProgress = Math.min(1.0f, player.swingProgress * 2.0f);
            }
        }
        
        // Color
        float r, g, b;
        if (rainbowColor.value) {
            long time = System.currentTimeMillis() - animationStartTime;
            float hue = (time % 3000) / 3000f;
            r = (float)Math.abs(Math.sin(hue * 2 * Math.PI));
            g = (float)Math.abs(Math.sin((hue + 0.33f) * 2 * Math.PI));
            b = (float)Math.abs(Math.sin((hue + 0.66f) * 2 * Math.PI));
        } else {
            r = colorR.value / 255f;
            g = colorG.value / 255f;
            b = colorB.value / 255f;
        }
        
        GlStateManager.color(r, g, b, 1.0f);
        
        GlStateManager.popMatrix();
    }
}