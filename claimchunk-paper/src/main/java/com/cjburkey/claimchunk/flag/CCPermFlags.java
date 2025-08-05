package com.cjburkey.claimchunk.flag;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.newflag.YmlFlagParser;
import com.cjburkey.claimchunk.newflag.YmlPermissionFlag;
import com.google.common.base.Charsets;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Keeps track of loading the permission flags specified in the flags.yml configuration file.
 *
 * @since 1.0.0
 */
public class CCPermFlags {

    public final HashMap<String, CCFlags.BlockFlagData> blockControls = new HashMap<>();
    public final HashMap<String, CCFlags.EntityFlagData> entityControls = new HashMap<>();
    public final HashMap<String, String> flagDenyMessages = new HashMap<>();
    public @Nullable CCFlags.SimpleFlag pvpFlag = null;
    public @Nullable CCFlags.SimpleFlag pearlFlag = null;
    private final HashSet<String> allFlags = new HashSet<>();
    private final CCInteractClasses interactClasses;

    public CCPermFlags(ClaimChunk claimChunk) {
        this.interactClasses = claimChunk.getInteractClasses();
    }

    public CCPermFlags(CCInteractClasses interactClasses) {
        this.interactClasses = interactClasses;
    }

    /**
     * Read the flags defined in the flag definitions file.
     *
     * @param flagsFile The file within the /plugins/ClaimChunk directory that stores the flag
     *     configurations.
     * @param plugin An instance of the plugin whose jar contains the default flags resource
     *     (probably an instance of ClaimChunk, is used to call {@link
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

    public @NotNull Set<String> getAllFlags() {
        return Collections.unmodifiableSet(allFlags);
    }

    /**
     * Get which flag (if any) has protections over a given block when it is allowed/denied
     *
     * @param blockType The Bukkit type of the block to query.
     * @param interactionType The type of block operation to check.
     * @return The name of the flag that should protect the block, or {@code null} if no flags
     *     prohibit this action.
     */
    public @Nullable CCFlags.ProtectingFlag getProtectingFlag(
            Material blockType, CCFlags.BlockFlagType interactionType) {
        // Loop through each flag
        // Maybe separate flags by interaction type to make this lookup cheaper,
        // but there shouldn't ever be more than like 30 flags unless people go
        // crazy. If you or someone you love somehow uses more than like 20
        // flags on your/their server, please DM me on Discord! I'm glad you
        // like the plugin :)
        for (Map.Entry<String, CCFlags.BlockFlagData> blockControllingFlag :
                blockControls.entrySet()) {
            String flagName = blockControllingFlag.getKey();
            CCFlags.BlockFlagType flagType = blockControllingFlag.getValue().flagType();
            CCFlags.FlagData flagData = blockControllingFlag.getValue().flagData();

            if (flagType == interactionType) {
                // Check whether this flag's protections would apply to this
                // block based on the include/exclude data AND if the
                // `protectWhen` value would apply for the flag's current
                // enabled/disabled state
                if (flagApplies(
                        blockType, this::typeMatches, flagData.include(), flagData.exclude())) {
                    return new CCFlags.ProtectingFlag(flagName, flagData);
                }
            }
        }

        // No flags prevent interaction with this block
        return null;
    }

    /**
     * Get which flag (if any) should protect the given entity based on whether the flag is enabled.
     *
     * @param entityType The Bukkit type of the entity to query.
     * @param interactionType The type of entity operation to check.
     * @return The name of the flag that should protect the entity, or {@code null} if no flags
     *     prohibit this action.
     */
    public @Nullable CCFlags.ProtectingFlag getProtectingFlag(
            EntityType entityType, CCFlags.EntityFlagType interactionType) {
        // Loop through each flag
        for (Map.Entry<String, CCFlags.EntityFlagData> entityControllingFlag :
                entityControls.entrySet()) {
            String flagName = entityControllingFlag.getKey();
            CCFlags.EntityFlagType flagType = entityControllingFlag.getValue().flagType();
            CCFlags.FlagData flagData = entityControllingFlag.getValue().flagData();

            if (flagType == interactionType) {
                // Check whether this flag protects the entity
                if (flagApplies(
                        entityType, this::typeMatches, flagData.include(), flagData.exclude())) {
                    return new CCFlags.ProtectingFlag(flagName, flagData);
                }
            }
        }

        // No flags prevent interaction with this block
        return null;
    }

    // Whether the given block/entity applies to a flag, given its include and exclude sets
    private <T> boolean flagApplies(
            // Pass in the type to allow lambda references to `typeMatches`
            // Looks weird but makes me have to type less in two (2) other places!
            T t,
            @NotNull BiFunction<T, String, Boolean> typeMatchMethod,
            @Nullable Set<String> include,
            @Nullable Set<String> exclude) {
        Predicate<String> predicate = excludeStr -> typeMatchMethod.apply(t, excludeStr);

        // Exclusions override inclusions; excluded blocks/entities don't
        // match, because they're excluded 😲
        if (exclude != null) {
            if (exclude.stream().anyMatch(predicate)) {
                return false;
            }
        }

        // Now check if we include anything
        if (include != null) {
            return include.stream().anyMatch(predicate);
        }

        // If we have reached this point, both of these must be true:
        // - Exclusions are not provided or don't match this block
        // - Include must be null, so everything matches.
        // Therefore, this flag DOES match the given block/entity! Yay!
        return true;
    }

