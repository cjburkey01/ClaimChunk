package com.cjburkey.claimchunk.cmd;

import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;

public final class MainHandler {
	
	public static void claimChunk(Player p) {
		if (!Utils.hasPerm(p, "claimchunk.claim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NoPermToClaim"));
			return;
		}
		ChunkHandler ch = ClaimChunk.getInstance().getChunks();
		Chunk loc = p.getLocation().getChunk();
		if (ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("ChunkAlreadyOwned"));
			return;
		}
		if (ClaimChunk.getInstance().useEconomy()) {
			Econ e = ClaimChunk.getInstance().getEconomy();
			double cost = ClaimChunk.getInstance().getConfig().getDouble("claimPrice");
			if (cost > 0) {
				Utils.log(e.getMoney(p.getUniqueId()) + " - " + cost);
				if (!e.buy(p.getUniqueId(), cost)) {
					Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NotEnoughMoney"));
					return;
				}
			}
		}
		int max = ClaimChunk.getInstance().getConfig().getInt("maxChunksClaimed");
		if (max > 0) {
			if (ch.getClaimed(p.getUniqueId()) > max) {
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("MaxChunks"));
				return;
			}
		}
		ChunkPos pos = ch.claimChunk(loc.getWorld(), loc.getX(), loc.getZ(), p);
		if (pos != null && ClaimChunk.getInstance().getConfig().getBoolean("particlesWhenClaiming")) {
			pos.outlineChunk(p, 3);
		}
		Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getLang("ChunkClaimed"));
	}
	
	public static void unclaimChunk(Player p) {
		if (!Utils.hasPerm(p, "claimchunk.unclaim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NoPermToUnclaim"));
			return;
		}
		ChunkHandler ch = ClaimChunk.getInstance().getChunks();
		Chunk loc = p.getLocation().getChunk();
		if(!ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("ChunkAlreadyNotClaimed"));
			return;
		}
		if(!ch.isOwner(loc.getWorld(), loc.getX(), loc.getZ(), p)) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NotYourChunk"));
			return;
		}
		if (ClaimChunk.getInstance().useEconomy()) {
			Econ e = ClaimChunk.getInstance().getEconomy();
			double reward = ClaimChunk.getInstance().getConfig().getDouble("unclaimReward");
			if (reward > 0) {
				e.addMoney(p.getUniqueId(), reward);
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("UnclaimReward").replaceAll("%%AMT%%", e.format(reward)));
			}
		}
		ch.unclaimChunk(loc.getWorld(), loc.getX(), loc.getZ());
		Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getLang("ChunkUnclaimed"));
	}
	
	public static void accessChunk(Player p, String[] args) {
		if (!Utils.hasPerm(p, "claimchunk.claim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NoPermToAccess"));
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
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("PlayerNotFound"));
				return;
			}
			toggle(p, otherId, args[0]);
		}
	}
	
	private static void toggle(Player owner, UUID other, String otherName) {
		if (owner.getUniqueId().equals(other)) {
			Utils.toPlayer(owner, Utils.getConfigColor("errorColor"), Utils.getLang("NotYourself"));
			return;
		}
		boolean hasAccess = ClaimChunk.getInstance().getAccess().toggleAccess(owner.getUniqueId(), other);
		if (hasAccess) {
			Utils.toPlayer(owner, Utils.getConfigColor("successColor"), Utils.getLang("HasAccess").replaceAll("%%PLAYER%%", otherName));
			return;
		}
		Utils.toPlayer(owner, Utils.getConfigColor("successColor"), Utils.getLang("NoLongerHasAccess").replaceAll("%%PLAYER%%", otherName));
	}
	
}