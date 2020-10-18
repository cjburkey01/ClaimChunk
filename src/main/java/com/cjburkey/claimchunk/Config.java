package com.cjburkey.claimchunk;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private final FileConfiguration config;

    public Config(FileConfiguration configFile) {
        config = configFile;
    }

    public FileConfiguration getFileConfig() {
        return config;
    }

    public boolean getBool(String section, String name) {
        return config.getBoolean(full(section, name));
    }

    public int getInt(String section, String name) {
        return config.getInt(full(section, name));
    }

    public double getDouble(String section, String name) {
        return config.getDouble(full(section, name));
    }

    public String getString(String section, String name) {
        return config.getString(full(section, name));
    }

    public List<String> getList(String section, String name) {
        return config.getStringList(full(section, name));
    }

    private ChatColor getColor(String name) {
        return ChatColor.valueOf(getString("colors", name));
    }

    public ChatColor errorColor() {
        return getColor("errorColor");
    }

    public ChatColor infoColor() {
        return getColor("infoColor");
    }

    private static String full(String section, String name) {
        // Format the section and name into a single YAML location for the config option
        return String.format("%s.%s", section, name);
    }

}
