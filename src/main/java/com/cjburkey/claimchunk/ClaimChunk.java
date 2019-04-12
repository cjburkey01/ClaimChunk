package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.AutoTabCompletion;
import com.cjburkey.claimchunk.cmd.CommandHandler;
import com.cjburkey.claimchunk.cmd.Commands;
import com.cjburkey.claimchunk.data.DataConversion;
import com.cjburkey.claimchunk.event.CancellableChunkEvents;
import com.cjburkey.claimchunk.event.PlayerConnectionHandler;
import com.cjburkey.claimchunk.event.PlayerMovementHandler;
import com.cjburkey.claimchunk.player.DataPlayer;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.io.File;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClaimChunk extends JavaPlugin {

    private static ClaimChunk instance;

    private boolean useEcon = false;

    private CommandHandler cmd;
    private Commands cmds;
    private Econ economy;
    private ChunkHandler chunkHandler;
    private PlayerHandler playerHandler;

    public void onLoad() {
        instance = this;
    }

    public void onEnable() {
        Utils.log("Spigot version: %s", getServer().getBukkitVersion());

        File chunkFile = new File(getDataFolder(), "/data/claimedChunks.json");
        File plyFile = new File(getDataFolder(), "/data/playerData.json");

        cmd = new CommandHandler();
        cmds = new Commands();
        economy = new Econ();
        playerHandler = new PlayerHandler(false, plyFile);
        chunkHandler = new ChunkHandler(false, chunkFile);

        File oldChunks = new File(getDataFolder(), "/data/claimed.chks");
        File oldCache = new File(getDataFolder(), "/data/playerCache.dat");
        File oldAccess = new File(getDataFolder(), "/data/grantedAccess.dat");
        try {
            DataConversion.check(oldChunks, oldCache, oldAccess, this);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        setupConfig();
        Utils.log("Config set up.");

        useEcon = ((getServer().getPluginManager().getPlugin("Vault") != null)
                && Config.getBool("economy", "useEconomy"));

        if (useEcon) {
            if (!economy.setupEconomy(this)) {
                Utils.err("Economy could not be setup. Make sure that you have an economy plugin (like Essentials) installed. ClaimChunk has been disabled.");
                disable();
                return;
            }
            Utils.log("Economy set up.");
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                    () -> Utils.log("Money Format: %s", economy.format(99132.76d)), 0L); // Once everything is loaded.
        } else {
            Utils.log("Economy not enabled. Either it was disabled with config or Vault was not found.");
        }

        setupCommands();
        Utils.log("Commands set up.");

        setupEvents();
        Utils.log("Events set up.");

        try {
            chunkHandler.readFromDisk();
            playerHandler.readFromDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.log("Loaded data.");

        scheduleDataSaver();
        Utils.log("Scheduled data saving.");

        // Prevent checking for players who haven't joined since this plugin was updated
        for (DataPlayer player : playerHandler.getJoinedPlayers()) {
            if (player.lastOnlineTime <= 0) {
                player.unclaimedAllChunks = true;
            }
        }
        int check = Config.getInt("chunks", "unclaimCheckIntervalTicks");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::handleAutoUnclaim, check, check);
        Utils.log("Scheduled unclaimed chunk checker.");

        Utils.log("Initialization complete.");
    }

    private void handleAutoUnclaim() {
        int length = Config.getInt("chunks", "automaticUnclaimSeconds");
        // Less than will disable the check
        if (length < 1) return;

        long time = System.currentTimeMillis();
        for (Player player : getServer().getOnlinePlayers()) {
            playerHandler.getPlayer(player.getUniqueId()).lastOnlineTime = time;
            Utils.log("Time: %s", time);
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

    public void onDisable() {
        try {
            chunkHandler.writeToDisk();
            playerHandler.writeToDisk();
            Utils.log("Saved data.");
        } catch (Exception e) {
            e.printStackTrace();
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
            chunkHandler.writeToDisk();
            playerHandler.writeToDisk();

            chunkHandler.readFromDisk();
            playerHandler.readFromDisk();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("Couldn't reload data: \"%s\"", e.getMessage());
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
