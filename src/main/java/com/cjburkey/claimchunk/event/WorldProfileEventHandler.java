package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Messages;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.config.ClaimChunkWorldProfile;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

// TODO: PISTON EXTENSIONS & RETRACTIONS, FIRE SPREAD
// TODO: CHECK IF PLAYER HAS TNT ENABLED

public class WorldProfileEventHandler implements Listener {

    private final ClaimChunk claimChunk;

    public WorldProfileEventHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    // -- EVENTS -- //

    // Entities

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

    /**
     * Event handler for when an entity is damaged by another entity (maybe a player)
     */
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

    // Blocks

    /**
     * Event handler for when a player breaks a block
     */
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

    /**
     * Event handler for when a player places a block
     */
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

    /**
     * Event handler for when a player right clicks on a block
     */
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

    // Hanging entities

    /**
     * Event handler for when things like item frames and paintings break
     */
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

    /**
     * Event handler for when things like item frames and paintings are placed
     */
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

    // Bucket usage

    /**
     * Event handler for when players pick up a liquid with a bucket
     */
    @EventHandler
    public void onLiquidPickup(PlayerBucketFillEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can break this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlockClicked().getRelative(event.getBlockFace()),
                     ClaimChunkWorldProfile.BlockAccessType.BREAK);
    }

    /**
     * Event handler for when players put down a liquid with a bucket
     */
    @EventHandler
    public void onLiquidPlace(PlayerBucketEmptyEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can place this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlockClicked().getRelative(event.getBlockFace()),
                     ClaimChunkWorldProfile.BlockAccessType.PLACE);
    }

    // Leads

    /**
     * Event handler for when players create a lead
     */
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(() -> event.setCancelled(true),
                      event.getPlayer(),
                      event.getEntity(),
                      ClaimChunkWorldProfile.EntityAccessType.INTERACT);
    }

    /**
     * Event handler for when players break a lead
     */
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can damage this entity
        onEntityEvent(() -> event.setCancelled(true),
                      event.getPlayer(),
                      event.getEntity(),
                      ClaimChunkWorldProfile.EntityAccessType.DAMAGE);
    }

    // Armor Stands

    /**
     * Event handler for when players manipulate (or do anything to, basically) armor stands
     */
    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(() -> event.setCancelled(true),
                event.getPlayer(),
                event.getRightClicked(),
                ClaimChunkWorldProfile.EntityAccessType.INTERACT);
    }

    // Explosion protection for entities

    /**
     * Event handler for when an entity is damaged by an explosion
     */
    @EventHandler
    public void onEntityDamagedByEntityExplosion(EntityDamageByEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the explosion can damage this entity
            onExplosionForEntityEvent(() -> event.setCancelled(true),
                                      event.getCause(),
                                      event.getEntity());
        }
    }

    /**
     * Event handler for when an entity is damaged by an explosion(again, bc to be safe? idk spigot can be weird and
     * it's better safe than sorry)
     */
    @EventHandler
    public void onEntityDamagedByExplosion(EntityDamageEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the explosion can damage this entity
            onExplosionForEntityEvent(() -> event.setCancelled(true),
                                      event.getCause(),
                                      event.getEntity());
        }
    }

    // Explosion protection for blocks from block and entity explosions

    /**
     * Event handler for when a block explodes
     */
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event != null && !event.isCancelled()) {
            onExplosionEvent(event.getBlock().getWorld(),
                             event.blockList());
        }
    }

    /**
     * Event handler for when an entity explodes
     */
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event != null && !event.isCancelled()) {
            onExplosionEvent(event.getEntity().getWorld(),
                             event.blockList());
        }
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

            // Get display name
            final String entityName = entity.getType().name();
            final String ownerName = chunkOwner != null
                    ? claimChunk.getPlayerHandler().getChunkName(chunkOwner)
                    : null;

            // Determine the correct message
            final Messages messages = claimChunk.getMessages();
            String msg = null;
            if (accessType == ClaimChunkWorldProfile.EntityAccessType.INTERACT) {
                if (chunkOwner == null) {
                    msg = messages.chunkCancelUnclaimedEntityInteract;
                } else {
                    msg = messages.chunkCancelClaimedEntityInteract;
                }
            } else if (accessType == ClaimChunkWorldProfile.EntityAccessType.DAMAGE) {
                if (chunkOwner == null) {
                    msg = messages.chunkCancelUnclaimedEntityDamage;
                } else {
                    msg = messages.chunkCancelClaimedEntityDamage;
                }
            }

            // Send the message
            if (msg == null) {
                Utils.err("Unknown message to send to player after entity event");
            } else {
                if (ownerName != null) {
                    msg = msg.replaceAll(Pattern.quote("%%OWNER%%"), ownerName);
                }
                Utils.toPlayer(player, msg.replaceAll(Pattern.quote("%%ENTITY%%"), entityName));
            }
        }
    }

    private void onBlockEvent(@Nonnull Runnable cancel,
                              @Nonnull Player player,
                              @Nonnull Block block,
                              @Nonnull ClaimChunkWorldProfile.BlockAccessType accessType) {
        // Get necessary information
        final UUID ply = player.getUniqueId();
        final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(block.getChunk());
        final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
        final boolean isOwnerOrAccess = isOwner || (chunkOwner != null && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(block.getWorld().getName());

        // Delegate event cancellation to the world profile
        if (profile.enabled && !profile.canAccessBlock(chunkOwner != null, isOwnerOrAccess, block.getWorld().getName(), block.getType(), accessType)) {
            cancel.run();

            // Get display name
            final String blockName = block.getType().name();
            final String ownerName = chunkOwner != null
                                        ? claimChunk.getPlayerHandler().getChunkName(chunkOwner)
                                        : null;

            // Determine the correct message
            final Messages messages = claimChunk.getMessages();
            String msg = null;
            if (accessType == ClaimChunkWorldProfile.BlockAccessType.INTERACT) {
                if (chunkOwner == null) {
                    msg = messages.chunkCancelUnclaimedBlockInteract;
                } else {
                    msg = messages.chunkCancelClaimedBlockInteract;
                }
            } else if (accessType == ClaimChunkWorldProfile.BlockAccessType.BREAK) {
                if (chunkOwner == null) {
                    msg = messages.chunkCancelUnclaimedBlockBreak;
                } else {
                    msg = messages.chunkCancelClaimedBlockBreak;
                }
            } else if (accessType == ClaimChunkWorldProfile.BlockAccessType.PLACE) {
                if (chunkOwner == null) {
                    msg = messages.chunkCancelUnclaimedBlockPlace;
                } else {
                    msg = messages.chunkCancelClaimedBlockPlace;
                }
            }

            // Send the message
            if (msg == null) {
                Utils.err("Unknown message to send to player after block event");
            } else {
                if (ownerName != null) {
                    msg = msg.replaceAll(Pattern.quote("%%OWNER%%"), ownerName);
                }
                Utils.toPlayer(player, msg.replaceAll(Pattern.quote("%%BLOCK%%"), blockName));
            }
        }
    }

    private void onExplosionForEntityEvent(@Nonnull Runnable cancel,
                                           @Nonnull EntityDamageEvent.DamageCause damageCause,
                                           @Nonnull Entity entity) {
        // Check if this is an explosion event
        if (damageCause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                && damageCause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        // Get this name for later usage
        final String worldName = entity.getWorld().getName();

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(worldName);

        // Delegate event cancellation to the world profile
        if (profile.enabled && !profile.getEntityAccess(claimChunk.getChunkHandler().isClaimed(entity.getLocation().getChunk()),
                                                        worldName,
                                                        entity.getType()).allowExplosion) {
            cancel.run();
        }
    }

    private void onExplosionEvent(@Nonnull World world,
                                  @Nonnull Collection<Block> blockList) {
        // Get chunk handler
        final ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        // Cache chunks to avoid so many look-ups through the chunk handler
        // The value is a boolean representing whether to cancel the event. `true` means the event will be cancelled
        final HashMap<Chunk, Boolean> cancelChunks = new HashMap<>();

        // Get the world name
        final String worldName = world.getName();

        // Get the world profile
        final ClaimChunkWorldProfile worldProfile = claimChunk.getProfileManager().getProfile(worldName);

        final ArrayList<Block> blocksCopy = new ArrayList<>(blockList);

        // Loop through all of the blocks
        for (Block block : blocksCopy) {
            // Get the chunk this block is in
            final Chunk chunk = block.getChunk();

            // Check if this type of block should be protected
            if (cancelChunks.computeIfAbsent(chunk, c ->
                    !worldProfile.getBlockAccess(chunkHandler.isClaimed(chunk),
                                                 worldName,
                                                 block.getType()).allowExplosion)) {
                // Try to remove the block from the explosion list
                if (!blockList.remove(block)) {
                    Utils.err("Failed to remove block of type \"%s\" at %s,%s,%s in world %s",
                              block.getType(),
                              block.getLocation().getBlockX(),
                              block.getLocation().getBlockY(),
                              block.getLocation().getBlockZ(),
                              block.getWorld().getName());
                }
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
