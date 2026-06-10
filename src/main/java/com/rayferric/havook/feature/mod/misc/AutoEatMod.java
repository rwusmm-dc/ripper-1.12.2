package com.rayferric.havook.feature.mod.misc;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AutoEatMod extends Mod {
    private static final int HUNGER_THRESHOLD = 10;
    private static final int EAT_DURATION = 32;

    private transient int previousHotbarSlot = -1;
    private transient int swappedInventorySlot = -1;
    private transient int activeFoodSlot = -1;
    private transient int eatingTicks = 0;
    private transient boolean wasEating = false;

    public AutoEatMod() {
        super("autoeat", "Auto Eat", "Automatically eats food when your hunger drops to half or lower.", ModCategoryEnum.MISC);
    }

    @Override
    public void onDisable() {
        restoreHotbarSlot();
        resetEatingState();
    }

    @Override
    public void onLocalPlayerUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || Minecraft.getMinecraft().world == null)
            return;

        boolean isEating = player.isHandActive() && player.getActiveHand() == EnumHand.MAIN_HAND;
        ItemStack heldItem = player.getHeldItemMainhand();

        if (player.getFoodStats().getFoodLevel() >= 20) {
            if (isEating) {
                stopEating(player);
            }
            restoreHotbarSlot();
            resetEatingState();
            return;
        }

        if (player.getFoodStats().getFoodLevel() > HUNGER_THRESHOLD && !isEating) {
            restoreHotbarSlot();
            resetEatingState();
            return;
        }

        if (wasEating && !isEating) {
            eatingTicks = 0;
        }
        wasEating = isEating;

        if (isEating) {
            eatingTicks++;
            if (eatingTicks >= EAT_DURATION || player.getFoodStats().getFoodLevel() >= 20) {
                stopEating(player);
                resetEatingState();
            }
            return;
        }

        if (swappedInventorySlot != -1 && !isEating) {
            restoreHotbarSlot();
        }

        ItemStack foodStack = findFoodStack(player);
        if (foodStack.isEmpty())
            return;

        if (activeFoodSlot >= 9) {
            int hotbarSlot = player.inventory.currentItem;
            swappedInventorySlot = activeFoodSlot;
            previousHotbarSlot = hotbarSlot;
            Minecraft.getMinecraft().playerController.windowClick(player.inventoryContainer.windowId,
                    swappedInventorySlot, hotbarSlot, ClickType.SWAP, player);
            return;
        }

        if (activeFoodSlot != player.inventory.currentItem) {
            player.inventory.currentItem = activeFoodSlot;
            return;
        }

        Minecraft.getMinecraft().playerController.processRightClick(player, Minecraft.getMinecraft().world, EnumHand.MAIN_HAND);
        eatingTicks = 0;
    }

    private void stopEating(EntityPlayerSP player) {
        Minecraft.getMinecraft().playerController.onStoppedUsingItem(player);
    }

    private void resetEatingState() {
        eatingTicks = 0;
        wasEating = false;
    }

    private ItemStack findFoodStack(EntityPlayerSP player) {
        activeFoodSlot = -1;

        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (isValidFood(stack) && !isRareFood(stack)) {
                activeFoodSlot = slot;
                return stack;
            }
        }

        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (isValidFood(stack)) {
                activeFoodSlot = slot;
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isValidFood(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ItemFood || stack.getItemUseAction() == net.minecraft.item.EnumAction.EAT);
    }

    private static boolean isRareFood(ItemStack stack) {
        return stack.getItem() instanceof ItemAppleGold;
    }

    private void restoreHotbarSlot() {
        if (Minecraft.getMinecraft().player == null)
            return;

        if (swappedInventorySlot != -1) {
            Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.inventoryContainer.windowId,
                    swappedInventorySlot, previousHotbarSlot, ClickType.SWAP, Minecraft.getMinecraft().player);
        }

        if (previousHotbarSlot != -1)
            Minecraft.getMinecraft().player.inventory.currentItem = previousHotbarSlot;

        previousHotbarSlot = -1;
        swappedInventorySlot = -1;
        activeFoodSlot = -1;
    }
}