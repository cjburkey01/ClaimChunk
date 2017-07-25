package com.cjburkey.claimchunk;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.java.JavaPlugin;
import com.cjburkey.claimchunk.chunk.AccessHandler;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.cmd.CmdAccessChunks;
import com.cjburkey.claimchunk.cmd.CmdClaimChunk;
import com.cjburkey.claimchunk.cmd.CmdUnclaimChunk;
import com.cjburkey.claimchunk.dynmap.ClaimChunkDynmap;
import com.cjburkey.claimchunk.event.CancellableChunkEvents;
import com.cjburkey.claimchunk.event.PlayerJoinHandler;
import com.cjburkey.claimchunk.event.PlayerMovementHandler;

public final class ClaimChunk extends JavaPlugin {
	
	private static ClaimChunk instance;
	
	private boolean useEcon = false;
	private boolean useDynmap = false;
	
	private File dataFile;
	private File plyFile;
	private File accessFile;
	
	private Econ economy;
	private ClaimChunkDynmap map;
	private Cacher cacher;
	private ChunkHandler chunkHandler;
	private AccessHandler accessHandler;
	
	public void onEnable() {
		instance = this;
		dataFile = new File(getDataFolder(), "/data/claimed.chks");
		plyFile = new File(getDataFolder(), "/data/playerCache.dat");
		accessFile = new File(getDataFolder(), "/data/grantedAccess.dat");
		economy = new Econ();
		map = new ClaimChunkDynmap();
		cacher = new Cacher();
		chunkHandler = new ChunkHandler();
		accessHandler = new AccessHandler();
		
		setupConfig();
		Utils.log("Config set up.");
		
		useEcon = ((getServer().getPluginManager().getPlugin("Vault") != null) && getConfig().getBoolean("useEconomy"));
		useDynmap = ((getServer().getPluginManager().getPlugin("dynmap") != null) && getConfig().getBoolean("useDynmap"));
		
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
			cacher.read(plyFile);
			accessHandler.read(accessFile);
			chunkHandler.readFromDisk(dataFile);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		Utils.log("Loaded data.");
		
		Utils.log("Initialization complete.");
	}
	
	public boolean canEdit(World world, int x, int z, UUID player) {
		if (!chunkHandler.isClaimed(world, x, z)) {
			return true;
		}
		if (chunkHandler.isOwner(world, x, z, player)) {
			return true;
		}
		if (accessHandler.hasAccess(chunkHandler.getOwner(world, x, z), player)) {
			return true;
		}
		return false;
	}
	
	public void cancelEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
		if (getConfig().getBoolean("blockInteractionInOtherPlayersChunks")) {
			if (!e.isCancelled()) {
				if (!canEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) {
					e.setCancelled(true);
					Utils.toPlayer(ply, Utils.getConfigColor("errorColor"), Utils.getLang("CannotEditThisChunk"));
				}
			}
		}
	}
	
	private void setupConfig() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}
	
	private void setupEvents() {
		getServer().getPluginManager().registerEvents(new PlayerJoinHandler(), this);
		getServer().getPluginManager().registerEvents(new CancellableChunkEvents(), this);
		getServer().getPluginManager().registerEvents(new PlayerMovementHandler(), this);
	}
	
	private void setupCommands() {
		getCommand("claimchunk").setExecutor(new CmdClaimChunk());
		getCommand("unclaimchunk").setExecutor(new CmdUnclaimChunk());
		getCommand("accesschunks").setExecutor(new CmdAccessChunks());
	}
	
	public void onDisable() {
		try {
			cacher.write(plyFile);
			accessHandler.write(accessFile);
			chunkHandler.writeToDisk(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.log("Finished disable.");
	}
	
	private void disable() {
		getServer().getPluginManager().disablePlugin(this);
	}
	
	public Econ getEconomy() {
		return economy;
	}
	
	public Cacher getPlayers() {
		return cacher;
	}
	
	public ChunkHandler getChunks() {
		return chunkHandler;
	}
	
	public AccessHandler getAccess() {
		return accessHandler;
	}
	
	public File getChunkFile() {
		return dataFile;
	}
	
	public File getPlyFile() {
		return plyFile;
	}
	
	public File getAccessFile() {
		return accessFile;
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
	
}