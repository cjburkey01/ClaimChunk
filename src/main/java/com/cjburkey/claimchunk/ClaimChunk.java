package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.*;
import com.cjburkey.claimchunk.cmd.*;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfile;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfileManager;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.ccconfig.*;
import com.cjburkey.claimchunk.data.newdata.*;
import com.cjburkey.claimchunk.event.*;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;
import com.cjburkey.claimchunk.lib.Metrics;
import com.cjburkey.claimchunk.placeholder.ClaimChunkPlaceholders;
import com.cjburkey.claimchunk.player.*;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.smartcommand.CCBukkitCommand;
import com.cjburkey.claimchunk.update.*;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;

import lombok.Getter;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// TODO: Split this plugin up into services that users can use
//       Services:
//          • Chunk handler
//              • Claimed chunks
//              • Access handling (per-chunk)
//          • World profile handler
//              • Access handling (per-world)
//          • Player handler
//              • Bidirectional UUID<->Username mapping
//              • Access handling (per-user)
//                  • Protection configuration overrides
//       Events:
//          • (Interface) CCCancellable
//              • void cancel(String reason)
//              • boolean isCancelled()
//          • (Abstract) CCChunkEvent
//              • World, Chunk
//          • (Abstract) CCAccessEvent
//              • UUID owner, UUID accessor
//          • PlayerClaimEvent: CCChunkEvent + CCCancellable
//              • Player, (mutable) Option<double> cost,
//          • PlayerUnclaimEvent: CCChunkEvent + CCCancellable
//              • Player, (mutable) Option<double> refund,
//          • PlayerGrantAccessEvent: CCAccessEvent + CCCancellable
//              • (Nullable) Player ownerPly, (Nullable) Player accessorPly
//          • PlayerTakeAccessEvent: CCAccessEvent + CCCancellable
//              • (Nullable) Player ownerPly, (Nullable) Player accessorPly
//          • ChunkAutoUnclaimEvent
//              • UUID owner, List<Chunk>

public final class ClaimChunk extends JavaPlugin {

    // The global instance of ClaimChunk on this server
    // A plugin can only exist in one instance on any given server so it's ok to have a static
    // instance
    private static ClaimChunk instance;
    // Set once ClaimChunk has registered the `chunk-claim` flag with WorldGuard.
    private static boolean worldGuardRegisteredFlag = false;

    // The configuration file
    private ClaimChunkConfig config;
    // The current version of the plugin
    @Getter private SemVer version;
    // The latest available version of the plugin available online
    @Nullable @Getter private SemVer availableVersion;
    // Whether an update is currently available
    private boolean updateAvailable;

    // The current data handler
    private IClaimChunkDataHandler dataHandler;

    // Whether the plugin should use an economy plugin
    private boolean useEcon = false;
    // An instance of the ClaimChunk economy handler
    private Econ economy;

    // An instance of the chunk handler
    private ChunkHandler chunkHandler;
    // An instance of the player handler
    private PlayerHandler playerHandler;
    // An instance of the rank handler
    private RankHandler rankHandler;
    // An instance of the world permissions manager
    private ClaimChunkWorldProfileManager profileManager;
    // The main handler (may not always be here, please don't rely on this)
    @Getter private MainHandler mainHandler;
    @Getter private ChunkOutlineHandler chunkOutlineHandler;
    // Config conversion storage
    private HashMap<String, ClaimChunkWorldProfile> convertedConfigProfiles = null;

    // An instance of the class responsible for handling all localized messages
    private V2JsonMessages messages;

    // PlaceholderAPI support
    private ClaimChunkPlaceholders placeholders;

    // A list that contains all the players that are in admin mode.
    @Getter private final AdminOverride adminOverride = new AdminOverride();

    public static void main(String[] args) {
        // The user tried to run this jar file like a program
        // It is meant to be used as a Spigot/Bukkit/Paper/etc Java plugin
        System.out.println("Please put this jar file in your /plugins/ folder.");
        System.exit(0);
    }

