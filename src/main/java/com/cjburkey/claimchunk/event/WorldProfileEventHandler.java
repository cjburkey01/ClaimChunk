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
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

// TODO: CHECK IF PLAYER HAS TNT ENABLED
//       PREVENT CHEST CONNECTIONS ACROSS CHUNK BOUNDARIES WITH DIFFERENT OWNERS
public class WorldProfileEventHandler implements Listener {

    private final ClaimChunk claimChunk;

    public WorldProfileEventHandler(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    // -- EVENTS -- //

    // Entities

    /**
     * Event handler for when a player right clicks on an entity.
     *
     * TODO: TEST
     *  • Interact with entity in unclaimed chunk where world allows interaction in unclaimed chunk
     *  • Interact with entity in unclaimed chunk where world does not allow interaction in unclaimed chunk
     *  • Interact with entity in another player's claimed chunk (without access) where world does allow interaction in claimed chunks
     *  • Interact with entity in another player's claimed chunk (without access) where world does not allow interaction in claimed chunks
     *  • Interact with entity in another player's claimed chunk (with access) where world does allow interaction in claimed chunks
     *  • Interact with entity in another player's claimed chunk (with access) where world does not allow interaction in claimed chunks
     *  • Interact with entity in own claimed chunk where world does allow interaction in claimed chunks
     *  • Interact with entity in own claimed chunk where world does not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Damage entity in unclaimed chunk where world allows damaging in unclaimed chunk
     *  • Damage entity in unclaimed chunk where world does not allow damaging in unclaimed chunk
     *  • Damage entity in another player's claimed chunk (without access) where world does allow damaging in claimed chunks
     *  • Damage entity in another player's claimed chunk (without access) where world does not allow damaging in claimed chunks
     *  • Damage entity in another player's claimed chunk (with access) where world does allow damaging in claimed chunks
     *  • Damage entity in another player's claimed chunk (with access) where world does not allow damaging in claimed chunks
     *  • Damage entity in own claimed chunk where world does allow damaging in claimed chunks
     *  • Damage entity in own claimed chunk where world does not allow damaging in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Break block in unclaimed chunk where world allows breaking in unclaimed chunk
     *  • Break block in unclaimed chunk where world does not allow breaking in unclaimed chunk
     *  • Break block in another player's claimed chunk (without access) where world does allow breaking in claimed chunks
     *  • Break block in another player's claimed chunk (without access) where world does not allow breaking in claimed chunks
     *  • Break block in another player's claimed chunk (with access) where world does allow breaking in claimed chunks
     *  • Break block in another player's claimed chunk (with access) where world does not allow breaking in claimed chunks
     *  • Break block in own claimed chunk where world does allow breaking in claimed chunks
     *  • Break block in own claimed chunk where world does not allow breaking in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Place block in unclaimed chunk where world allows placing in unclaimed chunk
     *  • Place block in unclaimed chunk where world does not allow placing in unclaimed chunk
     *  • Place block in another player's claimed chunk (without access) where world does allow placing in claimed chunks
     *  • Place block in another player's claimed chunk (without access) where world does not allow placing in claimed chunks
     *  • Place block in another player's claimed chunk (with access) where world does allow placing in claimed chunks
     *  • Place block in another player's claimed chunk (with access) where world does not allow placing in claimed chunks
     *  • Place block in own claimed chunk where world does allow placing in claimed chunks
     *  • Place block in own claimed chunk where world does not allow placing in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can place this block
        onBlockEvent(() -> event.setCancelled(true),
                     event.getPlayer(),
                     event.getBlock(),
                     ClaimChunkWorldProfile.BlockAccessType.PLACE);
    }

    /**
     * Event handler for when a player right clicks on a block
     *
     * TODO: TEST
     *  • Interact with block in unclaimed chunk where world allows interaction in unclaimed chunk
     *  • Interact with block in unclaimed chunk where world does not allow interaction in unclaimed chunk
     *  • Interact with block in another player's claimed chunk (without access) where world does allow interaction in claimed chunks
     *  • Interact with block in another player's claimed chunk (without access) where world does not allow interaction in claimed chunks
     *  • Interact with block in another player's claimed chunk (with access) where world does allow interaction in claimed chunks
     *  • Interact with block in another player's claimed chunk (with access) where world does not allow interaction in claimed chunks
     *  • Interact with block in own claimed chunk where world does allow interaction in claimed chunks
     *  • Interact with block in own claimed chunk where world does not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event != null
                && event.getClickedBlock() != null
                && event.getAction() == Action.RIGHT_CLICK_BLOCK
                && (!event.isBlockInHand() || !event.getPlayer().isSneaking())
                && event.getClickedBlock().getType() != Material.AIR
                && event.useInteractedBlock() == Event.Result.ALLOW) {
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
     *
     * TODO: TEST
     *  • Break hanging entity in unclaimed chunk where world allows damaging in unclaimed chunk
     *  • Break hanging entity in unclaimed chunk where world does not allow damaging in unclaimed chunk
     *  • Break hanging entity in another player's claimed chunk (without access) where world does allow damaging in claimed chunks
     *  • Break hanging entity in another player's claimed chunk (without access) where world does not allow damaging in claimed chunks
     *  • Break hanging entity in another player's claimed chunk (with access) where world does allow damaging in claimed chunks
     *  • Break hanging entity in another player's claimed chunk (with access) where world does not allow damaging in claimed chunks
     *  • Break hanging entity in own claimed chunk where world does allow damaging in claimed chunks
     *  • Break hanging entity in own claimed chunk where world does not allow damaging in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Place hanging entity in unclaimed chunk where world allows interaction in unclaimed chunk
     *  • Place hanging entity in unclaimed chunk where world does not allow interaction in unclaimed chunk
     *  • Place hanging entity in another player's claimed chunk (without access) where world does allow interaction in claimed chunks
     *  • Place hanging entity in another player's claimed chunk (without access) where world does not allow interaction in claimed chunks
     *  • Place hanging entity in another player's claimed chunk (with access) where world does allow interaction in claimed chunks
     *  • Place hanging entity in another player's claimed chunk (with access) where world does not allow interaction in claimed chunks
     *  • Place hanging entity in own claimed chunk where world does allow interaction in claimed chunks
     *  • Place hanging entity in own claimed chunk where world does not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Pickup liquid in unclaimed chunk where world allows breaking in unclaimed chunk
     *  • Pickup liquid in unclaimed chunk where world does not allow breaking in unclaimed chunk
     *  • Pickup liquid in another player's claimed chunk (without access) where world does allow breaking in claimed chunks
     *  • Pickup liquid in another player's claimed chunk (without access) where world does not allow breaking in claimed chunks
     *  • Pickup liquid in another player's claimed chunk (with access) where world does allow breaking in claimed chunks
     *  • Pickup liquid in another player's claimed chunk (with access) where world does not allow breaking in claimed chunks
     *  • Pickup liquid in own claimed chunk where world does allow breaking in claimed chunks
     *  • Pickup liquid in own claimed chunk where world does not allow breaking in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Place liquid in unclaimed chunk where world allows placing in unclaimed chunk
     *  • Place liquid in unclaimed chunk where world does not allow placing in unclaimed chunk
     *  • Place liquid in another player's claimed chunk (without access) where world does allow placing in claimed chunks
     *  • Place liquid in another player's claimed chunk (without access) where world does not allow placing in claimed chunks
     *  • Place liquid in another player's claimed chunk (with access) where world does allow placing in claimed chunks
     *  • Place liquid in another player's claimed chunk (with access) where world does not allow placing in claimed chunks
     *  • Place liquid in own claimed chunk where world does allow placing in claimed chunks
     *  • Place liquid in own claimed chunk where world does not allow placing in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: TEST
     *  • Create lead in unclaimed chunk where world allows interaction in unclaimed chunk
     *  • Create lead in unclaimed chunk where world does not allow interaction in unclaimed chunk
     *  • Create lead in another player's claimed chunk (without access) where world does allow interaction in claimed chunks
     *  • Create lead in another player's claimed chunk (without access) where world does not allow interaction in claimed chunks
     *  • Create lead in another player's claimed chunk (with access) where world does allow interaction in claimed chunks
     *  • Create lead in another player's claimed chunk (with access) where world does not allow interaction in claimed chunks
     *  • Create lead in own claimed chunk where world does allow interaction in claimed chunks
     *  • Create lead in own claimed chunk where world does not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
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
     *
     * TODO: Keep lead destruction as an interaction event or make it a damage event on the entity?
     *
     * TODO: TEST
     *  • Break lead in unclaimed chunk where world allows interaction in unclaimed chunk
     *  • Break lead in unclaimed chunk where world does not allow interaction in unclaimed chunk
     *  • Break lead in another player's claimed chunk (without access) where world does allow interaction in claimed chunks
     *  • Break lead in another player's claimed chunk (without access) where world does not allow interaction in claimed chunks
     *  • Break lead in another player's claimed chunk (with access) where world does allow interaction in claimed chunks
     *  • Break lead in another player's claimed chunk (with access) where world does not allow interaction in claimed chunks
     *  • Break lead in own claimed chunk where world does allow interaction in claimed chunks
     *  • Break lead in own claimed chunk where world does not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can damage this entity
        onEntityEvent(() -> event.setCancelled(true),
                      event.getPlayer(),
                      event.getEntity(),
                      ClaimChunkWorldProfile.EntityAccessType.INTERACT);
    }

    // Armor Stands

    /**
     * Event handler for when players manipulate (or do anything to, basically) armor stands
     *
     * TODO: TEST
     *  • Manipulate armor stand in unclaimed chunk where world allows interaction in unclaimed chunk
     *  • Manipulate armor stand in unclaimed chunk where world does not allow interaction in unclaimed chunk
     *  • Manipulate armor stand in another player's claimed chunk (without access) where world does allow interaction in claimed chunks
     *  • Manipulate armor stand in another player's claimed chunk (without access) where world does not allow interaction in claimed chunks
     *  • Manipulate armor stand in another player's claimed chunk (with access) where world does allow interaction in claimed chunks
     *  • Manipulate armor stand in another player's claimed chunk (with access) where world does not allow interaction in claimed chunks
     *  • Manipulate armor stand in own claimed chunk where world does allow interaction in claimed chunks
     *  • Manipulate armor stand in own claimed chunk where world does not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
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

    /*
        TODO: EXPLOSION TESTS
         • Blow up blocks in unclaimed chunk where world allows explosions in unclaimed chunks
         • Blow up blocks in unclaimed chunk where world does not allow explosions in unclaimed chunks
         • Blow up blocks in claimed chunk where world allows explosions in claimed chunks
         • Blow up blocks in claimed chunk where world does not allow explosions in claimed chunks
         • Blow up entities in unclaimed chunk where world allows explosions in unclaimed chunks
         • Blow up entities in unclaimed chunk where world does not allow explosions in unclaimed chunks
         • Blow up entities in claimed chunk where world allows explosions in claimed chunks
         • Blow up entities in claimed chunk where world does not allow explosions in claimed chunks
     */

    /**
     * Event handler for when an entity is damaged by an explosion
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onEntityDamagedByEntityExplosion(EntityDamageByEntityEvent event) {
        if (event != null
                && !event.isCancelled()
                && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            // Check if the explosion can damage this entity
            onExplosionForEntityEvent(() -> event.setCancelled(true),
                                      event.getEntity());
        }
    }

    /**
     * Event handler for when an entity is damaged by an explosion(again, bc to be safe? idk spigot can be weird and
     * it's better safe than sorry)
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onEntityDamagedByExplosion(EntityDamageEvent event) {
        if (event != null
                && !event.isCancelled()
                && (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                    || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            // Check if the explosion can damage this entity
            onExplosionForEntityEvent(() -> event.setCancelled(true),
                                      event.getEntity());
        }
    }

    // Explosion protection for blocks from block and entity explosions

    /**
     * Event handler for when a block explodes
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event != null && !event.isCancelled()) {
            onExplosionEvent(event.getEntity().getWorld(),
                             event.blockList());
        }
    }

    // Fire spread protection

    /**
     * Event handler for when a block spreads, like fire
     *
     * TODO: TEST
     *  • Test spread into unclaimed chunk from unclaimed chunk
     *  • Test spread into unclaimed chunk from claimed chunk
     *  • Test spread into claimed chunk from same owner's claimed chunk
     *  • Test spread into claimed chunk from different owner's claimed chunk
     *  • Test spread into claimed chunk from unclaimed chunk
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event != null
                && !event.isCancelled()
                && event.getSource().getType() == Material.FIRE) {
            onSpreadEvent(() -> event.setCancelled(true),
                          event.getSource(),
                          event.getBlock());
        }
    }

    // Piston protection

    /*
        TODO: PISTON TESTS
         • Test extend/retract into/from unclaimed chunk from/into unclaimed chunk
         • Test extend/retract into/from unclaimed chunk from/into claimed chunk
         • Test extend/retract into/from claimed chunk from/into different owner's claimed chunk
         • Test extend/retract into/from claimed chunk from/into same owner's claimed chunk
         • Test extend/retract into/from claimed chunk from/into unclaimed chunk
         • Test slime & honey blocks
     */

    @SuppressWarnings("unused")
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event != null && !event.isCancelled()) {
            onPistonAction(() -> event.setCancelled(true),
                           event.getBlock(),
                           event.getBlocks());
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event != null && !event.isCancelled()) {
            onPistonAction(() -> event.setCancelled(true),
                           event.getBlock(),
                           event.getBlocks());
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

            // Send cancellation message
            Messages.sendAccessDeniedEntityMessage(player, claimChunk, entity.getType().getKey(), accessType, chunkOwner);
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

            // Send cancellation message
            Messages.sendAccessDeniedBlockMessage(player, claimChunk, block.getType().getKey(), accessType, chunkOwner);
        }
    }

    private void onExplosionForEntityEvent(@Nonnull Runnable cancel,
                                           @Nonnull Entity entity) {
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

        if (worldProfile.enabled) {
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
    }

    private void onSpreadEvent(@Nonnull Runnable cancel,
                               @Nonnull Block sourceBlock,
                               @Nonnull Block newBlock) {
        // Check chunks
        Chunk sourceChunk = sourceBlock.getChunk();
        Chunk newChunk = newBlock.getChunk();

        // Get the owners of the chunks
        UUID sourceOwner = claimChunk.getChunkHandler().getOwner(sourceChunk);
        UUID newOwner = claimChunk.getChunkHandler().getOwner(newChunk);

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(sourceChunk.getWorld().getName());

        if (profile.enabled) {
            // Check if any spread needs to be stopped
            if (Objects.equals(sourceOwner, newOwner)) {
                // Disable fire spread from unclaimed chunks into unclaimed chunks
                if (!profile.fireInUnclaimed && sourceOwner == null) {
                    cancel.run();

                    // Disable fire spread from claimed chunks into the same owner's chunks
                } else if (!profile.fireInClaimed && sourceOwner != null) {
                    cancel.run();
                }
            } else {
                // Disable fire spread from unclaimed chunks into claimed chunks
                if (!profile.fireFromUnclaimedIntoClaimed && sourceOwner == null) {
                    cancel.run();

                    // Disable fire spread from claimed chunks into different claimed chunks
                } else if (!profile.fireFromClaimedIntoDiffClaimed && newOwner != null && sourceOwner != null) {
                    cancel.run();

                    // Disable fire spread from claimed chunks into unclaimed chunks
                } else if (!profile.fireFromClaimedIntoUnclaimed && sourceOwner != null) {
                    cancel.run();
                }
            }
        }
    }

    private void onPistonAction(Runnable cancel, Block piston, List<Block> blocks) {
        // Get the world for this profile
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(piston.getWorld().getName());

        if (profile.enabled) {
            // Get the source and target chunks
            UUID sourceChunkOwner = claimChunk.getChunkHandler().getOwner(piston.getChunk());
            HashMap<Chunk, UUID> targetChunksOwners = new HashMap<>();
            for (Block block : blocks) {
                targetChunksOwners.computeIfAbsent(block.getChunk(),
                        (chunk) -> claimChunk.getChunkHandler().getOwner(chunk));
            }

            // Check if unclaimed to claimed piston actions are protected
            if (sourceChunkOwner == null && !profile.pistonUnclaimedToClaimed) {
                for (UUID owner : targetChunksOwners.values()) {
                    if (owner != null) {
                        cancel.run();
                        return;
                    }
                }
            }

            // Check if claimed to unclaimed piston actions are protected
            if (sourceChunkOwner != null && !profile.pistonClaimedToUnclaimed) {
                for (UUID owner : targetChunksOwners.values()) {
                    if (owner == null) {
                        cancel.run();
                        return;
                    }
                }
            }

            // Check if claimed to claimed piston actions are protected
            if (sourceChunkOwner != null && !profile.pistonClaimedToDiffClaimed) {
                for (UUID owner : targetChunksOwners.values()) {
                    if (owner != null && !owner.equals(sourceChunkOwner)) {
                        cancel.run();
                        return;
                    }
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
