package com.cjburkey.claimchunk;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.cjburkey.claimchunk.cmd.CmdClaimChunk;
import net.milkbowl.vault.permission.Permission;

public final class ClaimChunk extends JavaPlugin {
	
	private static ClaimChunk instance;
	
	private Econ economy;
	private Permission perms;
	
	public void onEnable() {
		instance = this;
		economy = new Econ();
		
		if (!economy.setupEconomy(this)) {
			Utils.err("Economy could not be setup. Do you have an economy plugin other than Vault installed? ClaimChunk has been disabled.");
			disable();
			return;
		}
		
		Utils.log("Economy set up.");
		
		if (!setupPermissions()) {
			Utils.err("Permissions could not be initialized. ClaimChunk has been disabled.");
			disable();
			return;
		}
		
		Utils.log("Permissions set up.");
		
		setupCommands();
		Utils.log("Commands set up.");
		
		Utils.log("Initialization complete.");
	}
	
	private void setupCommands() {
		getCommand("claimchunk").setExecutor(new CmdClaimChunk());
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
	
	public static ClaimChunk getInstance() {
		return instance;
	}
	
}