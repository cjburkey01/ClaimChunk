package com.cjburkey.claimchunk.access;

import com.cjburkey.claimchunk.Utils;
import com.google.common.base.Charsets;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Keeps track of loading the permission flags specified in the flags.yml configuration file.
 *
 * @since 0.0.26
 */
public class CCPermFlags {

    public final HashMap<String, BlockFlagData> blockControls = new HashMap<>();
    public final HashMap<String, EntityFlagData> entityControls = new HashMap<>();

    /**
     * Read the flags defined in the flag definitions file.
     *
     * @param flagsFile The file within the /plugins/ClaimChunk directory that stores the flag
     *     configurations.
     * @param plugin An instance of the plugin whose jar contains the default flags resource
     *     (probably an instance of ClaimChunk, used to call {@link
     *     JavaPlugin#getResource(String)}).
     * @param defaultFlagsResource The path to/name of the default flags resource in the plugin jar
     *     file.
     */
    public void load(File flagsFile, JavaPlugin plugin, String defaultFlagsResource) {
        // Load the flags.yml file while ensuring the default exists
        YamlConfiguration config = readFlagFile(flagsFile, plugin, defaultFlagsResource);
        if (config == null) {
            throw new RuntimeException("Failed to load flag config file (see ClaimChunk errors)");
        }

        loadFromConfig(config);
    }

    /**
     * Load from the provided configuration data. The config should contain a section named
     * `permissionFlags` containing the flags.
     *
     * @param config The config file from which to load the user-defined flags.
     */
    public void loadFromConfig(YamlConfiguration config) {
        // Read the flag section
        ConfigurationSection flagSection = config.getConfigurationSection("permissionFlags");
        if (flagSection == null) {
            throw new RuntimeException("Flag config file missing permissionFlags section");
        }

        // Read each flag name
        for (String flagName : flagSection.getKeys(false)) {
            // Get the list of maps (see src/resources/defaultFlags.yml for format)
            List<Map<?, ?>> flagEntries = flagSection.getMapList(flagName);
            if (flagEntries.isEmpty()) {
                Utils.err("Flag \"%s\" has no protections", flagName);
                continue;
            }

            // Loop through each map
            for (Map<?, ?> flagMap : flagEntries) {
                String forType = (String) flagMap.get("for");
                String interactType = (String) flagMap.get("type");
                if (interactType == null) {
                    Utils.err(
                            "Missing interaction type in one of the flag protection maps in flag"
                                    + " \"%s\"",
                            flagName);
                }

                // Check if this is for blocks/entities
                switch (forType) {
                    case "BLOCKS" -> {
                        if (blockControls.containsKey(flagName)) {
                            Utils.err(
                                    "Flag \"%s\" already has block protections defined", flagName);
                            continue;
                        }

                        // Get the type of interaction to block
                        BlockFlagType flagType;
                        try {
                            flagType = BlockFlagType.valueOf(interactType);
                        } catch (Exception ignored) {
                            Utils.err(
                                    "Unknown block interaction type \"%s\" in flag \"%s\"",
                                    interactType, flagName);
                            continue;
                        }

                        // Get the includes/excludes
                        FlagData flagData = readIncludeExclude(flagMap);
                        if (flagData == null) {
                            Utils.err(
                                    "Failed to load flag includes/excludes from flag \"%s\" for"
                                            + " block protections",
                                    flagName);
                        }

                        // Add the protections
                        BlockFlagData blockFlagData = new BlockFlagData(flagType, flagData);
                        blockControls.put(flagName, blockFlagData);
                    }
                    case "ENTITIES" -> {
                        if (entityControls.containsKey(flagName)) {
                            Utils.err(
                                    "Flag \"%s\" already has entity protections defined", flagName);
                            continue;
                        }

                        // Get the type of interaction to block
                        EntityFlagType flagType;
                        try {
                            flagType = EntityFlagType.valueOf(interactType);
                        } catch (Exception ignored) {
                            Utils.err(
                                    "Unknown entity interaction type \"%s\" in flag \"%s\"",
                                    interactType, flagName);
                            continue;
                        }

                        // Get the includes/excludes
                        FlagData flagData = readIncludeExclude(flagMap);
                        if (flagData == null) {
                            Utils.err(
                                    "Failed to load flag includes/excludes from flag \"%s\" for"
                                            + " entity protections",
                                    flagName);
                        }

                        // Add the protections
                        EntityFlagData entityFlagData = new EntityFlagData(flagType, flagData);
                        entityControls.put(flagName, entityFlagData);
                    }
                    default ->
                            Utils.err(
                                    "Invalid flag protection target \"%s\" for flag \"%s\"",
                                    forType, flagName);
                }
            }

            // Player property CJ-made-error safety check :)
            if (blockControls.isEmpty() && entityControls.isEmpty()) {
                throw new RuntimeException(
                        "ClaimChunk failed to load any block/entity protection flags, make sure the"
                            + " /plugins/ClaimChunk/flags.yml file is set up correctly (or allow it"
                            + " to regenerate)");
            }
        }
    }

    // Please don't break :|
    @SuppressWarnings("unchecked")
    private FlagData readIncludeExclude(Map<?, ?> flagMap) {
        try {
            return new FlagData(
                    (List<String>) flagMap.get("include"), (List<String>) flagMap.get("exclude"));
        } catch (Exception e) {
            Utils.err("Failed to read include/exclude data: %s", e.getMessage());
        }
        return null;
    }

    private YamlConfiguration readFlagFile(
            File flagsFile, JavaPlugin plugin, String defaultFlagsResource) {
        if (flagsFile.exists()) {
            // Just load the config
            return YamlConfiguration.loadConfiguration(flagsFile);
        } else {
            // Load the configuration from the defaultFlags.yml file
            YamlConfiguration ymlConfig;
            try {
                InputStream resource = plugin.getResource(defaultFlagsResource);
                ymlConfig =
                        YamlConfiguration.loadConfiguration(
                                new InputStreamReader(
                                        Objects.requireNonNull(
                                                resource,
                                                "Failed to locate resource at "
                                                        + defaultFlagsResource),
                                        Charsets.UTF_8));
            } catch (Exception e) {
                Utils.err(
                        "Failed to load default flag config (Is your file UTF-8?): %s",
                        e.getMessage());
                return null;
            }

            // Save the defaults
            try {
                ymlConfig.options().copyDefaults(true);
                ymlConfig.save(flagsFile);
            } catch (Exception e) {
                Utils.err("Failed to save default flags file: %s", e.getMessage());
            }
            return ymlConfig;
        }
    }

    // -- CLASSES -- //

    public enum BlockFlagType {
        BREAK,
        PLACE,
        INTERACT,
        EXPLODE,
    }

    public enum EntityFlagType {
        DAMAGE,
        INTERACT,
        EXPLODE,
    }

    public record FlagData(@Nullable List<String> include, @Nullable List<String> exclude) {}

    public record BlockFlagData(BlockFlagType flagType, FlagData flagData) {}

    public record EntityFlagData(EntityFlagType flagType, FlagData flagData) {}
}
