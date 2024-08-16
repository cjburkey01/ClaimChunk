package com.cjburkey.claimchunk.access;

import com.cjburkey.claimchunk.Utils;
import com.google.common.base.Charsets;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Keeps track of loading the permission flags specified in the flags.yml configuration file.
 *
 * @since 0.0.26
 */
public class CCPermFlags {

    public final HashMap<String, CCFlags.BlockFlagData> blockControls = new HashMap<>();
    public final HashMap<String, CCFlags.EntityFlagData> entityControls = new HashMap<>();

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
    public void load(
            @NotNull File flagsFile,
            @NotNull JavaPlugin plugin,
            @NotNull String defaultFlagsResource) {
        // Load the flags.yml file while ensuring the default exists
        YamlConfiguration config = readFlagFile(flagsFile, plugin, defaultFlagsResource);
        if (config == null) {
            throw new RuntimeException(
                    "Failed to load flag config file (see ClaimChunk errors; if no errors, try"
                            + " enabling debugSpam in the config.yml?)");
        }

        loadFromConfig(config);
    }

    /**
     * Load from the provided configuration data. The config should contain a section named
     * `permissionFlags` containing the flags.
     *
     * @param config The config file from which to load the user-defined flags.
     */
    public void loadFromConfig(@NotNull YamlConfiguration config) {
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
                    continue;
                }

                // Check if this is for blocks/entities
                switch (forType) {
                    case "BLOCKS" -> {
                        if (blockControls.containsKey(flagName)) {
                            Utils.err(
                                    "Flag \"%s\" already has block protections defined", flagName);
                            continue;
                        }
                        // Ugly generics make this easier I guess
                        CCFlags.BlockFlagData blockFlagData =
                                readFlagType(
                                        flagName,
                                        interactType,
                                        flagMap,
                                        CCFlags.BlockFlagData::new,
                                        CCFlags.BlockFlagType.class);
                        if (blockFlagData == null) {
                            Utils.err("Failed to load block flag data for flag \"%s\"", flagName);
                            continue;
                        }
                        blockControls.put(flagName, blockFlagData);
                    }
                    case "ENTITIES" -> {
                        if (entityControls.containsKey(flagName)) {
                            Utils.err(
                                    "Flag \"%s\" already has entity protections defined", flagName);
                            continue;
                        }
                        // Ugly generics make this easier I guess
                        CCFlags.EntityFlagData entityFlagData =
                                readFlagType(
                                        flagName,
                                        interactType,
                                        flagMap,
                                        CCFlags.EntityFlagData::new,
                                        CCFlags.EntityFlagType.class);
                        if (entityFlagData == null) {
                            Utils.err("Failed to load entity flag data for flag \"%s\"", flagName);
                            continue;
                        }
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

    private @Nullable YamlConfiguration readFlagFile(
            @NotNull File flagsFile,
            @NotNull JavaPlugin plugin,
            @NotNull String defaultFlagsResource) {
        if (flagsFile.exists()) {
            // Just load the config
            Utils.debug("Flag file already exists");
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
                Utils.debug("Loaded default flags from jar");
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
                Utils.log("Created new default flags.yml");
            } catch (Exception e) {
                Utils.err("Failed to save default flags file: %s", e.getMessage());
            }
            return ymlConfig;
        }
    }

    // Generics make this method look like hell, but I just extracted this :)
    private static <
                    FlagTypeEnum extends Enum<FlagTypeEnum>,
                    FlagDataType extends CCFlags.IFlagData<FlagTypeEnum>>
            @Nullable FlagDataType readFlagType(
                    @NotNull String flagName,
                    @NotNull String interactType,
                    @NotNull Map<?, ?> flagMap,
                    @NotNull BiFunction<FlagTypeEnum, CCFlags.FlagData, FlagDataType> makeFlagData,
                    @NotNull Class<FlagTypeEnum> typeEnumClass) {
        // Get the type of interaction to block
        FlagTypeEnum flagType;
        try {
            flagType = FlagTypeEnum.valueOf(typeEnumClass, interactType);
        } catch (Exception ignored) {
            Utils.err(
                    "Unknown interaction type \"%s\" in flag \"%s\" for %s",
                    interactType, flagName, typeEnumClass.getName());
            return null;
        }

        // Get the includes/excludes
        CCFlags.FlagData flagData = readIncludeExclude(flagMap);
        if (flagData == null) {
            Utils.err(
                    "Failed to load flag includes/excludes from flag \"%s\" for %s protections",
                    flagName, typeEnumClass.getName());
            return null;
        }

        // Add the protections
        return makeFlagData.apply(flagType, flagData);
    }

    // Please don't break :|
    @SuppressWarnings("unchecked")
    private static @Nullable CCFlags.FlagData readIncludeExclude(@NotNull Map<?, ?> flagMap) {
        try {
            return new CCFlags.FlagData(
                    (List<String>) flagMap.get("include"), (List<String>) flagMap.get("exclude"));
        } catch (Exception e) {
            Utils.err("Failed to read include/exclude data: %s", e.getMessage());
        }
        return null;
    }

    // -- CLASSES -- //

}
