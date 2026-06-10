package com.rayferric.havook.feature.command;

import com.rayferric.havook.feature.Command;
import com.rayferric.havook.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class CoordsCommand extends Command {
    public CoordsCommand() {
        super("coords", ".coords", "Displays your current coordinates in chat (copyable).");
    }

    @Override
    public void execute(String[] args) {
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) {
            ChatUtil.error("You must be in a world to use this command.");
            return;
        }
        
        BlockPos pos = Minecraft.getMinecraft().player.getPosition();
        String coords = String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ());
        
        ChatUtil.info("Your coordinates: \247f" + coords);
        ChatUtil.info("\2477(Copy this text from chat)");
    }
}