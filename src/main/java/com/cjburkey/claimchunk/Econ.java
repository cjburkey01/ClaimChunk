package com.cjburkey.claimchunk;

import java.text.NumberFormat;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class Econ {

    private Economy econ;
    private ClaimChunk instance;

    boolean setupEconomy(ClaimChunk instance) {
        this.instance = instance;

        // Check if Vault is present
        if (instance.getServer().getPluginManager().getPlugin("Vault") == null) return false;

        // Get the Vault service if it is present
        RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);

        // Check if the service is valid
        if (rsp == null) return false;

        // Update current economy handler
        econ = rsp.getProvider();

        // Success
        return true;
    }

    public double getMoney(UUID player) {
        Player ply = getPlayer(player);
        // If the player has joined the server before, return their balance
        if (ply != null) {
            return econ.getBalance(ply);
        }
        return -1.0d;
    }

    @SuppressWarnings("UnusedReturnValue")
    public EconomyResponse addMoney(UUID player, double amt) {
        Player ply = getPlayer(player);
        if (ply != null) {
            // Add the (safe) balance to the player
            return econ.depositPlayer(ply, Math.abs(amt));
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    private EconomyResponse takeMoney(UUID player, double amt) {
        Player ply = getPlayer(player);
        if (ply != null) {
            // Remove the money from the player's balance
            return econ.withdrawPlayer(ply, Math.abs(amt));
        }
        return null;
    }

    /**
     * Take money from the player.
     *
     * @param ply  Player purchasing.
     * @param cost The cost of the purchase.
     * @return Whether or not the transaction was successful.
     */
    public boolean buy(UUID ply, double cost) {
        if (getMoney(ply) >= cost) {
            EconomyResponse response = takeMoney(ply, cost);
            // Return whether the transaction was completed successfully
            return response != null && response.type == EconomyResponse.ResponseType.SUCCESS;
        }
        return false;
    }

    public String format(double amt) {
        return NumberFormat.getCurrencyInstance().format(amt);
    }

    private Player getPlayer(UUID id) {
        if (instance == null) {
            return null;
        }
        return instance.getServer().getPlayer(id);
    }

}
