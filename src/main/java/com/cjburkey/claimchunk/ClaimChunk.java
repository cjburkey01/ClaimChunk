package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.AutoTabCompletion;
import com.cjburkey.claimchunk.cmd.CommandHandler;
import com.cjburkey.claimchunk.cmd.Commands;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfileManager;
import com.cjburkey.claimchunk.data.newdata.BulkMySQLDataHandler;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import com.cjburkey.claimchunk.data.newdata.JsonDataHandler;
import com.cjburkey.claimchunk.data.newdata.MySQLDataHandler;
import com.cjburkey.claimchunk.event.PlayerConnectionHandler;
import com.cjburkey.claimchunk.event.PlayerMovementHandler;
import com.cjburkey.claimchunk.event.WorldProfileEventHandler;
import com.cjburkey.claimchunk.lib.Metrics;
import com.cjburkey.claimchunk.placeholder.ClaimChunkPlaceholders;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.player.SimplePlayerData;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.update.SemVer;
import com.cjburkey.claimchunk.update.UpdateChecker;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ClaimChunk extends JavaPlugin {

    // The global instance of ClaimChunk on this server
    // A plugin can only exist in one instance on any given server so it's ok to have a static instance
    private static ClaimChunk instance;

    // The configuration file
    private Config config;
    // The current version of the plugin
    private SemVer version;
    // The latest available version of the plugin available online
    private SemVer availableVersion;
    // Whether an update is currently available
    private boolean updateAvailable;

    // The current data handler
    private IClaimChunkDataHandler dataHandler;
    // TODO: REWRITE COMMAND SYSTEM
    // An instance of the command handler
    private CommandHandler cmd;

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

    // An instance of the class responsible for handling all localized messages
    private Messages messages;

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

        // Load the config
        setupConfig();
        Utils.debug("Config set up.");

        // Enable WorldGuard support if possible
        if (WorldGuardHandler.init(this)) {
            Utils.log("WorldGuard support enabled.");
        } else {
            Utils.log("WorldGuard support not enabled because the WorldGuard plugin was not found.");
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
        cmd = new CommandHandler(this);
        economy = new Econ();
        chunkHandler = new ChunkHandler(dataHandler, this);
        playerHandler = new PlayerHandler(dataHandler, this);
        // As of version 0.0.23, the `ranks.json` file will be located in
        // `/plugins/ClaimChunk` instead of `/plugins/ClaimChunk/data` to make
        // it more accessible. The rank handler will automatically copy the
        // file to the new location if an old one exists but the new one
        // doesn't. The old file *won't be deleted* but it won't be loaded
        // once the new one exists either.
        rankHandler = new RankHandler(new File(getDataFolder(), "/ranks.json"),
                                      new File(getDataFolder(), "/data/ranks.json"),
                                      this);
        profileManager = new ClaimChunkWorldProfileManager(new File(getDataFolder(), "/worlds/"));
        initMessages();

        /*
            !! WE NO LONGER CONVERT DATA FROM THE OLD SYSTEM (versions 0.0.4 and prior)!!!!       !!
            !! IF OLD DATA NEEDS TO BE CONVERTED, LAUNCH THE SERVER WITH ClaimChunk 0.0.12 FIRST, !!
            !! THEN 0.0.13+ CAN BE INSTALLED                                                      !!
         */

        // Initialize the economy and exit if it fails
        if (!initEcon()) {
            disable();
            return;
        }

        // Initialize all the subcommands
        setupCommands();
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
        Utils.debug("Loaded data.");

        // Load the rank file
        try {
            rankHandler.readFromDisk();
        } catch (Exception e) {
            Utils.err("Failed to load ranks! No ranks will be loaded!");
            Utils.err("Here is the error for reference:");
            e.printStackTrace();
        }
        Utils.debug("Loaded data.");

        // Initialize the PlaceholderAPI expansion for ClaimChunk
        try {
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                if (new ClaimChunkPlaceholders(this).register()) {
                    Utils.log("Successfully enabled the ClaimChunk PlaceholderAPI expansion!");
                } else {
                    Utils.err("PlaceholderAPI is present but setting up the API failed!");
                }
            } else {
                Utils.log("PlaceholderAPI not found, not loading API.");
            }
        } catch (Exception e) {
            Utils.err("An error occurred while trying to enable the PlaceholderAPI expansion for claimchunk placeholders!");
            Utils.err("Here is the error for reference:");
            e.printStackTrace();
        }

        // Schedule the data saver
        scheduleDataSaver();
        Utils.debug("Scheduled data saving.");

        // Schedule the automatic unclaim task
        int check = config.getInt("chunks", "unclaimCheckIntervalTicks");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::handleAutoUnclaim, check, check);
        Utils.debug("Scheduled unclaimed chunk checker.");

        Utils.log("Initialization complete.");
    }

    private void initUpdateChecker() {
        if (config.getBool("basic", "checkForUpdates")) {
            // Wait 5 seconds before actually performing the update check
            getServer().getScheduler().runTaskLaterAsynchronously(this, this::doUpdateCheck, 100);
        }
    }

    private void doUpdateCheck() {
        try {
            // Get the current plugin version
            version = SemVer.fromString(getDescription().getVersion());

            // Get the latest online plugin version
            availableVersion = UpdateChecker.getLatestRelease("cjburkey01", "ClaimChunk");

            // Make sure the latest available version is valid
            if (availableVersion == null) {
                throw new IllegalStateException("Failed to get latest version of ClaimChunk from GitHub");
            }

            if (availableVersion.isNewerThan(version)) {
                // If the latest available version is newer than the current plugin version, the server should be updated
                updateAvailable = true;
                Utils.log("An update for ClaimChunk is available! Your version: %s | Latest version: %s",
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
        if (config.getBool("log", "anonymousMetrics")) {
            try {
                Metrics metrics = new Metrics(this);
                if (metrics.isEnabled()) Utils.debug("Enabled anonymous metrics collection with bStats.");
                else Utils.debug("Anonymous metric collection is disabled in the bStats config.");
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
                    (config.getBool("database", "useDatabase"))
                            ? (
                            (config.getBool("database", "groupRequests"))
                                    ? new BulkMySQLDataHandler<>(this, this::createJsonDataHandler, JsonDataHandler::deleteFiles)
                                    : new MySQLDataHandler<>(this, this::createJsonDataHandler, JsonDataHandler::deleteFiles))
                            : createJsonDataHandler();
        }
        Utils.debug("Using data handler \"%s\"", dataHandler.getClass().getName());
        try {
            // Initialize the data handler
            dataHandler.init();
            return true;
        } catch (Exception e) {
            Utils.err("Failed to initialize data storage system \"%s\", disabling ClaimChunk.", dataHandler.getClass().getName());
            e.printStackTrace();
            Utils.err("CLAIMCHUNK WILL NOT WORK WITHOUT A VALID DATA STORAGE SYSTEM!");
            Utils.err("Please double check your config and make sure it's set to the correct data information to ensure ClaimChunk can operate normally");
        }
        return false;
    }

    private void initMessages() {
        try {
            // Try to load the messages json file
            messages = Messages.load(new File(getDataFolder(), "/messages.json"));
        } catch (IOException e) {
            Utils.err("Failed to load ClaimChunk/messages.json");
            e.printStackTrace();
        }
    }

    private boolean initEcon() {
        // Determine if the economy might exist
        useEcon = (config.getBool("economy", "useEconomy")
                && (getServer().getPluginManager().getPlugin("Vault") != null));

        // Initialize the economy
        if (useEcon) {
            // Try to setup the vault economy
            if (!economy.setupEconomy(this)) {
                Utils.err("Economy could not be setup. Make sure that you have an economy plugin (like Essentials) installed. ClaimChunk has been disabled.");
                return false;
            }
            Utils.debug("Economy set up.");

            // Display the money format as an economy debug
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                    () -> Utils.debug("Money Format: %s", economy.format(99132.76d)), 0L); // Once everything is loaded.
        } else {
            Utils.log("Economy not enabled. Either it was disabled with config or Vault was not found.");
        }
        return true;
    }

    private JsonDataHandler createJsonDataHandler() {
        // Create the basic JSON data handler
        return new JsonDataHandler(
                this, new File(getDataFolder(), "/data/claimedChunks.json"),
                new File(getDataFolder(), "/data/playerData.json")
        );
    }

    private void handleAutoUnclaim() {
        int length = config.getInt("chunks", "automaticUnclaimSeconds");
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

                // Unclaim all of the player's chunks
                for (ChunkPos chunk : claimedChunks) {
                    chunkHandler.unclaimChunk(getServer().getWorld(chunk.getWorld()), chunk.getX(), chunk.getZ());
                }

                Utils.log("Unclaimed all chunks of player \"%s\" (%s)", player.lastIgn, player.player);
            }
        }
    }

    @Override
    public void onDisable() {
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
        Utils.log("Finished disable.");
    }

    private void setupConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = new Config(getConfig());
    }

    private void setupEvents() {
        // Register all the event handlers
        getServer().getPluginManager().registerEvents(new PlayerConnectionHandler(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(this), this);
        getServer().getPluginManager().registerEvents(new WorldProfileEventHandler(this), this);
    }

    private void setupCommands() {
        // Register all the commands
        Commands.register(cmd);

        // Get the Spigot command
        PluginCommand command = getCommand("chunk");
        if (command != null) {
            // Use our custom plugin executor
            command.setExecutor(cmd);

            // Set the tab completer so tab complete works with all the sub commands
            command.setTabCompleter(new AutoTabCompletion(this));
        }
    }

    private void scheduleDataSaver() {
        // From minutes, calculate after how long in ticks to save data.
        int saveTimeTicks = config.getInt("data", "saveDataInterval") * 60 * 20;

        // Async because possible lag when saving and loading.
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::reloadData, saveTimeTicks, saveTimeTicks);
    }

    private void reloadData() {
        try {
            // Save all the data
            dataHandler.save();

            // Load the data
            dataHandler.load();

            // Reload ranks
            rankHandler.readFromDisk();

            // Reload world profiles
            profileManager.reloadAllProfiles();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.err("Couldn't reload data: \"%s\"", e.getMessage());
        }
    }

    private void disable() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public Config chConfig() {
        return config;
    }

    public CommandHandler getCommandHandler() {
        return cmd;
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

    public boolean useEconomy() {
        return useEcon;
    }

    public SemVer getVersion() {
        return version;
    }

    public SemVer getAvailableVersion() {
        return availableVersion;
    }

    public Messages getMessages() {
        return messages;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable && version != null && availableVersion != null;
    }

    @SuppressWarnings("unused")
    public void overrideDataHandler(IClaimChunkDataHandler dataHandler) throws DataHandlerAlreadySetException {
        // Don't allow plugins to override a data handler if it's already set
        // The data handler must be set before ClaimChunk's onEnable is called (onLoad is good)
        if (this.dataHandler != null) {
            throw new DataHandlerAlreadySetException(
                    dataHandler.getClass().getName(),
                    this.dataHandler.getClass().getName()
            );
        }

        // Update the data handler
        this.dataHandler = dataHandler;
    }

    /**
     * External quick access to the main ClaimChunk class.
     *
     * @return The current instance of ClaimChunk
     * @see org.bukkit.plugin.PluginManager#getPlugin(String)
     * @deprecated It is recommended to use {@code (ClaimChunk) Bukkit.getServer().getPluginManager().getPlugin("ClaimChunk")}
     */
    @Deprecated
    public static ClaimChunk getInstance() {
        return instance;
    }

    public static class DataHandlerAlreadySetException extends Exception {

        public static final long serialVersionUID = 49857948732L;

        private DataHandlerAlreadySetException(String newDataHandlerName, String existingDataHandlerName) {
            super("The ClaimChunk data handler was already set to \"" + existingDataHandlerName
                    + "\" and it cannot be set to \"" + newDataHandlerName
                    + "\". This may be because ClaimChunk has already been enabled or another plugin sets it first.");
        }

    }

}
