package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public final class MainHandler {

    public static void claimChunk(Player p, Chunk loc) {
        if (Utils.lacksPerm(p, "claimchunk.claim")) {
            Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("claimNoPerm"));
            return;
        }
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        if (ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
            Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("claimAlreadyOwned"));
            return;
        }

        boolean useEcon = ClaimChunk.getInstance().useEconomy();
        boolean success = true;
        boolean econFree = false;

        if (useEcon) {
            if (ch.hasNoChunks(p.getUniqueId()) && Config.getBool("economy", "firstFree")) {
                econFree = true;
            } else {
                Econ e = ClaimChunk.getInstance().getEconomy();
                double cost = Config.getDouble("economy", "claimPrice");
                if (cost > 0) {
                    Utils.log("%s - %s", e.getMoney(p.getUniqueId()), cost);
                    if (!e.buy(p.getUniqueId(), cost)) {
                        Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("claimNotEnoughMoney"));
                        success = false;
                    }
                }
            }
        }

        if (success) {
            int max = Config.getInt("chunks", "maxChunksClaimed");
            if (max > 0) {
                if (ch.getClaimed(p.getUniqueId()) >= max) {
                    Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("claimTooMany"));
                    return;
                }
            }

            ChunkPos pos = ch.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p.getUniqueId());
            if (pos != null && Config.getBool("chunks", "particlesWhenClaiming")) {
                pos.outlineChunk(p, 3);
            }
            Utils.toPlayer(p, true, Config.getColor("successColor"), Utils.getMsg(econFree ? "claimFree" : "claimSuccess"));
        }
    }

    public static void unclaimChunk(Player p) {
        if (Utils.lacksPerm(p, "claimchunk.unclaim")) {
            Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("unclaimNoPerm"));
            return;
        }
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        Chunk loc = p.getLocation().getChunk();
        if (!ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
            Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("unclaimNotOwned"));
            return;
        }
        if (!ch.isOwner(loc.getWorld(), loc.getX(), loc.getZ(), p)) {
            Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("unclaimNotOwner"));
            return;
        }
        boolean refund = false;
        if (ClaimChunk.getInstance().useEconomy()) {
            Econ e = ClaimChunk.getInstance().getEconomy();
            double reward = Config.getDouble("economy", "unclaimReward");
            if (reward > 0) {
                e.addMoney(p.getUniqueId(), reward);
                Utils.toPlayer(p, true, Config.getColor("errorColor"),
                        Utils.getMsg("unclaimRefund").replace("%%AMT%%", e.format(reward)));
                refund = true;
            }
        }
        ch.unclaimChunk(loc.getWorld(), loc.getX(), loc.getZ());
        if (!refund) {
            Utils.toPlayer(p, true, Config.getColor("successColor"), Utils.getMsg("unclaimSuccess"));
        }
    }

    public static void accessChunk(Player p, String[] players) {
        for (String player : players) accessChunk(p, player, players.length > 1);
    }

    private static void accessChunk(Player p, String player, boolean multiple) {
        if (Utils.lacksPerm(p, "claimchunk.claim")) {
            Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("accessNoPerm"));
            return;
        }
        Player other = ClaimChunk.getInstance().getServer().getPlayer(player);
        if (other != null) {
            toggle(p, other.getUniqueId(), other.getName(), multiple);
        } else {
            UUID otherId = ClaimChunk.getInstance().getPlayerHandler().getUUID(player);
            if (otherId == null) {
                Utils.toPlayer(p, false, Config.getColor("errorColor"), Utils.getMsg("accessNoPlayer"));
                return;
            }
            toggle(p, otherId, player, multiple);
        }
    }

    private static void toggle(Player owner, UUID other, String otherName, boolean multiple) {
        if (owner.getUniqueId().equals(other)) {
            Utils.toPlayer(owner, false, Config.getColor("errorColor"), Utils.getMsg("accessOneself"));
            return;
        }
        boolean hasAccess = ClaimChunk.getInstance().getPlayerHandler().toggleAccess(owner.getUniqueId(), other);
        if (hasAccess) {
            Utils.toPlayer(owner, false, Config.getColor("successColor"),
                    Utils.getMsg(multiple ? "accessToggleMultiple" : "accessHas").replace("%%PLAYER%%", otherName));
            return;
        }
        Utils.toPlayer(owner, false, Config.getColor("successColor"),
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
        if (!anyOthersHaveAccess)
            Utils.msg(executor, Config.getColor("errorColor") + "  No other players have access to your chunks");
    }

}
