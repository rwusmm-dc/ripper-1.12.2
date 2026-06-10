package com.rayferric.havook.feature.mod.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModAttributeInteger;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import com.rayferric.havook.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.util.math.AxisAlignedBB;

public class FindDonkeyMod extends Mod {
    public transient ModAttributeBoolean tracers = new ModAttributeBoolean("Tracers", true);
    public transient ModAttributeBoolean showMules = new ModAttributeBoolean("ShowMules", true);
    public transient ModAttributeInteger maxDistance = new ModAttributeInteger("MaxDistance", 100);
    public transient ModAttributeBoolean showHorse = new ModAttributeBoolean("ShowHorse", false);

    private transient List<Entity> ENTITIES = new ArrayList<Entity>();
    private transient int BOX = 0;

    public FindDonkeyMod() {
        super("finddonkey", "Find Donkey", "Highlights donkeys and mules for easy finding.", ModCategoryEnum.RENDER);

        addAttrib(tracers);
        addAttrib(showMules);
        addAttrib(showHorse);
        addAttrib(maxDistance);
    }

    @Override
    public void onEnable() {
        BOX = GL11.glGenLists(1);
        GL11.glNewList(BOX, GL11.GL_COMPILE);
        RenderUtil.drawOutlinedBox(new AxisAlignedBB(-0.5, 0, -0.5, 0.5, 1.5, 0.5));
        GL11.glEndList();
    }

    @Override
    public void onDisable() {
        GL11.glDeleteLists(BOX, 1);
    }

    @Override
    public void onLocalPlayerUpdate() {
        ENTITIES.clear();
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
            return;

        for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
            if (entity.isDead)
                continue;

            boolean isDonkey = entity instanceof EntityDonkey;
            boolean isMule = entity instanceof EntityMule;
            boolean isHorse = entity instanceof EntityHorse && !(entity instanceof EntityDonkey) && !(entity instanceof EntityMule);

            if (!isDonkey && !isMule && !isHorse)
                continue;

            if (isMule && !showMules.value)
                continue;

            if (isHorse && !showHorse.value)
                continue;

            double distance = Minecraft.getMinecraft().player.getDistance(entity);
            if (distance > maxDistance.value)
                continue;

            ENTITIES.add(entity);
        }
    }

    @Override
    public void onRenderWorldLast(float partialTicks) {
        if (ENTITIES.isEmpty())
            return;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2);

        GL11.glPushMatrix();
        GL11.glTranslated(-Minecraft.getMinecraft().getRenderManager().renderPosX,
                -Minecraft.getMinecraft().getRenderManager().renderPosY,
                -Minecraft.getMinecraft().getRenderManager().renderPosZ);

        RenderUtil.drawESPBoxes(ENTITIES, BOX, partialTicks);
        if (tracers.value)
            RenderUtil.drawESPTracers(ENTITIES);

        GL11.glPopMatrix();

        GL11.glPopAttrib();
    }
}