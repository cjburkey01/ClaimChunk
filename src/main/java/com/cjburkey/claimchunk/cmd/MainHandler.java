package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class MainHandler {

    public static void claimChunk(Player p, Chunk loc) {
        // Check permissions
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("claimNoPerm"));
            return;
        }

        // Check if the chunk is already claimed
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        if (ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
            Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("claimAlreadyOwned"));
            return;
        }

        // Check if players can claim chunks here/in this world
        boolean allowedToClaimWG = WorldGuardHandler.isAllowedClaim(loc);
        boolean worldAllowsClaims = !Config.getList("chunks", "disabledWorlds").contains(loc.getWorld().getName());
        boolean adminOverride = Config.getBool("worldguard", "allowAdminOverride");
        boolean hasAdmin = Utils.hasPerm(p, false, "admin");    // UH OH THIS WAS BROKEN SINCE 0.0.8!!!
        if (!(worldAllowsClaims || (hasAdmin && adminOverride)) || !(allowedToClaimWG || (hasAdmin && adminOverride))) {
            Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("claimLocationBlock"));
            return;
        }

        // Check if economy should be used
        boolean useEcon = ClaimChunk.getInstance().useEconomy();
        boolean econFree = false;
        double finalCost = 0.0d;
        Econ e = null;
        if (useEcon) {
            if (ch.hasNoChunks(p.getUniqueId()) && Config.getBool("economy", "firstFree")) {
                econFree = true;
            } else {
                e = ClaimChunk.getInstance().getEconomy();
                double cost = Config.getDouble("economy", "claimPrice");
                if (cost > 0 && !e.buy(p.getUniqueId(), cost)) {
                    Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("claimNotEnoughMoney"));
                    return;
                }
                finalCost = cost;
            }
        }

        // Check if the player has room for more chunk claims
        int max = ClaimChunk.getInstance().getRankHandler().getMaxClaimsForPlayer(p);
        if (max > 0) {
            if (ch.getClaimed(p.getUniqueId()) >= max) {
                Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("claimTooMany"));
                return;
            }
        }

        // Claim the chunk if nothing is wrong
        ChunkPos pos = ch.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p.getUniqueId());
        if (pos != null && Config.getBool("chunks", "particlesWhenClaiming")) {
            pos.outlineChunk(p, 3);
        }
        Utils.toPlayer(p, Config.getColor("successColor"), Utils.getMsg(econFree ? "claimFree" : "claimSuccess")
                .replace("%%PRICE%%", ((e == null || finalCost <= 0.0d) ? Utils.getMsg("claimNoCost") : e.format(finalCost))));
    }

    public static void unclaimChunk(boolean adminOverride, boolean raw, Player p) {
        Chunk chunk = p.getLocation().getChunk();
        unclaimChunk(adminOverride, raw, p, p.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static boolean unclaimChunk(boolean adminOverride, boolean raw, Player p, String world, int x, int z) {
        try {
            // Check permissions
            if (!adminOverride && !Utils.hasPerm(p, true, "unclaim")) {
                if (!raw) Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("unclaimNoPerm"));
                return false;
            }

            // Check if the chunk isn't claimed
            ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
            World w = Bukkit.getWorld(world);
            if (w == null) {
                Utils.err("Failed to locate world %s", world);
                return false;
            }
            if (!ch.isClaimed(w, x, z)) {
                if (!raw) Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("unclaimNotOwned"));
                return false;
            }

            // Check if the unclaimer is the owner or admin override is enable
            if (!adminOverride && !ch.isOwner(w, x, z, p)) {
                if (!raw) Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("unclaimNotOwner"));
                return false;
            }

            // Check if a refund is required
            boolean refund = false;
            if (!adminOverride && ClaimChunk.getInstance().useEconomy()
                    && (ch.getClaimed(p.getUniqueId()) > 1 || !Config.getBool("economy", "firstFree"))) {
                Econ e = ClaimChunk.getInstance().getEconomy();
                double reward = Config.getDouble("economy", "unclaimReward");
                if (reward > 0) {
                    e.addMoney(p.getUniqueId(), reward);
                    if (!raw) {
                        Utils.toPlayer(p, Config.getColor("errorColor"),
                                Utils.getMsg("unclaimRefund").replace("%%AMT%%", e.format(reward)));
                    }
                    refund = true;
                }
            }

            // Unclaim the chunk
            ch.unclaimChunk(w, x, z);
            if (!refund && !raw) {
                Utils.toPlayer(p, Config.getColor("successColor"), Utils.getMsg("unclaimSuccess"));
            }
            return true;
        } catch (Exception e) {
            Utils.err("Failed to unclaim chunk for player %s at %s,%s in %s", p.getDisplayName(), x, z, world);
            e.printStackTrace();
        }
        return false;
    }

    public static void accessChunk(Player p, String[] players) {
        for (String player : players) accessChunk(p, player, players.length > 1);
    }

    private static void accessChunk(Player p, String player, boolean multiple) {
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("accessNoPerm"));
            return;
        }
        Player other = ClaimChunk.getInstance().getServer().getPlayer(player);
        if (other != null) {
            toggle(p, other.getUniqueId(), other.getName(), multiple);
        } else {
            UUID otherId = ClaimChunk.getInstance().getPlayerHandler().getUUID(player);
            if (otherId == null) {
                Utils.toPlayer(p, Config.getColor("errorColor"), Utils.getMsg("accessNoPlayer"));
                return;
            }
            toggle(p, otherId, player, multiple);
        }
    }

    private static void toggle(Player owner, UUID other, String otherName, boolean multiple) {
        if (owner.getUniqueId().equals(other)) {
            Utils.toPlayer(owner, Config.getColor("errorColor"), Utils.getMsg("accessOneself"));
            return;
        }
        boolean hasAccess = ClaimChunk.getInstance().getPlayerHandler().toggleAccess(owner.getUniqueId(), other);
        if (hasAccess) {
            Utils.toPlayer(owner, Config.getColor("successColor"),
                    Utils.getMsg(multiple ? "accessToggleMultiple" : "accessHas").replace("%%PLAYER%%", otherName));
            return;
        }
        Utils.toPlayer(owner, Config.getColor("successColor"),
                Utils.getMsg(multiple ? "accessToggleMultiple" : "accessNoLongerHas").replace("%%PLAYER%%", otherName));
    }

    public static void listAccessors(Player executor) {
        Utils.msg(executor, Config.getColor("infoColor") + "&l---[ ClaimChunk Access ] ---");
        boolean anyOthersHaveAccess = false;
        for (UUID player : ClaimChunk.getInstance().getPlayerHandler().getAccessPermitted(executor.getUniqueId())) {
            String name = ClaimChunk.getInstance().getPlayerHandler().getUsername(player);
            if (name != null) {
                Utils.msg(executor, Config.getColor("infoColor") + "  - " + name);
                anyOthersHaveAccess = true;
            }
        }
        if (!anyOthersHaveAccess) {
            Utils.msg(executor, Config.getColor("errorColor") + "  No other players have access to your chunks");
        }
    }

}