    @Override
    public void onLoad() {
        // Assign the global instance to this instance of the plugin
        instance = this;

        // Initialize static utilities
        Utils.init(this);

        // Enable debug messages, if its enabled in config
        if (getConfig().getBoolean("log.debug")) {
            Utils.overrideDebugEnable();
        } else {
            Utils.overrideDebugDisable();
        }

        // Get the current plugin version
        version = SemVer.fromString(getDescription().getVersion());
        if (version.marker != null) {
            Utils.overrideDebugEnable();
            Utils.debug("Plugin version is nonstandard release %s", version);
        }

        // Try to update the config to 0.0.23+ if it has old values.
        convertedConfigProfiles = tryConvertConfig0_0_23();

        // Load the config
        setupConfig();
        Utils.debug("Config set up.");

        // Initialize the world profile manager
        profileManager =
                new ClaimChunkWorldProfileManager(
                        this,
                        new File(getDataFolder(), "/worlds/"),
                        new CCConfigParser(),
                        new CCConfigWriter());

        // Initialize the chunk particle outline system
        Particle particle;
        try {
            particle = Particle.valueOf(config.getChunkOutlineParticle());
        } catch (Exception e) {
            particle = Particle.SMOKE_NORMAL;
        }
        chunkOutlineHandler =
                new ChunkOutlineHandler(
                        this,
                        particle,
                        20 / config.getChunkOutlineSpawnPerSec(),
                        config.getChunkOutlineHeightRadius(),
                        config.getChunkOutlineParticlesPerSpawn());

        // Check if the WorldGuard flag has already been registered
        if (!worldGuardRegisteredFlag) {
            // Enable WorldGuard support if possible
            if (WorldGuardHandler.init(this)) {
                worldGuardRegisteredFlag = true;
                Utils.log("WorldGuard support enabled.");
            } else {
                Utils.log(
                        "WorldGuard support not enabled because the WorldGuard plugin was not"
                                + " found.");
            }
        } else {
            Utils.log("Skipped registering WorldGuard flag, it's already initialized");
        }
    }

    @Override
    public void onEnable() {
        // Check for an update
        initUpdateChecker();

        // Start data collection with bStats
        initAnonymousData();

        // Initialize the data handler and exit if it fails
        if (!initDataHandler()) {
            disable();
            return;
        }

        // Initialize all the variables
        // cmd = new CommandHandler(this);
        economy = new Econ();
        chunkHandler = new ChunkHandler(dataHandler, this);
        playerHandler = new PlayerHandler(dataHandler, this);
        // As of version 0.0.23, the `ranks.json` file will be located in
        // `/plugins/ClaimChunk` instead of `/plugins/ClaimChunk/data` to make
        // it more accessible. The rank handler will automatically copy the
        // file to the new location if an old one exists but the new one
        // doesn't. The old file *won't be deleted* but it won't be loaded
        // once the new one exists either.
        rankHandler =
                new RankHandler(
                        new File(getDataFolder(), "/ranks.json"),
                        new File(getDataFolder(), "/data/ranks.json"),
                        this);

        // Initialize the messages displayed to the player
        initMessages();

        // Initialize the economy
        initEcon();

        // Initialize all the subcommands
        setupNewCommands();
        Utils.debug("Commands set up.");

        // Register the event handlers we'll use
        setupEvents();
        Utils.debug("Events set up.");

        // Load the stored data
        try {
            dataHandler.load();
        } catch (Exception e) {
            Utils.err("Failed to load the data handler, ClaimChunk will be disabled!");
            Utils.err("Here is the error for reference:");
            e.printStackTrace();
            disable();
            return;
        }
        Utils.debug("Loaded chunk data.");

        // Load the rank file
        try {
            rankHandler.readFromDisk();
        } catch (Exception e) {
            Utils.err("Failed to load ranks! No ranks will be loaded!");
            Utils.err("Here is the error for reference:");
            e.printStackTrace();
        }
        Utils.debug("Loaded rank data.");

        // Save our converted profile information after the worlds for the server have been loaded
        saveConvertedProfiles();

        // If the server doesn't have a world named "world", we can remove it because we only used
        // it as a default during conversion.
        if (getServer().getWorlds().stream().map(World::getName).noneMatch("world"::equals)) {
            profileManager.removeProfile("world");
        }

        // Initialize the PlaceholderAPI expansion for ClaimChunk
        try {
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                placeholders = new ClaimChunkPlaceholders(this);
                if (placeholders.register()) {
                    Utils.log("Successfully enabled the ClaimChunk PlaceholderAPI expansion!");
                } else {
                    Utils.err("PlaceholderAPI is present but setting up the API failed!");
                }
            } else {
                Utils.log("PlaceholderAPI not found, not loading API.");
            }
        } catch (Exception e) {
            Utils.err(
                    "An error occurred while trying to enable the PlaceholderAPI expansion for"
                            + " claimchunk placeholders!");
            Utils.err("Here is the error for reference:");
            e.printStackTrace();
        }

