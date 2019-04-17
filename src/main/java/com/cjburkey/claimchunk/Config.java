package com.cjburkey.claimchunk;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private static String full(String section, String name) {
        return String.format("%s.%s", section, name);
    }

    private static FileConfiguration getConfig() {
        return ClaimChunk.getInstance().getConfig();
    }

    public static boolean getBool(String section, String name) {
        return getConfig().getBoolean(full(section, name));
    }

    public static int getInt(String section, String name) {
        return getConfig().getInt(full(section, name));
    }

    public static double getDouble(String section, String name) {
        return getConfig().getDouble(full(section, name));
    }

    public static String getString(String section, String name) {
        return getConfig().getString(full(section, name));
    }

    public static ChatColor getColor(String name) {
        return ChatColor.valueOf(getString("colors", name));
    }

}
