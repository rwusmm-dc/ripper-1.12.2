package com.rayferric.havook.handler;

import com.rayferric.havook.ai.AIManager;
import com.rayferric.havook.ai.GameStateCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class AIEventHandler {

	@SubscribeEvent
	public void onChatReceived(ClientChatReceivedEvent event) {
		if (!AIManager.isEnabled()) return;

		String raw = event.getMessage().getUnformattedText();
		if (raw == null || raw.isEmpty()) return;

		GameStateCollector.logChat(raw);

		int colonIdx = raw.indexOf(": ");
		if (colonIdx > 0 && colonIdx < raw.length() - 2) {
			String sender = raw.substring(0, colonIdx);
			String message = raw.substring(colonIdx + 2);
			AIManager.onChatReceived(sender, message);
		}
	}

	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent event) {
		if (!AIManager.isEnabled()) return;
		if (event.getEntity() == null || !event.getEntity().equals(Minecraft.getMinecraft().player)) return;

		Entity source = event.getSource().getTrueSource();
		if (source instanceof EntityPlayer) {
			AIManager.onPlayerDamaged(source.getName());
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if (!AIManager.isEnabled()) return;
		if (event.getEntity() == null || !event.getEntity().equals(Minecraft.getMinecraft().player)) return;

		Entity source = event.getSource().getTrueSource();
		if (source instanceof EntityPlayer) {
			AIManager.onPlayerDeath(source.getName());
		}
	}
}
