package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ChunkEventHelper;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import java.util.Objects;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("unused")
public class CancellableChunkEvents implements Listener {

    // Block Break
    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {
        if (e != null) ChunkEventHelper.cancelBlockEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Clicking on Blocks/Crop trampling
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e != null
                && e.hasBlock()
                && e.getClickedBlock() != null
                && e.getAction() != Action.LEFT_CLICK_BLOCK
                && e.getAction() != Action.LEFT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_AIR) {
            ChunkEventHelper.cancelInteractionEventIfNotOwned(e.getPlayer(), e.getClickedBlock().getChunk(), e);
        }
    }

    // Placing Blocks
    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e) {
        if (e != null && !e.isCancelled())
            ChunkEventHelper.cancelBlockEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Item Frame Rotation
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e != null
                && (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME) || e.getRightClicked().getType().equals(EntityType.PAINTING))) {
            ChunkEventHelper.cancelInteractionEventIfNotOwned(e.getPlayer(), e.getRightClicked().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Break
    @EventHandler
    public void onItemFrameBroken(HangingBreakByEntityEvent e) {
        if (e != null && Objects.requireNonNull(e.getRemover()).getType().equals(EntityType.PLAYER)) {
            ChunkEventHelper.cancelBlockEventIfNotOwned((Player) e.getRemover(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Place
    @EventHandler
    public void onItemFramePlaced(HangingPlaceEvent e) {
        if (e != null && e.getPlayer() != null) {
            ChunkEventHelper.cancelBlockEventIfNotOwned(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Remove/Delete
    @EventHandler
    public void onItemFramePlaced(EntityDamageByEntityEvent e) {
        if (e != null
                && (e.getEntity().getType().equals(EntityType.ITEM_FRAME) || e.getEntity().getType().equals(EntityType.PAINTING))
                && e.getDamager().getType().equals(EntityType.PLAYER)) {
            ChunkEventHelper.cancelBlockEventIfNotOwned((Player) e.getDamager(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Explosions
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e != null) ChunkEventHelper.cancelExplosionIfConfig(e);
    }

    // Player/Animal damage
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e != null
                && !ClaimChunk.getInstance().getChunkHandler().isUnclaimed(e.getEntity().getLocation().getChunk())
                && !ClaimChunk.getInstance().getChunkHandler().isUnclaimed(e.getDamager().getLocation().getChunk())) {
            Player damager = (e.getDamager() instanceof Player) ? ((Player) e.getDamager()) :
                    ((e.getDamager() instanceof Projectile && (((Projectile) e.getDamager()).getShooter() instanceof Player)
                            ? ((Player) ((Projectile) e.getDamager()).getShooter())
                            : null));
            if (damager != null && isEntityProtected(e.getEntity())) {
                ChunkEventHelper.cancelAnimalEvent(damager, e.getEntity(), e.getDamager().getLocation().getChunk(), e);
            }
        }
    }

    // Liquid place
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketEmptyEvent e) {
        if (e != null) {
            BlockFace bf = e.getBlockFace();
            Vector v = new Vector(bf.getModX(), bf.getModY(), bf.getModZ());
            ChunkEventHelper.cancelBlockEventIfNotOwned(e.getPlayer(), e.getBlockClicked().getLocation().add(v).getChunk(), e);
        }
    }

    // Liquid pickup
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketFillEvent e) {
        if (e != null) {
            BlockFace bf = e.getBlockFace();
            Vector v = new Vector(bf.getModX(), bf.getModY(), bf.getModZ());
            ChunkEventHelper.cancelBlockEventIfNotOwned(e.getPlayer(), e.getBlockClicked().getLocation().add(v).getChunk(), e);
        }
    }

    // Lead creation
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent e) {
        if (e != null) {
            ChunkEventHelper.cancelAnimalEvent(e.getPlayer(), e.getEntity(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Lead destruction
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent e) {
        if (e != null) {
            ChunkEventHelper.cancelAnimalEvent(e.getPlayer(), e.getEntity(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Commands
    @EventHandler
    public void onCommandRun(PlayerCommandPreprocessEvent e) {
        if (e != null) {
            ChunkEventHelper.cancelCommandEvent(e.getPlayer(), e.getPlayer().getLocation().getChunk(), e);
        }
    }

    private boolean isEntityProtected(Entity entity) {
        return (entity instanceof Player && Config.getBool("protection", "blockPvp"))
                || entity instanceof Animals
                || entity instanceof ArmorStand
                || entity instanceof Vehicle
                || entity instanceof ItemFrame;
    }

}
