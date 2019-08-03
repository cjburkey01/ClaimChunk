package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.AutoTabCompletion;
import com.cjburkey.claimchunk.cmd.CommandHandler;
import com.cjburkey.claimchunk.cmd.Commands;
import com.cjburkey.claimchunk.data.newdata.BulkMySQLDataHandler;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import com.cjburkey.claimchunk.data.newdata.JsonDataHandler;
import com.cjburkey.claimchunk.data.newdata.MySQLDataHandler;
import com.cjburkey.claimchunk.event.CancellableChunkEvents;
import com.cjburkey.claimchunk.event.PlayerConnectionHandler;
import com.cjburkey.claimchunk.event.PlayerMovementHandler;
import com.cjburkey.claimchunk.lib.Metrics;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.player.SimplePlayerData;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.update.SemVer;
import com.cjburkey.claimchunk.update.UpdateChecker;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.io.File;
import java.io.IOException;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClaimChunk extends JavaPlugin {

    private static ClaimChunk instance;

    private boolean useEcon = false;

    private SemVer version;
    private SemVer availableVersion;
    private boolean updateAvailable;
    private IClaimChunkDataHandler dataHandler;
    private CommandHandler cmd;
    private Commands cmds;
    private Econ economy;
    private ChunkHandler chunkHandler;
    private PlayerHandler playerHandler;
    private RankHandler rankHandler;
    private Messages messages;

    public ClaimChunk() {
        instance = this;
    }

    public static ClaimChunk getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        System.out.println("Please put this jar file in your /plugins/ folder.");
        System.exit(0);
    }

    @Override
    public void onLoad() {
        // Load the config
        setupConfig();
        Utils.debug("Config set up.");

        // Enable WorldGuard support if possible
        if (WorldGuardHandler.init()) Utils.log("WorldGuard support enabled.");
        else Utils.log("WorldGuard support not enabled because the WorldGuard plugin was not found.");
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
        cmd = new CommandHandler();
        cmds = new Commands();
        economy = new Econ();
        chunkHandler = new ChunkHandler(dataHandler);
        playerHandler = new PlayerHandler(dataHandler);
        rankHandler = new RankHandler(new File(getDataFolder(), "/data/ranks.json"));
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

        // TODO: DYNMAP INTEGRATION
        // Initialize Dynmap integration
        /*if (DynmapHandler.init()) Utils.log("Initialized Dynmap integration.");
        else Utils.log("Failed to initialize Dynmap integration: Dynmap not found");*/

        // Load the stored data
        try {
            dataHandler.load();
            rankHandler.readFromDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.debug("Loaded data.");

        // Schedule the data saver
        scheduleDataSaver();
        Utils.debug("Scheduled data saving.");

        // Schedule the automatic unclaim task
        int check = Config.getInt("chunks", "unclaimCheckIntervalTicks");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::handleAutoUnclaim, check, check);
        Utils.debug("Scheduled unclaimed chunk checker.");

        Utils.log("Initialization complete.");
    }

    private void initUpdateChecker() {
        if (Config.getBool("basic", "checkForUpdates")) {
            // Wait 5 seconds before actually performing the update check
            getServer().getScheduler().runTaskLaterAsynchronously(this, this::doUpdateCheck, 100);
        }
    }

    private void doUpdateCheck() {
        try {
            version = SemVer.fromString(getDescription().getVersion());
            availableVersion = UpdateChecker.getLatestTag("cjburkey01", "ClaimChunk");
            if (availableVersion == null) {
                throw new IllegalStateException("Failed to get latest version of ClaimChunk from GitHub");
            }
            if (availableVersion.isNewerThan(version)) {
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
        if (Config.getBool("log", "anonymousMetrics")) {
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
                    (Config.getBool("database", "useDatabase"))
                            ? (
                            (Config.getBool("database", "groupRequests"))
                                    ? new BulkMySQLDataHandler<>(this::createJsonDataHandler, JsonDataHandler::deleteFiles)
                                    : new MySQLDataHandler<>(this::createJsonDataHandler, JsonDataHandler::deleteFiles))
                            : createJsonDataHandler();
        }
        Utils.debug("Using data handler \"%s\"", dataHandler.getClass().getName());
        try {
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
            messages = Messages.load(new File(getDataFolder(), "/messages.json"));
        } catch (IOException e) {
            Utils.err("Failed to load ClaimChunk/messages.json");
            e.printStackTrace();
        }
    }

    private boolean initEcon() {
        // Determine if the economy might exist
        useEcon = (Config.getBool("economy", "useEconomy")
                && (getServer().getPluginManager().getPlugin("Vault") != null));

        // Initialize the economy
        if (useEcon) {
            if (!economy.setupEconomy(this)) {
                Utils.err("Economy could not be setup. Make sure that you have an economy plugin (like Essentials) installed. ClaimChunk has been disabled.");
                return false;
            }
            Utils.debug("Economy set up.");
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                    () -> Utils.debug("Money Format: %s", economy.format(99132.76d)), 0L); // Once everything is loaded.
        } else {
            Utils.log("Economy not enabled. Either it was disabled with config or Vault was not found.");
        }
        return true;
    }

    private JsonDataHandler createJsonDataHandler() {
        return new JsonDataHandler(
                new File(getDataFolder(), "/data/claimedChunks.json"),
                new File(getDataFolder(), "/data/playerData.json")
        );
    }

    private void handleAutoUnclaim() {
        int length = Config.getInt("chunks", "automaticUnclaimSeconds");
        // Less than 1 will disable the check
        if (length < 1) return;

        long time = System.currentTimeMillis();
        for (Player player : getServer().getOnlinePlayers()) {
            playerHandler.setLastJoinedTime(player.getUniqueId(), time);
        }
        for (SimplePlayerData player : playerHandler.getJoinedPlayers()) {
            if (player.lastOnlineTime > 1000 && player.lastOnlineTime < (time - (1000 * length))) {
                ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(player.player);
                for (ChunkPos chunk : claimedChunks) {
                    try {
                        chunkHandler.unclaimChunk(getServer().getWorld(chunk.getWorld()), chunk.getX(), chunk.getZ());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Utils.log("Unclaimed all chunks of player \"%s\" (%s)", player.lastIgn, player.player);
            }
        }
    }

    @Override
    public void onDisable() {
        if (dataHandler != null) {
            try {
                dataHandler.save();
                Utils.debug("Saved data.");

                dataHandler.exit();
                Utils.debug("Cleaned up.");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Allows swapping the external data handler if the server is reloading
            dataHandler = null;
        }
        Utils.log("Finished disable.");
    }

    private void setupConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void setupEvents() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionHandler(), this);
        getServer().getPluginManager().registerEvents(new CancellableChunkEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementHandler(), this);
    }

    private void setupCommands() {
        cmds.register(cmd);
        PluginCommand command = getCommand("chunk");
        if (command != null) {
            command.setExecutor(cmd);
            command.setTabCompleter(new AutoTabCompletion());
        }
    }

    private void scheduleDataSaver() {
        // From minutes, calculate after how long in ticks to save data.
        int saveTimeTicks = Config.getInt("data", "saveDataInterval") * 60 * 20;

        // Async because possible lag when saving and loading.
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::reloadData, saveTimeTicks, saveTimeTicks);
    }

    private void reloadData() {
        try {
            dataHandler.save();
            dataHandler.load();
            rankHandler.readFromDisk();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.err("Couldn't reload data: \"%s\"", e.getMessage());
        }
    }

    private void disable() {
        getServer().getPluginManager().disablePlugin(this);
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
        if (this.dataHandler != null) throw new DataHandlerAlreadySetException(
                dataHandler.getClass().getName(),
                this.dataHandler.getClass().getName()
        );
        this.dataHandler = dataHandler;
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