    private boolean typeMatches(@NotNull Material blockType, @NotNull String inputStr) {
        return typeMatch(
                blockType, inputStr, interactClasses::getBlockClasses, Utils::materialFromString);
    }

    private boolean typeMatches(@NotNull EntityType entityType, @NotNull String inputStr) {
        return typeMatch(
                entityType, inputStr, interactClasses::getEntityClasses, Utils::entityFromString);
    }

    private static <T> boolean typeMatch(
            @NotNull T type,
            @NotNull String inputStr,
            Function<T, Set<String>> getClasses,
            Function<String, T> fromString) {
        String input = inputStr.trim();

        if (input.startsWith("@")) {
            String className = input.substring(1).trim();
            return getClasses.apply(type).contains(className);
        } else {
            // parsedType is null if material/entity type is not found, so no exceptions
            return fromString.apply(input) == type;
        }
    }

    // -- LOADING -- //

    // TODO: REPLACE `loadFromConfig`
    public void loadFromConfig(@NotNull YamlConfiguration config) {
        // Read the flag section
        ConfigurationSection flagSection = config.getConfigurationSection("permissionFlags");
        if (flagSection == null) {
            throw new RuntimeException("Flag config file missing permissionFlags section");
        }

        for (final String flagName : flagSection.getKeys(false)) {
            if (!flagName.matches("[a-zA-Z0-9_-]+")) {
                Utils.err(
                        "Flag name \"%s\" isn't alphanumeric! Must be a string of A-Z, a-z, 0-9,"
                                + " '_', or '-'",
                        flagName);
                continue;
            }

            // Get the list of maps (see src/resources/defaultFlags.yml for format)
            List<Map<?, ?>> flagEntries = flagSection.getMapList(flagName);
            if (flagEntries.isEmpty()) {
                Utils.err("Flag \"%s\" has no protections", flagName);
                continue;
            }

            // Parse the permission flag with our brand-new parser!
            @SuppressWarnings("unchecked") YmlPermissionFlag permissionFlag = YmlFlagParser.parsePermissionFlag((Map<String, Object>) flagEntries);
            // TODO:
        }
    }

    /*
    /**
     * Load from the provided configuration data. The config should contain a section named
     * `permissionFlags` containing the flags.
     *
     * @param config The config file from which to load the user-defined flags.
     * /
    public void loadFromConfig(@NotNull YamlConfiguration config) {
        // Read the flag section
        ConfigurationSection flagSection = config.getConfigurationSection("permissionFlags");
        if (flagSection == null) {
            throw new RuntimeException("Flag config file missing permissionFlags section");
        }

        // Read each flag name
        for (final String flagName : flagSection.getKeys(false)) {
            if (!flagName.matches("[a-zA-Z0-9_-]+")) {
                Utils.err(
                        "Flag name \"%s\" isn't alphanumeric! Must be a string of A-Z, a-z, 0-9,"
                                + " '_', or '-'",
                        flagName);
                continue;
            }

            // Get the list of maps (see src/resources/defaultFlags.yml for format)
            List<Map<?, ?>> flagEntries = flagSection.getMapList(flagName);
            if (flagEntries.isEmpty()) {
                Utils.err("Flag \"%s\" has no protections", flagName);
                continue;
            }

            // Check for a deny message in this flag
            List<String> msgs =
                    flagEntries.stream()
                            .filter(map -> map.containsKey("denyMessage"))
                            .map(map -> (String) map.get("denyMessage"))
                            .toList();
            @Nullable String protectionMessage = null;
            if (!msgs.isEmpty()) {
                if (msgs.size() > 1) {
                    Utils.warn("Protection message set multiple times for flag %s", flagName);
                }
                protectionMessage = msgs.getFirst();
            }

            flagDenyMessages.put(flagName, protectionMessage);

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
                        allFlags.add(flagName);
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
                        allFlags.add(flagName);
                        entityControls.put(flagName, entityFlagData);
                    }
                    case "PLAYERS" -> {
                        if (pvpFlag != null) {
                            Utils.err("Flag \"%s\" already handles pvp protection", pvpFlag.name());
                            continue;
                        }
                        allFlags.add(flagName);
                        pvpFlag = new CCFlags.SimpleFlag(flagName, readProtectWhen(flagMap));
                    }
                    case "PEARLS" -> {
                        if (pearlFlag != null) {
                            Utils.err(
                                    "Flag \"%s\" already handles ender pearl protection",
                                    pearlFlag.name());
                            continue;
                        }
                        allFlags.add(flagName);
                        pearlFlag = new CCFlags.SimpleFlag(flagName, readProtectWhen(flagMap));
                    }
                    default ->
                            Utils.err(
                                    "Invalid flag protection target \"%s\" for flag \"%s\"",
                                    forType, flagName);
                }
            }
        }

        // Player property CJ-made-error safety check :)
        if (allFlags.isEmpty()) {
            Utils.log("No flags loaded! If this is intentional, no worries!");
        }
    }
    */

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


}
