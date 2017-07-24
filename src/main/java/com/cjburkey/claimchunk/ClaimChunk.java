package com.cjburkey.claimchunk;

import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.cmd.CmdClaimChunk;
import com.cjburkey.claimchunk.cmd.CmdUnclaimChunk;
import net.milkbowl.vault.permission.Permission;

public final class ClaimChunk extends JavaPlugin {
	
	private static ClaimChunk instance;
	
	private File dataFile;
	
	private Econ economy;
	private Permission perms;
	private ChunkHandler chunkHandler;
	
	public void onEnable() {
		instance = this;
		dataFile = new File(getDataFolder(), "/data.chks");
		economy = new Econ();
		chunkHandler = new ChunkHandler();
		
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
		
		loadChunks();
		saveChunks();
		Utils.log("Chunks set up.");
		
		Utils.log("Initialization complete.");
	}
	
	private void setupConfig() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}
	
	public void saveChunks() {
		try {
			chunkHandler.writeToDisk(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadChunks() {
		try {
			chunkHandler.readFromDisk(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateChunks() {
		saveChunks();
		loadChunks();
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
	
	public ChunkHandler getChunks() {
		return chunkHandler;
	}
	
	public static ClaimChunk getInstance() {
		return instance;
	}
	
}