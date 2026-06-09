package com.rayferric.havook.feature.mod.combat;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModAttributeDouble;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import com.rayferric.havook.manager.FriendManager;
import com.rayferric.havook.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

public class KillAuraMod extends Mod {
    public transient ModAttributeDouble speed = new ModAttributeDouble("Speed", 7.0);
    public transient ModAttributeDouble range = new ModAttributeDouble("Range", 4.0);
    public transient ModAttributeBoolean useCooldown = new ModAttributeBoolean("UseCooldown", true);
    public transient ModAttributeBoolean attackPlayers = new ModAttributeBoolean("AttackPlayers", true);
    public transient ModAttributeBoolean attackMobs = new ModAttributeBoolean("AttackMobs", true);
    public transient ModAttributeBoolean attackAnimals = new ModAttributeBoolean("AttackAnimals", true);
    public transient ModAttributeBoolean attackInvisibleEntities = new ModAttributeBoolean("AttackInvisibleEntities", true);
    public transient ModAttributeBoolean checkLineOfSight = new ModAttributeBoolean("CheckLineOfSight", false);
    public transient ModAttributeBoolean attackMultipleTargets = new ModAttributeBoolean("AttackMultipleTargets", false);
    public transient ModAttributeBoolean useAxeToBreakShield = new ModAttributeBoolean("UseAxeToBreakShield", true);
    public transient ModAttributeBoolean attackWhileMainhandInUse = new ModAttributeBoolean("AttackWhileMainhandInUse", false);

    private transient long time;

    public KillAuraMod() {
        super("killaura", "Kill Aura", "Automatically attacks nearby entities.", ModCategoryEnum.COMBAT);
        addAttrib(speed);
        addAttrib(range);
        addAttrib(useCooldown);
        addAttrib(attackPlayers);
        addAttrib(attackMobs);
        addAttrib(attackAnimals);
        addAttrib(attackInvisibleEntities);
        addAttrib(checkLineOfSight);
        addAttrib(attackMultipleTargets);
        addAttrib(useAxeToBreakShield);
        addAttrib(attackWhileMainhandInUse);
    }

    @Override
    public void onEnable() {
        time = System.currentTimeMillis();
    }

    @Override
    public void onLocalPlayerUpdate() {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null)
            return;
        if (useCooldown.value ? Minecraft.getMinecraft().player.getCooledAttackStrength(0) < 1
                : (speed.value == 0 || System.currentTimeMillis() - time < 1000.0d / speed.value))
            return;

        boolean attacked = false;
        if (attackMultipleTargets.value) {
            for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
                attacked |= attackTarget(entity);
            }
        } else {
            Entity target = findTarget();
            attacked = target != null && attackTarget(target);
        }

        if (attacked)
            time = System.currentTimeMillis();
    }

    private boolean attackTarget(Entity target) {
        if (!isEligibleTarget(target))
            return false;

        if (target instanceof EntityPlayer) {
            if (!attackPlayers.value || FriendManager.isFriend(target.getName()))
                return false;
            double angle1 = (((EntityPlayer) target).rotationYaw + 180) % 360;
            double angle2 = Minecraft.getMinecraft().player.rotationYaw % 360;
            if (angle1 < 0)
                angle1 += 360;
            if (angle2 < 0)
                angle2 += 360;
            if (isPlayerShielded((EntityPlayer) target)
                    && 180 - Math.abs(Math.abs(angle1 - angle2) - 180) < 95
                    && (!useAxeToBreakShield.value
                            || !(Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemAxe)))
                return false;
        } else if (target instanceof IMob) {
            if (!attackMobs.value)
                return false;
        } else if (EntityUtil.isAnimal(target)) {
            if (!attackAnimals.value)
                return false;
        } else if (!(target instanceof EntityLivingBase) || target instanceof EntityItem) {
            return false;
        }

        if (isPlayerShielded(Minecraft.getMinecraft().player)
                || (!attackWhileMainhandInUse.value && isPlayerUsingMainhand(Minecraft.getMinecraft().player)))
            return false;

        Minecraft.getMinecraft().playerController.attackEntity(Minecraft.getMinecraft().player, target);
        Minecraft.getMinecraft().player.swingArm(EnumHand.MAIN_HAND);
        return true;
    }

    private Entity findTarget() {
        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
            if (!isEligibleTarget(entity))
                continue;
            double distance = Minecraft.getMinecraft().player.getDistance(entity);
            if (distance > range.value)
                continue;
            if (checkLineOfSight.value && !isVisible(entity))
                continue;
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = entity;
            }
        }

        return closest;
    }

    private boolean isEligibleTarget(Entity target) {
        return target != null && target != Minecraft.getMinecraft().player && !target.isDead
                && (!(target instanceof EntityLivingBase) || ((EntityLivingBase) target).getHealth() >= 0)
                && (!target.isInvisible() || attackInvisibleEntities.value);
    }

    private boolean isVisible(Entity target) {
        Vec3d eyePos = new Vec3d(Minecraft.getMinecraft().player.posX,
                Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight(),
                Minecraft.getMinecraft().player.posZ);
        Vec3d targetPos = new Vec3d(target.posX, target.posY + target.height * 0.5, target.posZ);
        return Minecraft.getMinecraft().world.rayTraceBlocks(eyePos, targetPos, false, true, false) == null;
    }

    private static boolean isPlayerShielded(EntityPlayer player) {
        return player.getItemInUseCount() > 0 && (player.getHeldItemMainhand().getItem() instanceof ItemShield
                || (player.getHeldItemOffhand().getItem() instanceof ItemShield && !isPlayerUsingMainhand(player)));
    }

    private static boolean isPlayerUsingMainhand(EntityPlayer player) {
        ItemStack main = player.getHeldItemMainhand();
        return player.getItemInUseCount() > 0 && ((main.getItemUseAction() == EnumAction.EAT
                && (!player.isCreative() && (player.getFoodStats().needFood() || main.getItem() instanceof ItemAppleGold)))
                || (main.getItemUseAction() == EnumAction.BOW && canShootBow(player))
                || main.getItemUseAction() == EnumAction.DRINK || main.getItemUseAction() == EnumAction.BLOCK);
    }

    private static boolean canShootBow(EntityPlayer player) {
        if (player.isCreative())
            return true;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == Items.ARROW)
                return true;
        }
        return false;
    }
}