package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfile;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

// TODO: BLOCK EXPLOSIONS, PISTON EXTENSIONS & RETRACTIONS, FIRE SPREAD

public class WorldProfileEventHandler implements Listener {

    private final ClaimChunk claimChunk;

    public WorldProfileEventHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    // -- EVENTS -- //

    /**
     * Event handler for when a player right clicks on an entity.
     */
    @EventHandler
    public void onEntityInteraction(PlayerInteractEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the player can interact with this entity
            onEntityEvent(() -> event.setCancelled(true),
                          event.getPlayer(),
                          event.getRightClicked(),
                          ClaimChunkWorldProfile.EntityAccessType.INTERACT);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the entity is a player
            Player player = unwrapPlayer(event.getDamager());

            // If the action isn't being performed by a player, we don't
            // particularly care.
            if (player != null) {
                // Check if the player can damage this entity
                onEntityEvent(() -> event.setCancelled(true),
                              player,
                              event.getEntity(),
                              ClaimChunkWorldProfile.EntityAccessType.DAMAGE);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the player can break this block
            onBlockEvent(() -> event.setCancelled(true),
                         event.getPlayer(),
                         event.getBlock(),
                         ClaimChunkWorldProfile.BlockAccessType.BREAK);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null || event.isCancelled()) return;

        // TODO: PREVENT CHEST CONNECTIONS ACROSS CHUNK BOUNDARIES WITH
        //       DIFFERENT OWNERS

        // Check if the player can place this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlock(),
                     ClaimChunkWorldProfile.BlockAccessType.PLACE);
    }

    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event != null
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType() != Material.AIR
                && event.useInteractedBlock() != Event.Result.DENY) {
            // Check if the player can interact with this block
            onBlockEvent(() -> event.setUseInteractedBlock(Event.Result.DENY),
                         event.getPlayer(),
                         event.getClickedBlock(),
                         ClaimChunkWorldProfile.BlockAccessType.INTERACT);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the entity is a player
            Player player = unwrapPlayer(event.getRemover());

            // If the action isn't being performed by a player, we don't
            // particularly care.
            if (player != null) {
                // Check if the player can damage this entity
                onEntityEvent(() -> event.setCancelled(true),
                              player,
                              event.getEntity(),
                              ClaimChunkWorldProfile.EntityAccessType.DAMAGE);
            }
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event != null && !event.isCancelled() && event.getPlayer() != null) {
            // Check if the player can interact with this entity (closest to "placing" an item frame)
            onEntityEvent(() -> event.setCancelled(true),
                          event.getPlayer(),
                          event.getEntity(),
                          ClaimChunkWorldProfile.EntityAccessType.INTERACT
            );
        }
    }

    // Explosions
    @EventHandler
    public void onEntityDamagedByExplosion(EntityDamageEvent event) {
        if (event != null
                && !event.isCancelled()
                && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                    || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            // Get the information for the current chunk
            final Chunk chunk = event.getEntity().getLocation().getChunk();
            final boolean isClaimed = claimChunk.getChunkHandler().isClaimed(chunk);

            // Get the profile for this world
            ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(chunk.getWorld().getName());

            // Get the access information
            final ClaimChunkWorldProfile.EntityAccess entityAccess
                    = profile.getEntityAccess(isClaimed, chunk.getWorld().getName(), event.getEntityType());

            // If explosions aren't allowed for this entity type, cancel the
            // damage.
            if (profile.enabled && !entityAccess.allowExplosion) {
                event.setCancelled(true);
            }
        }
    }

    // Liquid pickup
    @EventHandler
    public void onLiquidPickup(PlayerBucketFillEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can break this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlockClicked().getRelative(event.getBlockFace()),
                     ClaimChunkWorldProfile.BlockAccessType.BREAK);
    }

    // Liquid place
    @EventHandler
    public void onLiquidPlace(PlayerBucketEmptyEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can place this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlockClicked().getRelative(event.getBlockFace()),
                     ClaimChunkWorldProfile.BlockAccessType.PLACE);
    }

    // Lead creation
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(() -> event.setCancelled(true),
                      event.getPlayer(),
                      event.getEntity(),
                      ClaimChunkWorldProfile.EntityAccessType.INTERACT);
    }

    // Lead destruction
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can damage this entity
        onEntityEvent(() -> event.setCancelled(true),
                      event.getPlayer(),
                      event.getEntity(),
                      ClaimChunkWorldProfile.EntityAccessType.DAMAGE);
    }

    // -- HELPER METHODS -- //

    private void onEntityEvent(@Nonnull Runnable cancel,
                               @Nonnull Player player,
                               @Nonnull Entity entity,
                               @Nonnull ClaimChunkWorldProfile.EntityAccessType accessType) {
        // Get necessary information
        final UUID ply = player.getUniqueId();
        final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(entity.getLocation().getChunk());
        final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess = isOwner || (chunkOwner != null && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(entity.getWorld().getName());

        // Delegate event cancellation to the world profile
        if (profile.enabled && !profile.canAccessEntity(chunkOwner != null, isOwnerOrAccess, entity, accessType)) {
            cancel.run();

            // Show correct message
            if (accessType == ClaimChunkWorldProfile.EntityAccessType.INTERACT) {
                Utils.toPlayer(player, claimChunk.getMessages().chunkCancelEntityInteract);
            } else if (accessType == ClaimChunkWorldProfile.EntityAccessType.DAMAGE) {
                Utils.toPlayer(player, claimChunk.getMessages().chunkCancelEntityDamage);
            }
        }
    }

    private void onBlockEvent(@Nonnull Runnable cancel,
                              @Nullable Player player,
                              @Nonnull Block block,
                              @Nonnull ClaimChunkWorldProfile.BlockAccessType accessType) {
        // Get necessary information
        final UUID ply = player != null ? player.getUniqueId() : null;
        final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(block.getChunk());
        final boolean isOwner = (chunkOwner != null && player != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess = isOwner || (chunkOwner != null && ply != null && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(block.getWorld().getName());

        // Delegate event cancellation to the world profile
        if (profile.enabled && !profile.canAccessBlock(chunkOwner != null, isOwnerOrAccess, block.getWorld().getName(), block.getType(), accessType)) {
            cancel.run();

            // Show correct message
            if (accessType == ClaimChunkWorldProfile.BlockAccessType.INTERACT) {
                Utils.toPlayer(player, claimChunk.getMessages().chunkCancelBlockInteract);
            } else if (accessType == ClaimChunkWorldProfile.BlockAccessType.BREAK) {
                Utils.toPlayer(player, claimChunk.getMessages().chunkCancelBlockBreak);
            } else if (accessType == ClaimChunkWorldProfile.BlockAccessType.PLACE) {
                Utils.toPlayer(player, claimChunk.getMessages().chunkCancelBlockPlace);
            }
        }
    }

    /**
     * Tries to get an instance of Player out of any entity.
     *
     * @param possiblePlayer The entity from which to attempt to extract an instance of Player.
     * @return The Player instance, or {@code null} if a player couldn't be extracted.
     */
    private static @Nullable Player unwrapPlayer(@Nullable Entity possiblePlayer) {
        // Null check for safety
        if (possiblePlayer == null) return null;

        // Player entity
        if (possiblePlayer instanceof Player) return (Player) possiblePlayer;

        // Player shot a projectile
        if (possiblePlayer instanceof Projectile && ((Projectile) possiblePlayer).getShooter() instanceof Player) {
            return (Player) ((Projectile) possiblePlayer).getShooter();
        }

        // Either unimplemented or no player retrievable
        return null;
    }

}
