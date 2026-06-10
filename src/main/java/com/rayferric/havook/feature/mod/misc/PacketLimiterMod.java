package com.rayferric.havook.feature.mod.misc;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeInteger;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PacketLimiterMod extends Mod {
    public transient ModAttributeInteger maxPacketsPerTick = new ModAttributeInteger("MaxPacketsPerTick", 15);

    private int packetsSentThisTick = 0;
    private long lastTickTime = 0;

    public PacketLimiterMod() {
        super("packetlimiter", "Packet Limiter", "Prevents sending too many packets per tick (auto-caps CPS to 12-15).", ModCategoryEnum.MISC);
        addAttrib(maxPacketsPerTick);
    }

    @SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START) return;
        if (!isEnabled()) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTickTime >= 50) { // ~20 ticks per second
            packetsSentThisTick = 0;
            lastTickTime = currentTime;
        }
    }

    public boolean canSendPacket() {
        if (!isEnabled()) return true;
        if (packetsSentThisTick >= maxPacketsPerTick.value) {
            return false;
        }
        packetsSentThisTick++;
        return true;
    }
}