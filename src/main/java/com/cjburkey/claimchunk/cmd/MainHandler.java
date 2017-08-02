package com.cjburkey.claimchunk.cmd;

import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;

public final class MainHandler {
	
	public static void claimChunk(Player p) {
		if (!Utils.hasPerm(p, "claimchunk.claim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("claimNoPerm"));
			return;
		}
		ChunkHandler ch = ClaimChunk.getInstance().getChunks();
		Chunk loc = p.getLocation().getChunk();
		if (ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("claimAlreadyOwned"));
			return;
		}
		if (!ClaimChunk.getInstance().getChunks().hasChunk(p.getUniqueId()) && Config.getBool("economy", "firstFree")) {
			if (ClaimChunk.getInstance().useEconomy()) {
				Econ e = ClaimChunk.getInstance().getEconomy();
				double cost = Config.getDouble("economy", "claimPrice");
				if (cost > 0) {
					Utils.log(e.getMoney(p.getUniqueId()) + " - " + cost);
					if (!e.buy(p.getUniqueId(), cost)) {
						Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("claimNotEnoughMoney"));
						return;
					}
				}
			}
		} else {
			Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getMsg("claimFree"));
		}
		int max = Config.getInt("chunks", "maxChunksClaimed");
		if (max > 0) {
			if (ch.getClaimed(p.getUniqueId()) > max) {
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("claimTooMany"));
				return;
			}
		}
		ChunkPos pos = ch.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p);
		if (pos != null && Config.getBool("chunks", "particlesWhenClaiming")) {
			pos.outlineChunk(p, 3);
		}
		Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getMsg("claimSuccess"));
	}
	
	public static void unclaimChunk(Player p) {
		if (!Utils.hasPerm(p, "claimchunk.unclaim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("unclaimNoPerm"));
			return;
		}
		ChunkHandler ch = ClaimChunk.getInstance().getChunks();
		Chunk loc = p.getLocation().getChunk();
		if(!ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("unclaimNotOwned"));
			return;
		}
		if(!ch.isOwner(loc.getWorld(), loc.getX(), loc.getZ(), p)) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("unclaimNotOwner"));
			return;
		}
		boolean refund = false;
		if (ClaimChunk.getInstance().useEconomy()) {
			Econ e = ClaimChunk.getInstance().getEconomy();
			double reward = Config.getDouble("economy", "unclaimReward");
			if (reward > 0) {
				e.addMoney(p.getUniqueId(), reward);
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("unclaimRefund").replaceAll("%%AMT%%", e.format(reward)));
				refund = true;
			}
		}
		ch.unclaimChunk(loc.getWorld(), loc.getX(), loc.getZ());
		if (!refund) {
			Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getMsg("unclaimSuccess"));
		}
	}
	
	public static void accessChunk(Player p, String[] args) {
		if (!Utils.hasPerm(p, "claimchunk.claim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("accessNoPerm"));
			return;
		}
		@SuppressWarnings("deprecation")
		Player other = ClaimChunk.getInstance().getServer().getPlayer(args[0]);
		if (other != null) {
			toggle(p, other.getUniqueId(), other.getName());
			return;
		} else {
			UUID otherId = ClaimChunk.getInstance().getPlayers().getUuid(args[0]);
			if (otherId == null) {
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getMsg("accessNoPlayer"));
				return;
			}
			toggle(p, otherId, args[0]);
		}
	}
	
	private static void toggle(Player owner, UUID other, String otherName) {
		if (owner.getUniqueId().equals(other)) {
			Utils.toPlayer(owner, Utils.getConfigColor("errorColor"), Utils.getMsg("accessOneself"));
			return;
		}
		boolean hasAccess = ClaimChunk.getInstance().getAccess().toggleAccess(owner.getUniqueId(), other);
		if (hasAccess) {
			Utils.toPlayer(owner, Utils.getConfigColor("successColor"), Utils.getMsg("accessHas").replaceAll("%%PLAYER%%", otherName));
			return;
		}
		Utils.toPlayer(owner, Utils.getConfigColor("successColor"), Utils.getMsg("accessNoLongerHas").replaceAll("%%PLAYER%%", otherName));
	}
	
}
