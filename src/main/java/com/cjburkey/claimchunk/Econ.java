package com.cjburkey.claimchunk;

import java.text.NumberFormat;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public final class Econ {
	
	private Economy econ;
	
	public boolean setupEconomy(ClaimChunk instance) {
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
	
	public double getMoney(UUID ply) {
		OfflinePlayer op = getOfflinePlayer(ply);
		if (op != null && op.hasPlayedBefore()) {
			return econ.getBalance(op);
		}
		return -1.0d;
	}
	
	/**
	 * Take money from the player.
	 * @param ply Player purchasing.
	 * @param cost The cost of the purchase.
	 * @return Whether or not the transaction was successful.
	 */
	public boolean buy(UUID ply, double cost) {
		if (getMoney(ply) >= cost) {
			EconomyResponse r = takeMoney(ply, cost);
			if (r.type.equals(EconomyResponse.ResponseType.SUCCESS)) {
				return true;
			}
		}
		return false;
	}
	
	public void setMoney(UUID ply, double amt) {
		double current = getMoney(ply);
		double toAdd = amt - current;
		if (toAdd < 0) {
			takeMoney(ply, Math.abs(toAdd));
			return;
		}
		addMoney(ply, toAdd);
	}
	
	public EconomyResponse addMoney(UUID ply, double amt) {
		OfflinePlayer op = getOfflinePlayer(ply);
		if (op != null) {
			return econ.depositPlayer(op, Math.abs(amt));
		}
		return null;
	}
	
	public EconomyResponse takeMoney(UUID ply, double amt) {
		OfflinePlayer op = getOfflinePlayer(ply);
		if (op != null) {
			return econ.withdrawPlayer(op, Math.abs(amt));
		}
		return null;
	}
	
	public String format(double amt) {
		return NumberFormat.getCurrencyInstance().format(amt);
	}
	
	private OfflinePlayer getOfflinePlayer(UUID id) {
		return Bukkit.getOfflinePlayer(id);
	}
	
	public Economy getEconomy() {
		return econ;
	}
	
}