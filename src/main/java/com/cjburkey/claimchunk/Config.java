package com.cjburkey.claimchunk;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private static String full(String section, String name) {
        return String.format("%s.%s", section, name);
    }

    private static FileConfiguration getConfig() {
        return ClaimChunk.getInstance().getConfig();
    }

    public static boolean getBool(String section, String name, boolean def) {
        boolean val = getConfig().getBoolean(full(section, name), def);
        ClaimChunk.getInstance().saveConfig();
        return val;
    }

    public static int getInt(String section, String name, int def) {
        int val = getConfig().getInt(full(section, name), def);
        ClaimChunk.getInstance().saveConfig();
        return val;
    }

    public static double getDouble(String section, String name, double def) {
        double val = getConfig().getDouble(full(section, name), def);
        ClaimChunk.getInstance().saveConfig();
        return val;
    }

    public static String getString(String section, String name, String def) {
        String val = getConfig().getString(full(section, name), def);
        ClaimChunk.getInstance().saveConfig();
        return val;
    }

    public static List<String> getList(String section, String name) {
        return getConfig().getStringList(full(section, name));
    }

    private static ChatColor getColor(String name, String def) {
        return ChatColor.valueOf(getString("colors", name, def));
    }

    public static ChatColor errorColor() {
        return getColor("errorColor", "RED");
    }

    public static ChatColor infoColor() {
        return getColor("infoColor", "GOLD");
    }

    public static ChatColor successColor() {
        return getColor("successColor", "GREEN");
    }

}
