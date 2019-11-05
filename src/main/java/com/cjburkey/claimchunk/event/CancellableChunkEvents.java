package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ChunkEventHelper;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
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
        if (e != null) ChunkEventHelper.handleBlockEvent(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    // Clicking on Blocks/Crop trampling
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e != null
                && e.getClickedBlock() != null
                && e.getAction() != Action.LEFT_CLICK_BLOCK
                && e.getAction() != Action.LEFT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_AIR) {
            ChunkEventHelper.handleInteractionEvent(e.getPlayer(), e.getClickedBlock().getChunk(), e);
        }
    }

    // Placing Blocks
    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e) {
        if (e != null) {
            ChunkEventHelper.handleBlockEvent(e.getPlayer(), e.getBlock().getChunk(), e);
        }
    }

    // Item Frame Rotation
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e == null) return;

        final EntityType ENTITY = e.getRightClicked().getType();
        if (ENTITY == EntityType.ITEM_FRAME || ENTITY == EntityType.PAINTING) {
            ChunkEventHelper.handleInteractionEvent(e.getPlayer(), e.getRightClicked().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Break
    @EventHandler
    public void onItemFrameBroken(HangingBreakByEntityEvent e) {
        if (e != null && e.getRemover() != null && e.getRemover().getType() == EntityType.PLAYER) {
            ChunkEventHelper.handleBlockEvent((Player) e.getRemover(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Item Frame/Painting Place
    @EventHandler
    public void onItemFramePlaced(HangingPlaceEvent e) {
        if (e != null && e.getPlayer() != null) {
            ChunkEventHelper.handleBlockEvent(e.getPlayer(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Explosions
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e != null) ChunkEventHelper.handleExplosionIfConfig(e);
    }

    // Player/Animal damage
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e != null) {
            ChunkEventHelper.handleEntityDamageEvent(e);
        }
    }

    // Liquid place
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketEmptyEvent e) {
        if (e != null) {
            BlockFace bf = e.getBlockFace();
            Vector v = new Vector(bf.getModX(), bf.getModY(), bf.getModZ());
            ChunkEventHelper.handleBlockEvent(e.getPlayer(), e.getBlockClicked().getLocation().add(v).getChunk(), e);
        }
    }

    // Liquid pickup
    @EventHandler
    public void onLiquidPlacePickup(PlayerBucketFillEvent e) {
        if (e != null) {
            BlockFace bf = e.getBlockFace();
            Vector v = new Vector(bf.getModX(), bf.getModY(), bf.getModZ());
            ChunkEventHelper.handleBlockEvent(e.getPlayer(), e.getBlockClicked().getLocation().add(v).getChunk(), e);
        }
    }

    // Lead creation
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent e) {
        if (e != null) {
            ChunkEventHelper.handleEntityEvent(e.getPlayer(), e.getEntity(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Lead destruction
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent e) {
        if (e != null) {
            ChunkEventHelper.handleEntityEvent(e.getPlayer(), e.getEntity(), e.getEntity().getLocation().getChunk(), e);
        }
    }

    // Commands
    @EventHandler
    public void onCommandRun(PlayerCommandPreprocessEvent e) {
        if (e != null) {
            ChunkEventHelper.handleCommandEvent(e.getPlayer(), e.getPlayer().getLocation().getChunk(), e);
        }
    }

    // Fire spreading
    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        if (e != null) {
            ChunkEventHelper.handleSpreadEvent(e);
        }
    }

    // Fluids spreading
    @EventHandler
    public void onFireSpread(BlockFromToEvent e) {
        if (e != null) {
            ChunkEventHelper.handleToFromEvent(e);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e != null) {
            ChunkEventHelper.handlePistonExtendEvent(e);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent e) {
        if (e != null) {
            ChunkEventHelper.handlePistonRetractEvent(e);
        }
    }

}
