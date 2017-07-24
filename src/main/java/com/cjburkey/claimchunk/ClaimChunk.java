package com.cjburkey.claimchunk;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.cjburkey.claimchunk.chunk.AccessHandler;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.cmd.CmdClaimChunk;
import com.cjburkey.claimchunk.cmd.CmdUnclaimChunk;
import com.cjburkey.claimchunk.event.CancellableChunkEvents;
import com.cjburkey.claimchunk.event.PlayerJoinHandler;
import net.milkbowl.vault.permission.Permission;

public final class ClaimChunk extends JavaPlugin {
	
	private static ClaimChunk instance;
	
	private File dataFile;
	private File plyFile;
	private File accessFile;
	
	private Econ economy;
	private Permission perms;
	private Cacher cacher;
	private ChunkHandler chunkHandler;
	private AccessHandler accessHandler;
	
	public void onEnable() {
		instance = this;
		dataFile = new File(getDataFolder(), "/data/claimed.chks");
		plyFile = new File(getDataFolder(), "/data/playerCache.dat");
		accessFile = new File(getDataFolder(), "/data/grantedAccess.dat");
		economy = new Econ();
		cacher = new Cacher();
		chunkHandler = new ChunkHandler();
		accessHandler = new AccessHandler();
		
		if (!economy.setupEconomy(this)) {
			Utils.err("Economy could not be setup. Make sure that you have an economy plugin (like Essentials) installed. ClaimChunk has been disabled.");
			disable();
			return;
		}
		Utils.log("Economy set up.");
		
		setupConfig();
		Utils.log("Config set up.");
		
		if (!setupPermissions()) {
			Utils.err("Permissions could not be initialized. ClaimChunk has been disabled.");
			disable();
			return;
		}
		Utils.log("Permissions set up.");
		
		setupCommands();
		Utils.log("Commands set up.");
		
		setupEvents();
		Utils.log("Events set up.");
		
		try {
			chunkHandler.readFromDisk(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Utils.log("Chunks set up.");
		
		Utils.log("Initialization complete.");
	}
	
	public boolean canEdit(int x, int z, UUID player) {
		if (!chunkHandler.isClaimed(x, z)) {
			return true;
		}
		if (chunkHandler.isOwner(x, z, player)) {
			return true;
		}
		if (accessHandler.hasAccess(chunkHandler.getOwner(x, z), player)) {
			return true;
		}
		return false;
	}
	
	public void cancelEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
		if (!e.isCancelled()) {
			if (!canEdit(chunk.getX(), chunk.getZ(), ply.getUniqueId())) {
				e.setCancelled(true);
				Utils.toPlayer(ply, ChatColor.RED, Utils.getLang("CannotEditThisChunk"));
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
	}
	
	private void setupCommands() {
		getCommand("claimchunk").setExecutor(new CmdClaimChunk());
		getCommand("unclaimchunk").setExecutor(new CmdUnclaimChunk());
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}
	
	public void onDisable() {
		Utils.log("Finished disable.");
	}
	
	private void disable() {
		getServer().getPluginManager().disablePlugin(this);
	}
	
	public Econ getEconomy() {
		return economy;
	}
	
	public Permission getPermission() {
		return perms;
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
	
	public static ClaimChunk getInstance() {
		return instance;
	}
	
}