package com.rayferric.havook.feature.mod.misc;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModAttributeBoolean;
import com.rayferric.havook.feature.mod.ModAttributeInteger;
import com.rayferric.havook.feature.mod.ModAttributeString;
import com.rayferric.havook.feature.mod.ModCategoryEnum;
import com.rayferric.havook.util.ChatUtil;
import net.minecraft.client.Minecraft;

public class AutoSendMessageMod extends Mod {
    public transient ModAttributeString message = new ModAttributeString("Message", "");
    public transient ModAttributeInteger interval = new ModAttributeInteger("Interval", 60);
    public transient ModAttributeString unit = new ModAttributeString("Unit", "seconds");
    public transient ModAttributeBoolean enabled = new ModAttributeBoolean("Enabled", false);

    private transient long lastSentTime = 0;
    private transient long intervalMs = 60000;

    public AutoSendMessageMod() {
        super("autosendmessage", "Auto Send Message", "Automatically sends a message at a set interval.", ModCategoryEnum.MISC);
        addAttrib(message);
        addAttrib(interval);
        addAttrib(unit);
        addAttrib(enabled);
    }

    @Override
    public void onEnable() {
        updateIntervalMs();
        lastSentTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onLocalPlayerUpdate() {
        if (!enabled.value) return;
        if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastSentTime >= intervalMs) {
            String msg = message.value;
            if (msg != null && !msg.isEmpty()) {
                Minecraft.getMinecraft().player.sendChatMessage(msg);
                lastSentTime = now;
            }
        }
    }

    public void updateIntervalMs() {
        int intervalValue = interval.value;
        String unitValue = unit.value;
        if (unitValue == null) unitValue = "seconds";

        switch (unitValue.toLowerCase()) {
            case "seconds":
            case "second":
            case "s":
                intervalMs = intervalValue * 1000L;
                break;
            case "minutes":
            case "minute":
            case "m":
                intervalMs = intervalValue * 60000L;
                break;
            case "hours":
            case "hour":
            case "h":
                intervalMs = intervalValue * 3600000L;
                break;
            default:
                intervalMs = intervalValue * 1000L;
                break;
        }
    }

    public void setMessage(String msg) {
        message.value = msg;
    }

    public void setInterval(int intervalValue) {
        interval.value = intervalValue;
        updateIntervalMs();
    }

    public void setUnit(String unitValue) {
        unit.value = unitValue;
        updateIntervalMs();
    }

    public void setEnabled(boolean enabledValue) {
        enabled.value = enabledValue;
        if (enabledValue) onEnable();
        else onDisable();
    }
}