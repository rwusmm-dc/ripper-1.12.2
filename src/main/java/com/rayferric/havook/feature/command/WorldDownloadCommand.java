package com.rayferric.havook.feature.command;

import com.rayferric.havook.feature.Command;
import com.rayferric.havook.feature.mod.misc.WorldDownloadMod;
import com.rayferric.havook.manager.ModManager;
import com.rayferric.havook.util.ChatUtil;

public class WorldDownloadCommand extends Command {
    public WorldDownloadCommand() {
        super("worlddownload", ".worlddownload", "Start/stop downloading the multiplayer world chunks you visit.");
    }

    @Override
    public void execute(String[] args) {
        WorldDownloadMod mod = (WorldDownloadMod) ModManager.getModById("worlddownload");
        if (mod == null) {
            ChatUtil.error("World Download mod not found!");
            return;
        }

        if (mod.isDownloading()) {
            mod.setEnabled(false);
        } else {
            mod.setEnabled(true);
        }
    }
}