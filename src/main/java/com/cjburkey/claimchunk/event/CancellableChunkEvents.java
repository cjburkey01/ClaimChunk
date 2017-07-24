package com.cjburkey.claimchunk.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.cjburkey.claimchunk.ClaimChunk;

public class CancellableChunkEvents implements Listener {
	
	@EventHandler
	public void onBlockBroken(BlockBreakEvent e) {
		if(e != null && e.getPlayer() != null && e.getBlock() != null) {
			ClaimChunk.getInstance().cancelEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(e != null && e.getPlayer() != null && e.getClickedBlock() != null) {
			if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				return;
			}
			if (e.getAction() == Action.LEFT_CLICK_AIR) {
				return;
			}
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				return;
			}
			ClaimChunk.getInstance().cancelEventIfNotOwned(e.getPlayer(), e.getClickedBlock().getChunk(), e);
		}
	}
	
}