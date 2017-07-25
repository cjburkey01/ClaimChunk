package com.cjburkey.claimchunk.event;

import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;

public class PlayerMovementHandler implements Listener {
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e != null && e.getPlayer() != null && !e.isCancelled()) {
			Chunk prev = e.getFrom().getChunk();
			Chunk to = e.getTo().getChunk();
			ChunkHandler ch = ClaimChunk.getInstance().getChunks();
			boolean lastClaimed = ch.isClaimed(prev.getWorld(), prev.getX(), prev.getZ());
			if (ch.isClaimed(to.getWorld(), to.getX(), to.getZ())) {
				if (lastClaimed) {
					UUID prevOwner = ch.getOwner(prev.getWorld(), prev.getX(), prev.getZ());
					UUID newOwner = ch.getOwner(to.getWorld(), to.getX(), to.getZ());
					if (!prevOwner.equals(newOwner)) {
						showTitle(e.getPlayer(), to);
					}
				} else {
					showTitle(e.getPlayer(), to);
				}
			} else {
				if (lastClaimed) {
					Utils.toPlayer(e.getPlayer(), Utils.getConfigColor("infoColor"), Utils.getLang("ChunkLeave"));
				}
			}
		}
	}
	
	private void showTitle(Player player, Chunk newChunk) {
		UUID newOwner = ClaimChunk.getInstance().getChunks().getOwner(newChunk.getWorld(), newChunk.getX(), newChunk.getZ());
		if (!newOwner.equals(player.getUniqueId())) {
			String newName = ClaimChunk.getInstance().getPlayers().getName(newOwner);
			if (newName != null) {
				String text = Utils.getLang("ChunkOwner").replaceAll(Pattern.quote("%%PLAYER%%"), newName);
				Utils.toPlayer(player, Utils.getConfigColor("infoColor"), text);
			}
		} else {
			Utils.toPlayer(player, Utils.getConfigColor("infoColor"), Utils.getLang("ChunkSelf"));
		}
	}
	
}