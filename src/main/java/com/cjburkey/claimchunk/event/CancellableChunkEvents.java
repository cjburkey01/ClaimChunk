package com.cjburkey.claimchunk.event;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.cjburkey.claimchunk.ClaimChunk;

public class CancellableChunkEvents implements Listener {
	
	// Block Break
	@EventHandler
	public void onBlockBroken(BlockBreakEvent e) {
		if(e != null && e.getPlayer() != null && e.getBlock() != null) {
			ClaimChunk.getInstance().cancelEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
		}
	}
	
	// Clicking on Blocks
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
	
	// Item Frame Rotation
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		if(e != null && e.getPlayer() != null && e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
			ClaimChunk.getInstance().cancelEventIfNotOwned(e.getPlayer(), e.getRightClicked().getLocation().getChunk(), e);
		}
	}
	
	// Item Frame Break
	@EventHandler
	public void onItemFrameBroken(HangingBreakByEntityEvent e) {
		if(e != null && e.getEntity().getType().equals(EntityType.ITEM_FRAME) && e.getRemover().getType().equals(EntityType.PLAYER)) {
			ClaimChunk.getInstance().cancelEventIfNotOwned((Player) e.getRemover(), e.getEntity().getLocation().getChunk(), e);
		}
	}
	
	// Item Frame Place
	@EventHandler
	public void onItemFramePlaced(HangingPlaceEvent e) {
		if(e != null && e.getEntity().getType().equals(EntityType.ITEM_FRAME) && e.getPlayer() != null) {
			ClaimChunk.getInstance().cancelEventIfNotOwned(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
		}
	}
	
	// Item Frame Remove Item
	@EventHandler
	public void onItemFramePlaced(EntityDamageByEntityEvent e) {
		if(e != null && e.getEntity().getType().equals(EntityType.ITEM_FRAME) && e.getDamager().getType().equals(EntityType.PLAYER)) {
			ClaimChunk.getInstance().cancelEventIfNotOwned((Player) e.getDamager(), e.getEntity().getLocation().getChunk(), e);
		}
	}
	
}