package com.cjburkey.claimchunk.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;

@SuppressWarnings("unused")
@Deprecated
public class CancellableChunkEvents implements Listener {

    // Block Break
    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {
    }

    // Placing Blocks
    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e) {
    }

    // Clicking on Blocks/Crop trampling
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
    }

    // Item Frame Rotation
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
    }

    // Item Frame/Painting Break
    @EventHandler
    public void onItemFrameBroken(HangingBreakByEntityEvent e) {
    }

    // Item Frame/Painting Place
    @EventHandler
    public void onItemFramePlaced(HangingPlaceEvent e) {
    }

    // Explosions
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
    }
    // Player/Animal damage
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
    }

    // Liquid place
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketEmptyEvent e) {
    }

    // Liquid pickup
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketFillEvent e) {
    }

    // Lead creation
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent e) {
    }

    // Lead destruction
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent e) {
    }

    // Commands
    @EventHandler
    public void onCommandRun(PlayerCommandPreprocessEvent e) {
    }

    // Fire spreading
    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
    }

    // Fluids spreading
    @EventHandler
    public void onFireSpread(BlockFromToEvent e) {
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
    }

    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent e) {
    }

}
