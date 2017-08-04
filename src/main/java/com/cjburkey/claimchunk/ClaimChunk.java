package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.cmd.CommandHandler;
import com.cjburkey.claimchunk.cmd.Commands;
import com.cjburkey.claimchunk.data.DataConversion;
import com.cjburkey.claimchunk.dynmap.ClaimChunkDynmap;
import com.cjburkey.claimchunk.event.CancellableChunkEvents;
import com.cjburkey.claimchunk.event.PlayerConnectionHandler;
import com.cjburkey.claimchunk.event.PlayerMovementHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;
import com.cjburkey.claimchunk.tab.AutoTabCompletion;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ClaimChunk extends JavaPlugin {
	
	private static ClaimChunk instance;
	
	private boolean useEcon = false;
	private boolean useDynmap = false;
	
	//private File dataFile;
	//private File accessFile;
	//private File namesFile;
	private File chunkFile;
	private File plyFile;
	
	private CommandHandler cmd;
	private Commands cmds;
	private Econ economy;
	private ClaimChunkDynmap map;
	private ChunkHandler chunkHandler;
	private PlayerHandler playerHandler;
	
	public void onLoad() {
		instance = this;
	}
	
	public void onEnable() {
		//dataFile = new File(getDataFolder(), "/data/claimed.chks");
		//plyFile = new File(getDataFolder(), "/data/playerCache.dat");
		//accessFile = new File(getDataFolder(), "/data/grantedAccess.dat");
		chunkFile = new File(getDataFolder(), "/data/claimedChunks.json");
		plyFile = new File(getDataFolder(), "/data/playerData.json");
		
		cmd = new CommandHandler();
		cmds = new Commands();
		economy = new Econ();
		map = new ClaimChunkDynmap();
		playerHandler = new PlayerHandler(plyFile);
		chunkHandler = new ChunkHandler(chunkFile);
		
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
		
		useEcon = ((getServer().getPluginManager().getPlugin("Vault") != null) && Config.getBool("economy", "useEconomy"));
		useDynmap = ((getServer().getPluginManager().getPlugin("dynmap") != null) && Config.getBool("dynmap", "useDynmap"));
		
		if (useEcon) {
			if (!economy.setupEconomy(this)) {
				Utils.err("Economy could not be setup. Make sure that you have an economy plugin (like Essentials) installed. ClaimChunk has been disabled.");
				disable();
				return;
			}
			Utils.log("Economy set up.");
			getServer().getScheduler().scheduleSyncDelayedTask(this, () -> Utils.log("Money Format: " + economy.format(99132.76)), 0l);		// Once everything is loaded.
		} else {
			Utils.log("Economy not enabled. Either it was disabled with config or Vault was not found.");
		}
		
		if (useDynmap) {
			if (!map.registerAndSuch()) {
				Utils.log("There was an error while enabling Dynmap support.");
				disable();
				return;
			} else {
				Utils.log("Dynmap support enabled.");
			}
		} else {
			Utils.log("Dynmap support not enabled. Either it was disabled with config or Dynmap was not found.");
		}
		
		setupCommands();
		Utils.log("Commands set up.");
		
		setupEvents();
		Utils.log("Events set up.");
		
		try {
			chunkHandler.readFromDisk();
			playerHandler.readFromDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.log("Loaded data.");
		
		scheduleDataSaver();
		Utils.log("Scheduled data saving.");
		
		Utils.log("Initialization complete.");
	}
	
	public void onDisable() {
		try {
			chunkHandler.writeToDisk();
			playerHandler.writeToDisk();
			Utils.log("Saved data.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.log("Finished disable.");
	}
	
	private void setupConfig() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}
	
	private void setupEvents() {
		getServer().getPluginManager().registerEvents(new PlayerConnectionHandler(), this);
		getServer().getPluginManager().registerEvents(new CancellableChunkEvents(), this);
		getServer().getPluginManager().registerEvents(new PlayerMovementHandler(), this);
	}
	
	private void setupCommands() {
		cmds.register(cmd);
		getCommand("chunk").setExecutor(cmd);
		getCommand("chunk").setTabCompleter(new AutoTabCompletion());
	}
	
	private void scheduleDataSaver() {
		// From minutes, calculate after how long in ticks to save data.
		int saveTimeTicks = Config.getInt("data", "saveDataInterval") * 60 * 20;
        // Async because lags when auto claim
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::reloadData, saveTimeTicks, saveTimeTicks);
    }

    private void reloadData() {
		try {
			chunkHandler.writeToDisk();
			playerHandler.writeToDisk();
			
			chunkHandler.readFromDisk();
			playerHandler.readFromDisk();
		} catch (IOException e) {
			e.printStackTrace();
			Utils.log("Couldn't reload data: \"" + e.getMessage() + "\"");
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
	
	public boolean useDynmap() {
		return useDynmap;
	}
	
	public static ClaimChunk getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		System.out.println("Please put this in your /plugins/ folder.");
	}
	
}