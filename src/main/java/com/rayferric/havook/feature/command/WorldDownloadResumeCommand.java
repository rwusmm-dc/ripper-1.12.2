package com.rayferric.havook.feature.command;

import com.rayferric.havook.feature.Command;
import com.rayferric.havook.feature.mod.misc.WorldDownloadMod;
import com.rayferric.havook.manager.ModManager;
import com.rayferric.havook.util.ChatUtil;

public class WorldDownloadResumeCommand extends Command {
    public WorldDownloadResumeCommand() {
        super("worlddownloadresume", ".worlddownloadresume", "Resume world download from the highest numbered world-N folder in downloads.");
    }

    @Override
    public void execute(String[] args) {
        WorldDownloadMod mod = (WorldDownloadMod) ModManager.getModById("worlddownload");
        if (mod == null) {
            ChatUtil.error("World Download mod not found!");
            return;
        }

        mod.resumeFromLatest();
    }
}