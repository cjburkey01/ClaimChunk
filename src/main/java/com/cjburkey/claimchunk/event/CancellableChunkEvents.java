package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ChunkHelper;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import java.util.Objects;
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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@SuppressWarnings("unused")
public class CancellableChunkEvents implements Listener {

    // Block Break
    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {
        if (e != null) ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Clicking on Blocks
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e != null && e.getClickedBlock() != null) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK
                    || e.getAction() == Action.LEFT_CLICK_AIR
                    || e.getAction() == Action.RIGHT_CLICK_AIR) {
                return;
            }
            ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getClickedBlock().getChunk(), e);
        }
    }

    // Placing Blocks
    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e) {
        if (e != null) ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Item Frame Rotation
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e != null &&
                (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME) || e.getRightClicked().getType().equals(EntityType.PAINTING))) {
            ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getRightClicked().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Break
    @EventHandler
    public void onItemFrameBroken(HangingBreakByEntityEvent e) {
        if (e != null && Objects.requireNonNull(e.getRemover()).getType().equals(EntityType.PLAYER)) {
            ChunkHelper.cancelEventIfNotOwned((Player) e.getRemover(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Place
    @EventHandler
    public void onItemFramePlaced(HangingPlaceEvent e) {
        if (e != null && e.getPlayer() != null) {
            ChunkHelper.cancelEventIfNotOwned(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Remove/Delete
    @EventHandler
    public void onItemFramePlaced(EntityDamageByEntityEvent e) {
        if (e != null && (e.getEntity().getType().equals(EntityType.ITEM_FRAME) || e.getEntity().getType().equals(EntityType.PAINTING))
                && e.getDamager().getType().equals(EntityType.PLAYER)) {
            ChunkHelper.cancelEventIfNotOwned((Player) e.getDamager(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // TnT and Creeper explosions
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!e.isCancelled()) {
            if (ClaimChunk.getInstance().getChunkHandler().isUnclaimed(e.getLocation().getChunk())
                    && Config.getBool("protection", "blockUnclaimedChunks")) {
                return;
            }
            ChunkHelper.cancelExplosionIfConfig(e);
        }
    }

    // Animal damage
    @EventHandler()
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (ClaimChunk.getInstance().getChunkHandler().isUnclaimed(e.getEntity().getLocation().getChunk())) {
            return;
        }
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Animals)
            ChunkHelper.cancelAnimalDamage((Player) e.getDamager(), e.getDamager().getLocation().getChunk(), e);
    }

}
