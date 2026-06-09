package com.rayferric.havook.feature.mod.combat;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class AutoTotemMod extends Mod {
    public AutoTotemMod() {
        super("autotem", "Auto Totem", "Refills your offhand with a totem when it becomes empty.",
                ModCategoryEnum.COMBAT);
    }

    @Override
    public void onLocalPlayerUpdate() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || player.world == null)
            return;

        ItemStack offhand = player.getHeldItemOffhand();
        if (!offhand.isEmpty())
            return;

        int totemSlot = findTotemSlot(player);
        if (totemSlot == -1)
            return;

        Minecraft.getMinecraft().playerController.windowClick(player.inventoryContainer.windowId, totemSlot, 0,
                ClickType.PICKUP, player);
        Minecraft.getMinecraft().playerController.windowClick(player.inventoryContainer.windowId, 45, 0,
                ClickType.PICKUP, player);
    }

    private static int findTotemSlot(EntityPlayerSP player) {
        for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
            ItemStack stack = player.inventory.mainInventory.get(slot);
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING)
                return convertInventorySlot(slot);
        }
        return -1;
    }

    private static int convertInventorySlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }
}