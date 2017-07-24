package com.cjburkey.claimchunk;

import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

public final class Econ {
	
	private Economy econ;
	
	public boolean setupEconomy(ClaimChunk instance) {
		/*if (instance.getServer().getPluginManager().getPlugin("Vault") == null) {
			Utils.err("Vault not found in server.");
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			Utils.err("RegisteredServiceProvider is null.");
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;*/
		if (instance.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	public Economy getEconomy() {
		return econ;
	}
	
}