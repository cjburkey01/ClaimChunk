package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.AutoTabCompletion;
import com.cjburkey.claimchunk.cmd.CommandHandler;
import com.cjburkey.claimchunk.cmd.Commands;
import com.cjburkey.claimchunk.data.n.IClaimChunkDataHandler;
import com.cjburkey.claimchunk.data.n.JsonDataHandler;
import com.cjburkey.claimchunk.data.n.MySQLDataHandler;
import com.cjburkey.claimchunk.event.CancellableChunkEvents;
import com.cjburkey.claimchunk.event.PlayerConnectionHandler;
import com.cjburkey.claimchunk.event.PlayerMovementHandler;
import com.cjburkey.claimchunk.lib.Metrics;
import com.cjburkey.claimchunk.player.DataPlayer;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.rank.RankHandler;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.io.File;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClaimChunk extends JavaPlugin {

    private static ClaimChunk instance;

    private boolean useEcon = false;

    private IClaimChunkDataHandler dataHandler;
    private CommandHandler cmd;
    private Commands cmds;
    private Econ economy;
    private ChunkHandler chunkHandler;
    private PlayerHandler playerHandler;
    private RankHandler rankHandler;

    public ClaimChunk() {
        instance = this;
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
        Utils.debug("Spigot version: %s", getServer().getBukkitVersion());

        // Initialize the storage files
        File chunkFile = new File(getDataFolder(), "/data/claimedChunks.json");
        File plyFile = new File(getDataFolder(), "/data/playerData.json");
        File rankFile = new File(getDataFolder(), "/data/ranks.json");

        // Initialize the data handler
        dataHandler = Config.getBool("database", "useDatabase")
                ? new MySQLDataHandler()
                : new JsonDataHandler(chunkFile, plyFile);
        try {
            dataHandler.init();
        } catch (Exception e) {
            Utils.err("Failed to initialize data storage system \"%s\": \"%s\", disabling ClaimChunk.", dataHandler.getClass().getName(), e.getMessage());
            e.printStackTrace();
            Utils.err("CLAIMCHUNK WILL NOT WORK WITHOUT A VALID DATA STORAGE SYSTEM!");
            Utils.err("Please double check your config to ensure it's set to the correct data information to ensure ClaimChunk can operate normally");
            getServer().getPluginManager().disablePlugin(this);
            dataHandler = null;
            return;
        }

        // Initialize all the variables
        cmd = new CommandHandler();
        cmds = new Commands();
        economy = new Econ();
        chunkHandler = new ChunkHandler(dataHandler);
        playerHandler = new PlayerHandler(dataHandler);
        rankHandler = new RankHandler(rankFile);

        /*
            !! WE NO LONGER CONVERT DATA FROM THE OLD SYSTEM (versions 0.0.4 and prior)!!!!       !!
            !! IF OLD DATA NEEDS TO BE CONVERTED, LAUNCH THE SERVER WITH ClaimChunk 0.0.12 FIRST, !!
            !! THEN 0.0.13+ CAN BE INSTALLED                                                      !!
         */

        // MCStats
        if (Config.getBool("log", "anonymousMetrics")) {
            try {
                Metrics metrics = new Metrics(this);
                if (metrics.start()) Utils.debug("Enabled anonymous metrics collection.");
                else Utils.err("Unable to initialize metrics collection");
            } catch (Exception e) {
                Utils.err("Failed to initialize anonymous metrics collection: %s", e.getMessage());
            }
        } else {
            Utils.debug("Disabled anonymous metrics collection.");
        }

        // Determine if the economy might exist
        useEcon = (Config.getBool("economy", "useEconomy")
                && (getServer().getPluginManager().getPlugin("Vault") != null));

        // Initialize the economy
        if (useEcon) {
            if (!economy.setupEconomy(this)) {
                Utils.err("Economy could not be setup. Make sure that you have an economy plugin (like Essentials) installed. ClaimChunk has been disabled.");
                disable();
                return;
            }
            Utils.debug("Economy set up.");
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                    () -> Utils.debug("Money Format: %s", economy.format(99132.76d)), 0L); // Once everything is loaded.
        } else {
            Utils.log("Economy not enabled. Either it was disabled with config or Vault was not found.");
        }

        // Initialize all the subcommands
        setupCommands();
        Utils.debug("Commands set up.");

        // Register the events we'll need
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

        // Prevent checking for players who haven't joined since this plugin was updated
        for (DataPlayer player : playerHandler.getJoinedPlayers()) {
            if (player.lastOnlineTime <= 0) {
                player.unclaimedAllChunks = true;
            }
        }
        int check = Config.getInt("chunks", "unclaimCheckIntervalTicks");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::handleAutoUnclaim, check, check);
        Utils.debug("Scheduled unclaimed chunk checker.");

        Utils.log("Initialization complete.");
    }

    private void handleAutoUnclaim() {
        int length = Config.getInt("chunks", "automaticUnclaimSeconds");
        // Less than will disable the check
        if (length < 1) return;

        long time = System.currentTimeMillis();
        for (Player player : getServer().getOnlinePlayers()) {
            playerHandler.getPlayer(player.getUniqueId()).lastOnlineTime = time;
            Utils.debug("Time: %s", time);
        }
        for (DataPlayer player : playerHandler.getJoinedPlayers()) {
            if (!player.unclaimedAllChunks && player.lastOnlineTime < (time - (1000 * length))) {
                ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(player.player);
                for (ChunkPos chunk : claimedChunks) {
                    try {
                        chunkHandler.unclaimChunk(getServer().getWorld(chunk.getWorld()), chunk.getX(), chunk.getZ());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Utils.log("Unclaimed all chunks of player \"%s\" (%s)", player.lastIgn, player.player);
                player.unclaimedAllChunks = true;
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
        Objects.requireNonNull(getCommand("chunk")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("chunk")).setTabCompleter(new AutoTabCompletion());
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

    public static ClaimChunk getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        System.out.println("Please put this jar file in your /plugins/ folder.");
        System.exit(0);
    }

}
