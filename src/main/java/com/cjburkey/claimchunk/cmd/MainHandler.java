package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class MainHandler {

    public static void claimChunk(Player p, Chunk loc) {
        // Check permissions
        if (!Utils.hasPerm(p, true, "claim")) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimNoPerm);
            return;
        }

        // Check if the chunk is already claimed
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        if (ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimAlreadyOwned);
            return;
        }

        // Check if players can claim chunks here/in this world
        boolean allowedToClaimWG = WorldGuardHandler.isAllowedClaim(loc);
        boolean worldAllowsClaims = !Config.getList("chunks", "disabledWorlds").contains(loc.getWorld().getName());
        boolean adminOverride = Config.getBool("worldguard", "allowAdminOverride");
        boolean hasAdmin = Utils.hasPerm(p, false, "admin");    // UH OH THIS WAS BROKEN SINCE 0.0.8!!!
        if (!(worldAllowsClaims || (hasAdmin && adminOverride)) || !(allowedToClaimWG || (hasAdmin && adminOverride))) {
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimLocationBlock);
            return;
        }

        // Check if economy should be used
        boolean useEcon = ClaimChunk.getInstance().useEconomy();
        boolean econFree = false;
        double finalCost = 0.0d;
        Econ e = null;
        if (useEcon) {
            if (!ch.getHasAllFreeChunks(p.getUniqueId())) {
                econFree = true;
            } else {
                e = ClaimChunk.getInstance().getEconomy();
                double cost = Config.getDouble("economy", "claimPrice");
                if (cost > 0 && !e.buy(p.getUniqueId(), cost)) {
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimNotEnoughMoney);
                    return;
                }
                finalCost = cost;
            }
        }

        // Check if the player has room for more chunk claims
        int max = ClaimChunk.getInstance().getRankHandler().getMaxClaimsForPlayer(p);
        Utils.debug("Player %s can claim %s chunks", p.getDisplayName(), max);
        if (max > 0) {
            if (ch.getClaimed(p.getUniqueId()) >= max) {
                Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().claimTooMany);
                return;
            }
        }

        // Claim the chunk if nothing is wrong
        ChunkPos pos = ch.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p.getUniqueId());
        if (pos != null && Config.getBool("chunks", "particlesWhenClaiming")) {
            pos.outlineChunk(p, 3);
        }
        String msg;
        if (econFree) {
            int freeCount = Config.getInt("economy", "firstFreeChunks");
            if (freeCount == 1) {
                msg = ClaimChunk.getInstance().getMessages().claimFree1;
            } else {
                msg = ClaimChunk.getInstance().getMessages().claimFrees.replaceAll(Pattern.quote("%%COUNT%%"), freeCount + "");
            }
        } else {
            msg = ClaimChunk.getInstance().getMessages().claimSuccess
                    .replace("%%PRICE%%", ((e == null || finalCost <= 0.0d) ? ClaimChunk.getInstance().getMessages().claimNoCost : e.format(finalCost)));
        }
        Utils.toPlayer(p, msg);
    }

    public static void toggleTnt(Player executor) {
        ChunkHandler handler = ClaimChunk.getInstance().getChunkHandler();
        Chunk chunk = executor.getLocation().getChunk();
        if (handler.isOwner(chunk, executor)) {
            Utils.toPlayer(executor, (handler.toggleTnt(chunk) ? ClaimChunk.getInstance().getMessages().tntEnabled : ClaimChunk.getInstance().getMessages().tntDisabled));
            return;
        }
        Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().tntNoPerm);
    }

    public static void unclaimChunk(boolean adminOverride, boolean raw, Player p) {
        Chunk chunk = p.getLocation().getChunk();
        unclaimChunk(adminOverride, raw, p, p.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static boolean unclaimChunk(boolean adminOverride, boolean hideTitle, Player p, String world, int x, int z) {
        try {
            // Check permissions
            if ((!adminOverride && !Utils.hasPerm(p, true, "unclaim"))
                    || (adminOverride && !Utils.hasPerm(p, false, "admin"))) {
                if (!hideTitle)
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimNoPerm);
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
                if (!hideTitle)
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimNotOwned);
                return false;
            }

            // Check if the unclaimer is the owner or admin override is enable
            if (!adminOverride && !ch.isOwner(w, x, z, p)) {
                if (!hideTitle)
                    Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimNotOwner);
                return false;
            }

            // Check if a refund is required
            boolean refund = false;

            if (!adminOverride && ClaimChunk.getInstance().useEconomy()
                    && ch.getClaimed(p.getUniqueId()) > Config.getInt("economy", "firstFreeChunks")) {
                Econ e = ClaimChunk.getInstance().getEconomy();
                double reward = Config.getDouble("economy", "unclaimReward");
                if (reward > 0) {
                    e.addMoney(p.getUniqueId(), reward);
                    if (!hideTitle) {
                        Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimRefund.replace("%%AMT%%", e.format(reward)));
                    }
                    refund = true;
                }
            }

            // Unclaim the chunk
            ch.unclaimChunk(w, x, z);
            if (!refund && !hideTitle) {
                Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().unclaimSuccess);
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
            Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().accessNoPerm);
            return;
        }

        @SuppressWarnings("deprecation")
        Player other = ClaimChunk.getInstance().getServer().getPlayer(player);
        if (other != null) {
            toggle(p, other.getUniqueId(), other.getName(), multiple);
        } else {
            UUID otherId = ClaimChunk.getInstance().getPlayerHandler().getUUID(player);
            if (otherId == null) {
                Utils.toPlayer(p, ClaimChunk.getInstance().getMessages().accessNoPlayer);
                return;
            }
            toggle(p, otherId, player, multiple);
        }
    }

    private static void toggle(Player owner, UUID other, String otherName, boolean multiple) {
        if (owner.getUniqueId().equals(other)) {
            Utils.toPlayer(owner, ClaimChunk.getInstance().getMessages().accessOneself);
            return;
        }
        boolean hasAccess = ClaimChunk.getInstance().getPlayerHandler().toggleAccess(owner.getUniqueId(), other);
        if (hasAccess) {
            Utils.toPlayer(owner,
                    (multiple ? ClaimChunk.getInstance().getMessages().accessToggleMultiple : ClaimChunk.getInstance().getMessages().accessHas).replace("%%PLAYER%%", otherName));
            return;
        }
        Utils.toPlayer(owner,
                (multiple ? ClaimChunk.getInstance().getMessages().accessToggleMultiple : ClaimChunk.getInstance().getMessages().accessNoLongerHas).replace("%%PLAYER%%", otherName));
    }

    public static void listAccessors(Player executor) {
        Utils.msg(executor, Config.infoColor() + "&l---[ ClaimChunk Access ] ---");
        boolean anyOthersHaveAccess = false;
        for (UUID player : ClaimChunk.getInstance().getPlayerHandler().getAccessPermitted(executor.getUniqueId())) {
            String name = ClaimChunk.getInstance().getPlayerHandler().getUsername(player);
            if (name != null) {
                Utils.msg(executor, Config.infoColor() + "  - " + name);
                anyOthersHaveAccess = true;
            }
        }
        if (!anyOthersHaveAccess) {
            Utils.msg(executor, Config.errorColor() + "  No other players have access to your chunks");
        }
    }

}