        // Schedule the data saver
        scheduleDataSaver();
        Utils.debug("Scheduled data saving.");

        // Schedule the automatic unclaim task
        int check = config.getUnclaimCheckIntervalTicks();
        getServer()
                .getScheduler()
                .scheduleSyncRepeatingTask(this, this::handleAutoUnclaim, check, check);
        Utils.debug("Scheduled unclaimed chunk checker.");

        // Done!
        Utils.log("Initialization complete.");
    }

    // This is going to be an UGLY method, but ideally I'll shift things around and hide this away
    // in some other class.
    private HashMap<String, ClaimChunkWorldProfile> tryConvertConfig0_0_23() {
        // Create a default profile for the "world" and copy it whenever a new world is referenced
        // in the config. Note: If there is no world with the name "world", we should remove it
        // after obviously.
        HashMap<String, ClaimChunkWorldProfile> convertedProfiles = new HashMap<>();
        convertedProfiles.put("world", ClaimChunkWorldProfileManager.getDefaultProfile());

        // I don't like doing this often, but oh well.
        boolean needsBackup = false;

        if (getConfig().contains("protection.blockUnclaimedChunks")) {
            // Deny permissions in unclaimed chunks
            convertedProfiles.forEach(
                    (worldName, worldProfile) ->
                            worldProfile.unclaimedChunks.entityAccesses.put(
                                    EntityType.UNKNOWN, new EntityAccess(false, false, false)));
            convertedProfiles.forEach(
                    (worldName, worldProfile) ->
                            worldProfile.unclaimedChunks.blockAccesses.put(
                                    Material.AIR, new BlockAccess(false, false, false, false)));

            Utils.debug(
                    "Copied old `protection.blockUnclaimedChunks` setting from the config into"
                            + " world profiles");

            needsBackup = true;
        } else if (getConfig().contains("protection.blockUnclaimedChunksInWorlds")) {
            // If `blockUnclaimedChunks` is false, set up worlds in this list to deny unclaimed
            // chunk interactions.
            @SuppressWarnings("unchecked")
            List<String> list =
                    (List<String>) getConfig().getList("protection.blockUnclaimedChunksInWorlds");
            if (list != null) {
                for (String world : list) {
                    ClaimChunkWorldProfile profile =
                            convertedProfiles.computeIfAbsent(
                                    world,
                                    worldName ->
                                            new ClaimChunkWorldProfile(
                                                    Objects.requireNonNull(
                                                            convertedProfiles.get("world"))));

                    profile.unclaimedChunks.entityAccesses.put(
                            EntityType.UNKNOWN, new EntityAccess(false, false, false));
                    profile.unclaimedChunks.blockAccesses.put(
                            Material.AIR, new BlockAccess(false, false, false, false));
                }
            }

            Utils.debug(
                    "Copied old `protection.blockUnclaimedChunksInWorlds` setting from the config"
                            + " into world profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockPlayerChanges")
                && !getConfig().getBoolean("protection.blockPlayerChanges")) {
            // If this is false, we DON'T need to stop players destroying/placing blocks in claimed
            // chunks in any worlds.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        BlockAccess access = profile.claimedChunks.blockAccesses.get(Material.AIR);
                        if (access != null) {
                            access.allowBreak = true;
                            access.allowPlace = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockPlayerChanges` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockInteractions")
                && !getConfig().getBoolean("protection.blockInteractions")) {
            // If this is false, we DON'T need to prevent players interacting with blocks or
            // entities in claimed chunks.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        EntityAccess entityAccess =
                                profile.claimedChunks.entityAccesses.get(EntityType.UNKNOWN);
                        if (entityAccess != null) {
                            entityAccess.allowInteract = true;
                        }
                        BlockAccess blockAccess =
                                profile.claimedChunks.blockAccesses.get(Material.AIR);
                        if (blockAccess != null) {
                            blockAccess.allowInteract = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockInteractions` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.protectEntities")
                && !getConfig().getBoolean("protection.protectEntities")) {
            // If this is false, entities DON'T need to be protected from other players in claimed
            // chunks.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        EntityAccess entityAccess =
                                profile.claimedChunks.entityAccesses.get(EntityType.UNKNOWN);
                        if (entityAccess != null) {
                            entityAccess.allowDamage = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.protectEntities` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockTnt")
                && !getConfig().getBoolean("protection.blockTnt")) {
            // If this is false, disable explosion protection on entities and blocks
            convertedProfiles.forEach(
                    (world, profile) -> {
                        EntityAccess entityAccess =
                                profile.claimedChunks.entityAccesses.get(EntityType.UNKNOWN);
                        if (entityAccess != null) {
                            entityAccess.allowExplosion = true;
                        }
                        BlockAccess blockAccess =
                                profile.claimedChunks.blockAccesses.get(Material.AIR);
                        if (blockAccess != null) {
                            blockAccess.allowExplosion = true;
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockTnt` setting from the config into world profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockCreeper")
                || getConfig().contains("protection.blockWither")) {
            // These both should be handled by explosion protection now!
            needsBackup = true;
        }
        if (getConfig().contains("protection.blockFireSpread")
                && getConfig().getBoolean("protection.blockFireSpread")) {
            // Block fire spread into claimed chunks in all worlds if this is true
            convertedProfiles.forEach(
                    (world, profile) -> {
                        profile.fireSpread.fromClaimedIntoDiffClaimed = false;
                        profile.fireSpread.fromUnclaimedIntoClaimed = false;
                    });

            Utils.debug(
                    "Copied old `protection.blockFireSpread` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockFluidSpreadIntoClaims")) {
            // If this is true, we need to enable fluid spread prevention from unclaimed chunks into
            // claimed ones.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        profile.waterSpread.fromClaimedIntoDiffClaimed = false;
                        profile.waterSpread.fromUnclaimedIntoClaimed = false;
                    });

            Utils.debug(
                    "Copied old `protection.blockFluidSpreadIntoClaims` setting from the config"
                            + " into world profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockPistonsIntoClaims")) {
            // If this is true, we need to stop pistons extending from unclaimed chunks into claimed
            // chunks.
            convertedProfiles.forEach(
                    (world, profile) -> {
                        profile.pistonExtend.fromClaimedIntoDiffClaimed = false;
                        profile.pistonExtend.fromUnclaimedIntoClaimed = false;
                    });

            Utils.debug(
                    "Copied old `protection.blockPistonsIntoClaims` setting from the config into"
                            + " world profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockPvp")) {
            // TODO: If this is true, PvP needs to be disabled.
            // TODO: BEFORE THIS CAN HAPPEN, WE NEED TO GET PvP HANDLED SEPARATELY!!

            needsBackup = true;
        }
        if (getConfig().contains("protection.blockedCmds")) {
            // Add blocked commands for each world
            convertedProfiles.forEach(
                    (world, profile) -> {
                        @SuppressWarnings("unchecked")
                        List<String> commands =
                                (List<String>) getConfig().getList("protection.blockedCmds");
                        if (commands != null) {
                            profile.blockedCmdsInDiffClaimed.addAll(commands);
                        }
                    });

            Utils.debug(
                    "Copied old `protection.blockedCmds` setting from the config into world"
                            + " profiles");

            needsBackup = true;
        }
        if (getConfig().contains("protection.disableOfflineProtect")
                && getConfig().getBoolean("protection.disableOfflineProtect")) {
            // Set each world to deny protections to owned claimed chunks for offline players
            convertedProfiles.values().forEach(profile -> profile.protectOffline = false);
        }

        // Perform the backup if any old values are present.
        if (needsBackup) {
            backupConfigPost0_0_23();

            Utils.debug("Converted old config options to world profiles, removing from config.");

            // Unset all of the config values (if they're set)
            getConfig().set("protection.blockUnclaimedChunks", null);
            getConfig().set("protection.blockUnclaimedChunksInWorlds", null);
            getConfig().set("protection.blockPlayerChanges", null);
            getConfig().set("protection.blockInteractions", null);
            getConfig().set("protection.blockTnt", null);
            getConfig().set("protection.blockCreeper", null);
            getConfig().set("protection.blockWither", null);
            getConfig().set("protection.blockFireSpread", null);
            getConfig().set("protection.blockFluidSpreadIntoClaims", null);
            getConfig().set("protection.blockPistonsIntoClaims", null);
            getConfig().set("protection.protectEntities", null);
            getConfig().set("protection.blockPvp", null);
            getConfig().set("protection.blockedCmds", null);
            getConfig().set("protection.disableOfflineProtect", null);
            saveConfig();

            Utils.debug("Wrote the update config.");

            return convertedProfiles;
        }

        return null;
    }

    private void saveConvertedProfiles() {
        if (convertedConfigProfiles != null) {
            // Debug
            Utils.debug("%s profiles to create", convertedConfigProfiles.size());

            // Load all the worlds to generate defaults
            // Note: If the config was just converted over, then those profiles will be used in
            // place of the defaults :)
            for (World world : getServer().getWorlds()) {
                // If we have converted profiles to load, check if this world is in them.
                ClaimChunkWorldProfile convertedProfile =
                        convertedConfigProfiles.get(world.getName());

                // If we don't have a converted file for this world, check if we have one for the
                // default "world"
                if (convertedProfile == null) {
                    Utils.debug("Loading world profile for world \"%s\"", world.getName());
                    convertedProfile =
                            new ClaimChunkWorldProfile(convertedConfigProfiles.get("world"));
                } else {
                    Utils.debug(
                            "Loading converted world profile for world \"%s\"", world.getName());
                }

                // The getProfile method makes a lookup to determine if this world has a profile. We
                // know it won't have a profile because we haven't added any to this handler yet. By
                // providing a default, the handler will save that default if the world profile
                // config file doesn't exist.
                profileManager.getProfile(world.getName(), convertedProfile);
            }
        } else {
            // If we don't have any conversions to do, just load the profiles as the default and
            // create the files as necessary.
            getServer().getWorlds().stream()
                    .map(World::getName)
                    .forEach(profileManager::getProfile);
        }
    }

    private void backupConfigPost0_0_23() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            try {
                File backupConfig = new File(getDataFolder(), "config-pre-0.0.23.yml");
                if (!backupConfig.exists()) {
                    // Copy the config to a new file
                    Files.copy(
                            configFile.toPath(),
                            backupConfig.toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    Utils.log("Config already backed up.");
                }
            } catch (IOException e) {
                Utils.err("An error occurred while making a backup of the config file!");
                Utils.err("More information:");
                e.printStackTrace();
                Utils.err(
                        "Attempting to shut the server down because the plugin needs to"
                                + " convert the data to work (disabling the plugin would be"
                                + " even worse) and it's not safe to do so without a"
                                + " backup.");
                Utils.err(
                        "Note: you can also do this manually by removing all of the"
                                + " config values under the \"protections\" label except"
                                + " for \"disableOfflineProtect\"; you will, however, need"
                                + " to update the files within the"
                                + " \"plugins/ClaimChunk/worlds\" folder to match your"
                                + " desired configuration beyond the defaults.");
                disable();
                System.exit(0);
            }
        }
    }

    private void initUpdateChecker() {
        if (config.getCheckForUpdates()) {
            // Wait 5 seconds before actually performing the update check
            getServer().getScheduler().runTaskLaterAsynchronously(this, this::doUpdateCheck, 100);
        }
    }

    private void doUpdateCheck() {
        try {
            // Get the latest online plugin version
            availableVersion = UpdateChecker.getLatestRelease("cjburkey01", "ClaimChunk");

            // Make sure the latest available version is valid
            if (availableVersion == null) {
                Utils.err("Failed to get latest version of ClaimChunk from GitHub");
                return;
            }

            if (availableVersion.isNewerThan(version)) {
                // If the latest available version is newer than the current plugin version, the
                // server
                // should be updated
                updateAvailable = true;
                Utils.log(
                        "An update for ClaimChunk is available! Your version: %s | Latest version:"
                                + " %s",
                        version, availableVersion);
            } else {
                Utils.log("You are using the latest version of ClaimChunk: %s", version);
            }
        } catch (Exception e) {
            Utils.err("Failed to check for update");
            e.printStackTrace();
        }
    }

    private void initAnonymousData() {
        // bStats: https://bstats.org/
        if (config.getAnonymousMetrics()) {
            try {
                // Service ID obtained from https://bstats.org/what-is-my-plugin-id
                Metrics metrics = new Metrics(this, 5179);
                if (metrics.metricsBase.enabled) {
                    Utils.debug("Enabled anonymous metrics collection with bStats.");
                } else {
                    Utils.debug("Anonymous metric collection is disabled in the bStats config.");
                }
            } catch (Exception e) {
                Utils.err("Failed to initialize anonymous metrics collection: %s", e.getMessage());
            }
        } else {
            Utils.debug("Disabled anonymous metrics collection.");
        }
    }

    private boolean initDataHandler() {
        // Initialize the data handler if another plugin hasn't substituted one already
        if (dataHandler == null) {
            // The ternary operator is great
            // But it's ugly sometimes
            // Yuck!
            dataHandler =
                    (config.getUseDatabase())
                            ? ((config.getGroupRequests())
                                    ? new BulkMySQLDataHandler<>(
                                            this,
                                            this::createJsonDataHandler,
                                            JsonDataHandler::deleteFiles)
                                    : new MySQLDataHandler<>(
                                            this,
                                            this::createJsonDataHandler,
                                            JsonDataHandler::deleteFiles))
                            : createJsonDataHandler();
        }
        Utils.debug("Using data handler \"%s\"", dataHandler.getClass().getName());
        try {
            // Initialize the data handler
            dataHandler.init();
            return true;
        } catch (Exception e) {
            Utils.err(
                    "Failed to initialize data storage system \"%s\", disabling ClaimChunk.",
                    dataHandler.getClass().getName());
            e.printStackTrace();
            Utils.err("CLAIMCHUNK WILL NOT WORK WITHOUT A VALID DATA STORAGE SYSTEM!");
            Utils.err(
                    "Please double check your config and make sure it's set to the correct data"
                            + " information to ensure ClaimChunk can operate normally");
        }
        return false;
    }

    private void initMessages() {
        try {
            // Try to load the messages json file
            messages = V2JsonMessages.load(new File(getDataFolder(), "/messages.json"));
        } catch (IOException e) {
            Utils.err("Failed to load ClaimChunk/messages.json");
            e.printStackTrace();
        }
    }

    private void initEcon() {
        // Check if the economy is enabled and Vault is present
        useEcon =
                config.getUseEconomy() && getServer().getPluginManager().getPlugin("Vault") != null;

        // Try to initialize the economy if it should exist
        if (useEcon) {
            // Try to setup the Vault economy
            if (economy.setupEconomy(this)) {
                // It was successful
                Utils.debug("Economy set up.");

                // Display the money format as an economy debug
                getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                this,
                                () -> Utils.debug("Money Format: %s", economy.format(99132.76d)),
                                0L); // Once everything is loaded.
                return;
            }

            // Vault failed to initialize its economy.
            Utils.err(
                    "The Vault economy could not be setup. Make sure that you have an economy"
                            + " plugin (like Essentials) installed. The economy feature has been"
                            + " disabled; chunk claiming and unclaiming will be free.");
            useEcon = false;
        }

        // Something prevented the economy from being enabled.
        Utils.log("Economy not enabled.");
    }

    private JsonDataHandler createJsonDataHandler() {
        // Create the basic JSON data handler
        return new JsonDataHandler(
                this,
                new File(getDataFolder(), "/data/claimedChunks.json"),
                new File(getDataFolder(), "/data/playerData.json"));
    }

    private void handleAutoUnclaim() {
        int length = config.getAutomaticUnclaimSeconds();
        // Less than 1 will disable the check
        if (length < 1) return;

        // The current time
        long time = System.currentTimeMillis();

        for (Player player : getServer().getOnlinePlayers()) {
            // For every online player, set their most recent online time to the current time
            playerHandler.setLastJoinedTime(player.getUniqueId(), time);
        }

        for (SimplePlayerData player : playerHandler.getJoinedPlayers()) {
            // If the player has joined since time was recorded (that's 1s)
            boolean playerJoinedSinceTimeRecordUpdate = player.lastOnlineTime > 1000;
            // If the player hasn't been online recently enough
            boolean playerBeenOfflineTooLong = player.lastOnlineTime < (time - (1000L * length));

            if (playerJoinedSinceTimeRecordUpdate && playerBeenOfflineTooLong) {
                // Get a list of all the player's chunks
                ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(player.player);

                if (claimedChunks.length > 0) {
                    // Unclaim all of the player's chunks
                    for (ChunkPos chunk : claimedChunks) {
                        chunkHandler.unclaimChunk(
                                getServer().getWorld(chunk.getWorld()), chunk.getX(), chunk.getZ());
                    }

                    Utils.log(
                            "Unclaimed all chunks of player \"%s\" (%s)",
                            player.lastIgn, player.player);
                }
            }
        }
    }

    private void setupConfig() {
        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            getConfig().options().copyDefaults(true);
        } else {
            InputStream resourceStream = getResource("config.yml");
            if (resourceStream == null) {
                Utils.err("Failed to get config.yml from ClaimChunk jar");
                return;
            }
            // update config file
            FileConfiguration jarConfig =
                    YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
            reloadConfig();
            FileConfiguration tempConfig = getConfig();

            // add missing options
            for (String current : jarConfig.getKeys(true)) {
                if (!tempConfig.getKeys(true).contains(current)) {
                    tempConfig.set(current, jarConfig.get(current));
                }
            }
            // remove useless options
            for (String current : tempConfig.getKeys(true)) {
                if (!jarConfig.getKeys(true).contains(current)) {
                    if (!current.startsWith(".")) {
                        tempConfig.set(current, null);
                    }
                }
            }
        }
        saveConfig();
        config = new ClaimChunkConfig(getConfig());
    }

    private void setupEvents() {
        // Register all the event handlers
        getServer().getPluginManager().registerEvents(new PlayerConnectionHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(this), this);
        getServer().getPluginManager().registerEvents(new WorldProfileEventHandler(this), this);
    }

    private void setupNewCommands() {
        // TODO: CONFIG THIS
        final String claimChunkCommandName = "chunk";
        final String[] claimChunkCommandAliases = new String[0];

        // Create and register the `/chunk` command with Bukkit
        new CCBukkitCommand(claimChunkCommandName, claimChunkCommandAliases, this);

        // An archaic class controlling a shit-ton of shit. Needs to be cleaned up during the API
        // change :/
        mainHandler = new MainHandler(this);
    }

    private void scheduleDataSaver() {
        // From minutes, calculate after how long in ticks to save data.
        int saveTimeTicks = config.getSaveDataIntervalInMinutes() * 1200;

        // Async because possible lag when saving and loading.
        getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(this, this::taskSaveData, saveTimeTicks, saveTimeTicks);
    }

    private void taskSaveData() {
        try {
            // Save all the data
            dataHandler.save();

            // Reload ranks
            rankHandler.readFromDisk();

            // Unload all of the world profiles so they'll be loaded next time they're needed
            profileManager.unloadAllProfiles();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.err("Couldn't reload data: \"%s\"", e.getMessage());
        }
    }

    private void disable() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public ClaimChunkConfig chConfig() {
        return config;
    }

    public Econ getEconomy() {
        return economy;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    public ChunkHandler getChunkHandler() {
        return chunkHandler;
    }

    public RankHandler getRankHandler() {
        return rankHandler;
    }

    public ClaimChunkWorldProfileManager getProfileManager() {
        return profileManager;
    }

    public ClaimChunkPlaceholders getPlaceholderIntegration() {
        return placeholders;
    }

    public String fillPlaceholders(@Nullable CommandSender player, @NotNull String input) {
        if (getPlaceholderIntegration() != null) {
            // Ew :(
            return PlaceholderAPI.setPlaceholders(
                    player instanceof Player
                            ? (Player) player
                            : (player instanceof OfflinePlayer ? (OfflinePlayer) player : null),
                    input);
        }
        return input;
    }

    public boolean useEconomy() {
        return useEcon;
    }

    public V2JsonMessages getMessages() {
        return messages;
    }

    public boolean isUpdateAvailable() {
        return availableVersion != null && updateAvailable;
    }

    @SuppressWarnings("unused")
    public void overrideDataHandler(IClaimChunkDataHandler dataHandler)
            throws DataHandlerAlreadySetException {
        // Don't allow plugins to override a data handler if it's already set
        // The data handler must be set before ClaimChunk's onEnable is called (onLoad is good)
        if (this.dataHandler != null) {
            throw new DataHandlerAlreadySetException(
                    dataHandler.getClass().getName(), this.dataHandler.getClass().getName());
        }

        // Update the data handler
        this.dataHandler = dataHandler;
    }

    @Override
    public void onDisable() {
        // Cancel repeating tasks
        Bukkit.getScheduler().cancelTasks(this);

        // Cleanup data handler
        if (dataHandler != null) {
            try {
                // Save all the data
                dataHandler.save();
                Utils.debug("Saved data.");

                // Cleanup the data handler
                dataHandler.exit();
                Utils.debug("Cleaned up.");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Allow swapping the external data handler (if the server is reloading)
            dataHandler = null;
        }

        // Unset everything to allow full reloads
        config = null;
        version = null;
        availableVersion = null;
        economy = null;
        chunkHandler = null;
        playerHandler = null;
        rankHandler = null;
        profileManager = null;
        chunkOutlineHandler = null;
        placeholders = null;
        messages = null;
        mainHandler = null;

        Utils.log("Finished disable.");
    }

    /**
     * External quick access to the main ClaimChunk class.
     *
     * @return The current instance of ClaimChunk
     * @see org.bukkit.plugin.PluginManager#getPlugin(String)
     * @deprecated It is recommended to use {@code (ClaimChunk)
     *     Bukkit.getServer().getPluginManager().getPlugin("ClaimChunk")}
     */
    @Deprecated
    public static ClaimChunk getInstance() {
        return instance;
    }

    public static class DataHandlerAlreadySetException extends Exception {

        public static final long serialVersionUID = 49857948732L;

        private DataHandlerAlreadySetException(
                String newDataHandlerName, String existingDataHandlerName) {
            super(
                    "The ClaimChunk data handler was already set to \""
                            + existingDataHandlerName
                            + "\" and it cannot be set to \""
                            + newDataHandlerName
                            + "\". This may be because ClaimChunk has already been enabled or"
                            + " another plugin sets it first.");
        }
    }
}
