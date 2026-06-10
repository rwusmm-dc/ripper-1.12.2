package com.rayferric.havook.feature.mod.render;

import org.lwjgl.opengl.GL11;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;

public class BetterNametagMod extends Mod {
    public transient ModAttributeBoolean showSneakingPlayers = new ModAttributeBoolean("ShowSneakingPlayers", true);
    public transient ModAttributeBoolean upscaleDistantTags = new ModAttributeBoolean("UpscaleDistantTags", true);
    public transient ModAttributeBoolean showDistance = new ModAttributeBoolean("ShowDistance", true);
    public transient ModAttributeBoolean showArmor = new ModAttributeBoolean("ShowArmor", true);
    public transient ModAttributeBoolean showWeapon = new ModAttributeBoolean("ShowWeapon", true);
    public transient ModAttributeBoolean showPotionEffects = new ModAttributeBoolean("ShowPotionEffects", true);
    public transient ModAttributeBoolean showHealth = new ModAttributeBoolean("ShowHealth", true);

    public BetterNametagMod() {
        super("betternametag", "Better Nametag", "Shows distance, armor, weapon, potion effects and health above entity heads.", ModCategoryEnum.RENDER);
        addAttrib(showSneakingPlayers);
        addAttrib(upscaleDistantTags);
        addAttrib(showDistance);
        addAttrib(showArmor);
        addAttrib(showWeapon);
        addAttrib(showPotionEffects);
        addAttrib(showHealth);
    }

    @Override
    public void onRenderLivingSpecialsPre(RenderLivingEvent.Specials.Pre event) {
        EntityLivingBase entity = event.getEntity();
        if (!(entity instanceof EntityPlayer) || entity == Minecraft.getMinecraft().player)
            return;
        if (entity.isDead || entity.getHealth() < 0 || entity.isInvisible())
            return;

        EntityPlayer player = (EntityPlayer) entity;
        StringBuilder tag = new StringBuilder();
        
        // Player name
        tag.append("\2477").append(player.getDisplayName().getFormattedText());
        
        // Distance
        if (showDistance.value) {
            double distance = Minecraft.getMinecraft().player.getDistance(entity);
            tag.append(" \247b[\247f").append(String.format("%.1f", distance)).append("\247bm]");
        }
        
        // Health
        if (showHealth.value) {
            int health = (int) Math.ceil(entity.getHealth());
            String healthColor = health > 12 ? "\247a" : (health > 6 ? "\247e" : "\247c");
            tag.append(" ").append(healthColor).append(health);
        }
        
        // Armor
        if (showArmor.value) {
            ItemStack[] armor = player.inventory.armorInventory;
            for (int i = 3; i >= 0; i--) {
                ItemStack stack = armor[i];
                if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {
                    ItemArmor armorItem = (ItemArmor) stack.getItem();
                    int damage = stack.getMaxDamage() - stack.getItemDamage();
                    int maxDamage = stack.getMaxDamage();
                    double durability = (double) damage / maxDamage * 100;
                    String color = durability > 50 ? "\247a" : (durability > 25 ? "\247e" : "\247c");
                    tag.append(" ").append(color).append(armorItem.armorType.name().substring(0, 1).toUpperCase())
                       .append(" ").append(String.format("%.0f", durability)).append("%");
                }
            }
        }
        
        // Main hand weapon
        if (showWeapon.value) {
            ItemStack mainHand = player.getHeldItemMainhand();
            if (!mainHand.isEmpty()) {
                tag.append(" \247d[").append(mainHand.getDisplayName()).append("\247d]");
            }
        }
        
        // Potion effects
        if (showPotionEffects.value) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                tag.append(" \2475").append(effect.getPotion().getName().replace("effect.", "").substring(0, 3));
                if (effect.getAmplifier() > 0) {
                    tag.append(effect.getAmplifier() + 1);
                }
                tag.append(" ").append(effect.getDuration() / 20).append("s");
            }
        }

        GL11.glPushMatrix();
        Vec3d pos = new Vec3d(event.getX(), event.getY() + entity.height / 1.5, event.getZ());
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GL11.glTranslated(pos.x, pos.y + 1, pos.z);
        if (upscaleDistantTags.value) {
            double scale = Math.max(1, pos.distanceTo(new Vec3d(0, 0, 0)) / 6);
            GL11.glScaled(scale, scale, scale);
        }
        EntityRenderer.drawNameplate(Minecraft.getMinecraft().fontRenderer,
                tag.toString(), 0, 0, 0, 0,
                Minecraft.getMinecraft().getRenderManager().playerViewY,
                Minecraft.getMinecraft().getRenderManager().playerViewX,
                Minecraft.getMinecraft().gameSettings.thirdPersonView == 2, 
                showSneakingPlayers.value ? false : entity.isSneaking());
        GL11.glPopMatrix();
        event.setCanceled(true);
    }
}