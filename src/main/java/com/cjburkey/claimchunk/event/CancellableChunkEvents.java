package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ChunkHelper;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import java.util.Objects;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("unused")
public class CancellableChunkEvents implements Listener {

    // Block Break
    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {
        if (e != null && !e.isCancelled()) ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Clicking on Blocks/Crop trampling
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e != null && !e.isCancelled() && e.hasBlock()) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK
                    || e.getAction() == Action.LEFT_CLICK_AIR
                    || e.getAction() == Action.RIGHT_CLICK_AIR) {
                return;
            }
            if (e.getClickedBlock() != null) {
                ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getClickedBlock().getChunk(), e);
            }
        }
    }

    // Placing Blocks
    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e) {
        if (e != null && !e.isCancelled()) ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Item Frame Rotation
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e != null
                && !e.isCancelled()
                && (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME) || e.getRightClicked().getType().equals(EntityType.PAINTING))) {
            ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getRightClicked().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Break
    @EventHandler
    public void onItemFrameBroken(HangingBreakByEntityEvent e) {
        if (e != null && !e.isCancelled() && Objects.requireNonNull(e.getRemover()).getType().equals(EntityType.PLAYER)) {
            ChunkHelper.cancelEventIfNotOwned((Player) e.getRemover(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Place
    @EventHandler
    public void onItemFramePlaced(HangingPlaceEvent e) {
        if (e != null && !e.isCancelled() && e.getPlayer() != null) {
            ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Remove/Delete
    @EventHandler
    public void onItemFramePlaced(EntityDamageByEntityEvent e) {
        if (e != null
                && !e.isCancelled()
                && (e.getEntity().getType().equals(EntityType.ITEM_FRAME) || e.getEntity().getType().equals(EntityType.PAINTING))
                && e.getDamager().getType().equals(EntityType.PLAYER)) {
            ChunkHelper.cancelEventIfNotOwned((Player) e.getDamager(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // TnT and Creeper explosions
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e == null || e.isCancelled()) return;
        if (ClaimChunk.getInstance().getChunkHandler().isUnclaimed(e.getLocation().getChunk())
                && Config.getBool("protection", "blockUnclaimedChunks")) {
            return;
        }
        ChunkHelper.cancelExplosionIfConfig(e);
    }

    // Player/Animal damage
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e == null || e.isCancelled() || ClaimChunk.getInstance().getChunkHandler().isUnclaimed(e.getEntity().getLocation().getChunk())) {
            return;
        }
        if ((e.getDamager() instanceof Player) &&
                (((e.getEntity() instanceof Player) && Config.getBool("protection", "blockPvp"))
                        || (e.getEntity() instanceof Animals))) {
            ChunkHelper.cancelEntityEvent((Player) e.getDamager(), e.getDamager().getLocation().getChunk(), e);
        }
    }

    // Liquid place
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketEmptyEvent e) {
        if (e == null || e.isCancelled()) return;
        BlockFace bf = e.getBlockFace();
        Vector v = new Vector(bf.getModX(), bf.getModY(), bf.getModZ());
        ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getBlockClicked().getLocation().add(v).getChunk(), e);
    }

    // Liquid pickup
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketFillEvent e) {
        if (e == null || e.isCancelled()) return;
        BlockFace bf = e.getBlockFace();
        Vector v = new Vector(bf.getModX(), bf.getModY(), bf.getModZ());
        ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getBlockClicked().getLocation().add(v).getChunk(), e);
    }

    // Lead creation
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent e) {
        if (e == null || e.isCancelled()) return;
        ChunkHelper.cancelEntityEvent(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
    }

    // Lead destruction
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent e) {
        if (e == null || e.isCancelled()) return;
        ChunkHelper.cancelEntityEvent(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
    }

}
