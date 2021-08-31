package com.cjburkey.claimchunk.event;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Messages;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.config.*;
import com.cjburkey.claimchunk.config.access.BlockAccess;
import com.cjburkey.claimchunk.config.access.EntityAccess;
import com.cjburkey.claimchunk.config.spread.SpreadProfile;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
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
     * <p>TODO: TEST //• Interact with entity in unclaimed chunk where world allows interaction in
     * unclaimed chunk //• Interact with entity in unclaimed chunk where world does not allow
     * interaction in unclaimed chunk //• Interact with entity in another player's claimed chunk
     * (without access) where world does allow interaction in claimed chunks //• Interact with
     * entity in another player's claimed chunk (without access) where world does not allow
     * interaction in claimed chunks • Interact with entity in another player's claimed chunk (with
     * access) where world does allow interaction in claimed chunks • Interact with entity in
     * another player's claimed chunk (with access) where world does not allow interaction in
     * claimed chunks • Interact with entity in own claimed chunk where world does allow interaction
     * in claimed chunks • Interact with entity in own claimed chunk where world does not allow
     * interaction in claimed chunks
     */
    @EventHandler
    public void onEntityInteraction(PlayerInteractEntityEvent event) {
        if (event != null && !event.isCancelled()) {

            // Check if the player can interact with this entity
            onEntityEvent(
                    () -> event.setCancelled(true),
                    event.getPlayer(),
                    event.getRightClicked(),
                    EntityAccess.EntityAccessType.INTERACT);
        }
    }

    /**
     * Event handler for when an entity is damaged by another entity (maybe a player)
     *
     * <p>TODO: TEST //• Damage entity in unclaimed chunk where world allows damaging in unclaimed
     * chunk //• Damage entity in unclaimed chunk where world does not allow damaging in unclaimed
     * chunk //• Damage entity in another player's claimed chunk (without access) where world does
     * allow damaging in claimed chunks //• Damage entity in another player's claimed chunk (without
     * access) where world does not allow damaging in claimed chunks • Damage entity in another
     * player's claimed chunk (with access) where world does allow damaging in claimed chunks •
     * Damage entity in another player's claimed chunk (with access) where world does not allow
     * damaging in claimed chunks • Damage entity in own claimed chunk where world does allow
     * damaging in claimed chunks • Damage entity in own claimed chunk where world does not allow
     * damaging in claimed chunks
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
                onEntityEvent(
                        () -> event.setCancelled(true),
                        player,
                        event.getEntity(),
                        EntityAccess.EntityAccessType.DAMAGE);
            }
        }
    }

    // Blocks

    /**
     * Event handler for when a player breaks a block
     *
     * <p>TODO: TEST //• Break block in unclaimed chunk where world allows breaking in unclaimed
     * chunk //• Break block in unclaimed chunk where world does not allow breaking in unclaimed
     * chunk //• Break block in another player's claimed chunk (without access) where world does
     * allow breaking in claimed chunks //• Break block in another player's claimed chunk (without
     * access) where world does not allow breaking in claimed chunks • Break block in another
     * player's claimed chunk (with access) where world does allow breaking in claimed chunks •
     * Break block in another player's claimed chunk (with access) where world does not allow
     * breaking in claimed chunks • Break block in own claimed chunk where world does allow breaking
     * in claimed chunks • Break block in own claimed chunk where world does not allow breaking in
     * claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event != null && !event.isCancelled()) {

            // Check if the player can break this block
            onBlockEvent(
                    () -> event.setCancelled(true),
                    event.getPlayer(),
                    event.getBlock().getType(),
                    event.getBlock(),
                    BlockAccess.BlockAccessType.BREAK);
        }
    }

    /** Event handler for when a block is changed by a wither boss explosion */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockExplode(EntityChangeBlockEvent event) {
        if (event != null
                && !event.isCancelled()
                && (event.getEntityType() == EntityType.WITHER
                        || event.getEntityType() == EntityType.WITHER_SKULL)) {
            onExplosionForBlockEvent(() -> event.setCancelled(true), event.getBlock());
        }
    }

    /**
     * Event handler for when a player places a block
     *
     * <p>TODO: TEST //• Place block in unclaimed chunk where world allows placing in unclaimed
     * chunk //• Place block in unclaimed chunk where world does not allow placing in unclaimed
     * chunk //• Place block in another player's claimed chunk (without access) where world does
     * allow placing in claimed chunks //• Place block in another player's claimed chunk (without
     * access) where world does not allow placing in claimed chunks • Place block in another
     * player's claimed chunk (with access) where world does allow placing in claimed chunks • Place
     * block in another player's claimed chunk (with access) where world does not allow placing in
     * claimed chunks • Place block in own claimed chunk where world does allow placing in claimed
     * chunks • Place block in own claimed chunk where world does not allow placing in claimed
     * chunks • Place adjacent block in neighboring unclaimed chunk. • Place adjacent block in
     * neighboring claimed-by-same chunk. • Place adjacent block in neighboring claimed-by-different
     * chunk.
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check to make sure this block doesn't connect to any blocks in a claim
        onBlockAdjacentCheck(() -> event.setCancelled(true), event.getPlayer(), event.getBlock());

        // Make sure the event wasn't cancelled by the adjacent check
        if (event.isCancelled()) return;

        // Check if the player can place this block
        onBlockEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getBlock().getType(),
                event.getBlock(),
                BlockAccess.BlockAccessType.PLACE);
    }

    /**
     * Event handler for when a player right clicks on a block
     *
     * <p>TODO: TEST //• Interact with block in unclaimed chunk where world allows interaction in
     * unclaimed chunk //• Interact with block in unclaimed chunk where world does not allow
     * interaction in unclaimed chunk //• Interact with block in another player's claimed chunk
     * (without access) where world does allow interaction in claimed chunks //• Interact with block
     * in another player's claimed chunk (without access) where world does not allow interaction in
     * claimed chunks • Interact with block in another player's claimed chunk (with access) where
     * world does allow interaction in claimed chunks • Interact with block in another player's
     * claimed chunk (with access) where world does not allow interaction in claimed chunks •
     * Interact with block in own claimed chunk where world does allow interaction in claimed chunks
     * • Interact with block in own claimed chunk where world does not allow interaction in claimed
     * chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockInteraction(PlayerInteractEvent event) {
        if (event != null
                && event.getClickedBlock() != null
                && event.getClickedBlock().getType() != Material.AIR
                && event.useInteractedBlock() == Event.Result.ALLOW
                && ((event.getAction() == Action.RIGHT_CLICK_BLOCK
                                && (!event.isBlockInHand() || !event.getPlayer().isSneaking())
                                && event.useInteractedBlock() == Event.Result.ALLOW)
                        || event.getAction() == Action.PHYSICAL)) {
            // Check if the player can interact with this block
            onBlockEvent(
                    () -> event.setUseInteractedBlock(Event.Result.DENY),
                    event.getPlayer(),
                    event.getClickedBlock().getType(),
                    event.getClickedBlock(),
                    BlockAccess.BlockAccessType.INTERACT);
        }
    }

    // Hanging entities

    /**
     * Event handler for when things like item frames and paintings break
     *
     * <p>TODO: TEST //• Break hanging entity in unclaimed chunk where world allows damaging in
     * unclaimed chunk //• Break hanging entity in unclaimed chunk where world does not allow
     * damaging in unclaimed chunk //• Break hanging entity in another player's claimed chunk
     * (without access) where world does allow damaging in claimed chunks //• Break hanging entity
     * in another player's claimed chunk (without access) where world does not allow damaging in
     * claimed chunks • Break hanging entity in another player's claimed chunk (with access) where
     * world does allow damaging in claimed chunks • Break hanging entity in another player's
     * claimed chunk (with access) where world does not allow damaging in claimed chunks • Break
     * hanging entity in own claimed chunk where world does allow damaging in claimed chunks • Break
     * hanging entity in own claimed chunk where world does not allow damaging in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event != null && !event.isCancelled()) {
            // Check if the break was the result of an explosion
            if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
                onExplosionForEntityEvent(() -> event.setCancelled(true), event.getEntity());
                return;
            }

            // Otherwise, check if the entity is a player
            Player player = unwrapPlayer(event.getRemover());

            // If the action isn't being performed by a player, we don't
            // particularly care now.
            if (player != null) {
                // Check if the player can damage this entity
                onEntityEvent(
                        () -> event.setCancelled(true),
                        player,
                        event.getEntity(),
                        EntityAccess.EntityAccessType.DAMAGE);
            }
        }
    }

    /**
     * Event handler for when things like item frames and paintings are placed
     *
     * <p>TODO: TEST //• Place hanging entity in unclaimed chunk where world allows interaction in
     * unclaimed chunk //• Place hanging entity in unclaimed chunk where world does not allow
     * interaction in unclaimed chunk //• Place hanging entity in another player's claimed chunk
     * (without access) where world does allow interaction in claimed chunks • Place hanging entity
     * in another player's claimed chunk (without access) where world does not allow interaction in
     * claimed chunks • Place hanging entity in another player's claimed chunk (with access) where
     * world does allow interaction in claimed chunks • Place hanging entity in another player's
     * claimed chunk (with access) where world does not allow interaction in claimed chunks • Place
     * hanging entity in own claimed chunk where world does allow interaction in claimed chunks •
     * Place hanging entity in own claimed chunk where world does not allow interaction in claimed
     * chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event != null && !event.isCancelled() && event.getPlayer() != null) {
            // Check if the player can interact with this entity (closest to "placing" an item
            // frame)
            onEntityEvent(
                    () -> event.setCancelled(true),
                    event.getPlayer(),
                    event.getEntity(),
                    EntityAccess.EntityAccessType.INTERACT);
        }
    }

    // Bucket usage

    /**
     * Event handler for when players pick up a liquid with a bucket
     *
     * <p>TODO: TEST //• Pickup liquid in unclaimed chunk where world allows breaking in unclaimed
     * chunk //• Pickup liquid in unclaimed chunk where world does not allow breaking in unclaimed
     * chunk //• Pickup liquid in another player's claimed chunk (without access) where world does
     * allow breaking in claimed chunks //• Pickup liquid in another player's claimed chunk (without
     * access) where world does not allow breaking in claimed chunks • Pickup liquid in another
     * player's claimed chunk (with access) where world does allow breaking in claimed chunks •
     * Pickup liquid in another player's claimed chunk (with access) where world does not allow
     * breaking in claimed chunks • Pickup liquid in own claimed chunk where world does allow
     * breaking in claimed chunks • Pickup liquid in own claimed chunk where world does not allow
     * breaking in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLiquidPickup(PlayerBucketFillEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can break this block
        onBlockEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getBlock().getType(),
                event.getBlock(),
                BlockAccess.BlockAccessType.BREAK);
    }

    /**
     * Event handler for when players put down a liquid with a bucket
     *
     * <p>TODO: TEST //• Place liquid in unclaimed chunk where world allows placing in unclaimed
     * chunk //• Place liquid in unclaimed chunk where world does not allow placing in unclaimed
     * chunk //• Place liquid in another player's claimed chunk (without access) where world does
     * allow placing in claimed chunks //• Place liquid in another player's claimed chunk (without
     * access) where world does not allow placing in claimed chunks • Place liquid in another
     * player's claimed chunk (with access) where world does allow placing in claimed chunks • Place
     * liquid in another player's claimed chunk (with access) where world does not allow placing in
     * claimed chunks • Place liquid in own claimed chunk where world does allow placing in claimed
     * chunks • Place liquid in own claimed chunk where world does not allow placing in claimed
     * chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLiquidPlace(PlayerBucketEmptyEvent event) {
        if (event == null || event.isCancelled()) return;

        // Determine the kind of liquid contained within the bucket
        Material bucketLiquid = null;
        if (event.getBucket() == Material.WATER_BUCKET) bucketLiquid = Material.WATER;
        if (event.getBucket() == Material.LAVA_BUCKET) bucketLiquid = Material.LAVA;
        if (bucketLiquid == null) return;

        // Check if the player can place this block
        onBlockEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                bucketLiquid,
                event.getBlock(),
                BlockAccess.BlockAccessType.PLACE);
    }

    /** Event handler for when players capture entities (fish and stuff) in a bucket. */
    @EventHandler
    public void onFishCapture(PlayerBucketEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Delegate to interaction event permissions
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getEntity(),
                EntityAccess.EntityAccessType.INTERACT);
    }

    // Leads

    /**
     * Event handler for when players create a lead
     *
     * <p>TODO: TEST //• Create lead in unclaimed chunk where world allows interaction in unclaimed
     * chunk //• Create lead in unclaimed chunk where world does not allow interaction in unclaimed
     * chunk //• Create lead in another player's claimed chunk (without access) where world does
     * allow interaction in claimed chunks //• Create lead in another player's claimed chunk
     * (without access) where world does not allow interaction in claimed chunks • Create lead in
     * another player's claimed chunk (with access) where world does allow interaction in claimed
     * chunks • Create lead in another player's claimed chunk (with access) where world does not
     * allow interaction in claimed chunks • Create lead in own claimed chunk where world does allow
     * interaction in claimed chunks • Create lead in own claimed chunk where world does not allow
     * interaction in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLeadCreate(PlayerLeashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getEntity(),
                EntityAccess.EntityAccessType.INTERACT);
    }

    /**
     * Event handler for when players break a lead
     *
     * <p>TODO: Keep lead destruction as an interaction event or make it a damage event on the
     * entity?
     *
     * <p>TODO: TEST //• Break lead in unclaimed chunk where world allows interaction in unclaimed
     * chunk //• Break lead in unclaimed chunk where world does not allow interaction in unclaimed
     * chunk //• Break lead in another player's claimed chunk (without access) where world does
     * allow interaction in claimed chunks //• Break lead in another player's claimed chunk (without
     * access) where world does not allow interaction in claimed chunks • Break lead in another
     * player's claimed chunk (with access) where world does allow interaction in claimed chunks •
     * Break lead in another player's claimed chunk (with access) where world does not allow
     * interaction in claimed chunks • Break lead in own claimed chunk where world does allow
     * interaction in claimed chunks • Break lead in own claimed chunk where world does not allow
     * interaction in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLeadDestroy(PlayerUnleashEntityEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can damage this entity
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getEntity(),
                EntityAccess.EntityAccessType.INTERACT);
    }

    // Armor Stands

    /**
     * Event handler for when players manipulate (or do anything to, basically) armor stands
     *
     * <p>TODO: TEST //• Manipulate armor stand in unclaimed chunk where world allows interaction in
     * unclaimed chunk //• Manipulate armor stand in unclaimed chunk where world does not allow
     * interaction in unclaimed chunk //• Manipulate armor stand in another player's claimed chunk
     * (without access) where world does allow interaction in claimed chunks //• Manipulate armor
     * stand in another player's claimed chunk (without access) where world does not allow
     * interaction in claimed chunks • Manipulate armor stand in another player's claimed chunk
     * (with access) where world does allow interaction in claimed chunks • Manipulate armor stand
     * in another player's claimed chunk (with access) where world does not allow interaction in
     * claimed chunks • Manipulate armor stand in own claimed chunk where world does allow
     * interaction in claimed chunks • Manipulate armor stand in own claimed chunk where world does
     * not allow interaction in claimed chunks
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (event == null || event.isCancelled()) return;

        // Check if the player can interact with this entity
        onEntityEvent(
                () -> event.setCancelled(true),
                event.getPlayer(),
                event.getRightClicked(),
                EntityAccess.EntityAccessType.INTERACT);
    }

    // Explosion protection for entities

    /** Event handler for when an entity is damaged by an explosion */
    @SuppressWarnings("unused")
    @EventHandler
    public void onEntityDamagedByEntityExplosion(EntityDamageByEntityEvent event) {
        if (event != null
                && !event.isCancelled()
                && (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                        || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            // Check if the explosion can damage this entity
            onExplosionForEntityEvent(() -> event.setCancelled(true), event.getEntity());
        }
    }

    /**
     * Event handler for when an entity is damaged by an explosion(again, bc to be safe? idk spigot
     * can be weird and it's better safe than sorry)
     */
    @EventHandler
    public void onEntityDamagedByExplosion(EntityDamageEvent event) {
        if (event != null
                && !event.isCancelled()
                && (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                        || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            // Check if the explosion can damage this entity
            onExplosionForEntityEvent(() -> event.setCancelled(true), event.getEntity());
        }
    }

    // Explosion protection for blocks from block and entity explosions

    /** Event handler for when a block explodes */
    @SuppressWarnings("unused")
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event != null && !event.isCancelled()) {
            onExplosionEvent(event.getBlock().getWorld(), event.blockList());
        }
    }

    /** Event handler for when an entity explodes */
    @SuppressWarnings("unused")
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event != null && !event.isCancelled()) {
            onExplosionEvent(event.getEntity().getWorld(), event.blockList());
        }
    }

    // Fire spread protection

    /**
     * Event handler for when a block spreads, like fire.
     *
     * <p>TODO: TEST • Test spread into unclaimed chunk from unclaimed chunk • Test spread into
     * unclaimed chunk from claimed chunk • Test spread into claimed chunk from same owner's claimed
     * chunk • Test spread into claimed chunk from different owner's claimed chunk • Test spread
     * into claimed chunk from unclaimed chunk
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        if (event != null && !event.isCancelled() && event.getSource().getType() == Material.FIRE) {
            onSpreadEvent(
                    () -> event.setCancelled(true),
                    event.getSource(),
                    event.getBlock(),
                    profile -> profile.fireSpread);
        }
    }

    /**
     * Event handler for when a liquid spreads, like water or lava.
     *
     * <p>TODO: TEST
     */
    @SuppressWarnings("unused")
    @EventHandler
    public void onLiquidSpread(BlockFromToEvent event) {
        if (event != null && !event.isCancelled()) {
            // Get the spreading block type
            Material blockType = event.getBlock().getType();
            if (blockType != Material.WATER
                    // Protection against waterlogged block water spread
                    && !isWaterlogged(event.getBlock())
                    && blockType != Material.LAVA) {
                return;
            }

            // Check if we need to cancel this event
            onSpreadEvent(
                    () -> event.setCancelled(true),
                    event.getBlock(),
                    event.getToBlock(),
                    profile ->
                            (blockType == Material.LAVA
                                    ? profile.lavaSpread
                                    : profile.waterSpread));
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
            onPistonAction(
                    () -> event.setCancelled(true),
                    event.getBlock(),
                    event.getDirection(),
                    event.getBlocks());
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event != null && !event.isCancelled()) {
            onPistonAction(
                    () -> event.setCancelled(true),
                    event.getBlock(),
                    event.getDirection(),
                    event.getBlocks());
        }
    }

    // Command execution blocking

    @EventHandler
    public void onPlyCmd(PlayerCommandPreprocessEvent event) {
        if (event != null && !event.isCancelled()) {
            // Get the full message sent
            String cmdLabel = event.getMessage();

            // Make sure it was a command
            if (cmdLabel.startsWith("/")) {
                // Split the command into its label and separate arguments
                String[] args = cmdLabel.substring(1).split("\\s+");
                if (args.length >= 1) {
                    // Get the command instance from the command label
                    PluginCommand cmd = Bukkit.getPluginCommand(args[0]);
                    if (cmd != null) {
                        onCommand(() -> event.setCancelled(true), event.getPlayer(), cmd);
                    }
                }
            }
        }
    }

    /**
     * Event handler for when blocks will be spawned from a player (or dispenser) using bonemeal on
     * grass, for example.
     */
    @EventHandler
    public void onBonemeal(BlockFertilizeEvent event) {
        if (event == null || event.isCancelled()) return;

        // Get info
        Player player = event.getPlayer();
        if (player == null) return;
        UUID mealer = player.getUniqueId();

        // Check if admin to bypass
        if (claimChunk.getAdminOverride().hasOverride(mealer)) return;

        // Cache chunk ownership because why not
        HashMap<ChunkPos, Boolean> claimed = new HashMap<>();
        // Keep track of blocks to remove from the list
        HashSet<BlockState> remove = new HashSet<>();

        // Decide which blocks to remove from the change list
        event.getBlocks().stream()
                .filter(
                        blockState -> {
                            ChunkPos p = new ChunkPos(blockState.getChunk());
                            return claimed.computeIfAbsent(
                                    p,
                                    pos ->
                                            // This method returns `true` if the method should
                                            // be cancelled
                                            onBlockEvent(
                                                    player,
                                                    blockState.getType(),
                                                    blockState.getBlock(),
                                                    BlockAccess.BlockAccessType.PLACE,
                                                    false));
                        })
                .forEach(remove::add);

        // Remove all the blocks previously designated for removal
        event.getBlocks().removeAll(remove);
    }

    // -- HELPER METHODS -- //

    private void onEntityEvent(
            @NotNull Runnable cancel,
            @NotNull Player player,
            @NotNull Entity entity,
            @NotNull EntityAccess.EntityAccessType accessType) {

        // Get the profile for this world
        ClaimChunkWorldProfile profile =
                claimChunk.getProfileManager().getProfile(entity.getWorld().getName());

        // check if the world profile is enabled
        if (profile.enabled) {
            final UUID ply = player.getUniqueId();
            // check if the player has AdminOverride
            // do an early return
            if (claimChunk.getAdminOverride().hasOverride(player.getUniqueId())) return;

            final UUID chunkOwner =
                    claimChunk.getChunkHandler().getOwner(entity.getLocation().getChunk());
            final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
            final boolean isOwnerOrAccess =
                    isOwner
                            || (chunkOwner != null
                                    && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

            // Delegate event cancellation to the world profile
            if (!profile.canAccessEntity(chunkOwner != null, isOwnerOrAccess, entity, accessType)) {
                // cancel event
                cancel.run();

                // Send cancellation message
                Messages.sendAccessDeniedEntityMessage(
                        player, claimChunk, entity.getType().getKey(), accessType, chunkOwner);
            }
        }
    }

    private void onBlockAdjacentCheck(
            @NotNull Runnable cancel, @NotNull Player player, @NotNull Block block) {
        // Get the current world profile
        ClaimChunkWorldProfile profile =
                claimChunk.getProfileManager().getProfile(block.getWorld().getName());

        // Make sure we're supposed to check for adjacent blocks for this type in this world
        if (profile.enabled && profile.preventAdjacent.contains(block.getType())) {
            final UUID ply = player.getUniqueId();
            // check if the player has AdminOverride
            // (early return)
            if (claimChunk.getAdminOverride().hasOverride(ply)) return;

            final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(block.getChunk());

            // Loop through adjacent horizontal neighbors
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    // Non-diagonal check
                    if (x != z) {
                        // Get neighbor info
                        final Block neighbor = block.getRelative(x, 0, z);
                        final UUID neighborOwner =
                                claimChunk.getChunkHandler().getOwner(neighbor.getChunk());

                        // Make sure the neighbor block is the same type and is owned by someone
                        // other than the
                        // owner
                        // for the chunk in which they're building, or this person has access to
                        // build in that
                        // owner's
                        // chunk
                        final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
                        final boolean isOwnerOrAccess =
                                isOwner
                                        || (chunkOwner != null
                                                && claimChunk
                                                        .getPlayerHandler()
                                                        .hasAccess(chunkOwner, ply));
                        if (neighbor.getType() == block.getType()
                                && neighborOwner != null
                                && neighborOwner != chunkOwner
                                && !isOwnerOrAccess) {

                            // cancel event
                            cancel.run();

                            // Send cancellation message
                            final String ownerName =
                                    claimChunk.getPlayerHandler().getChunkName(neighborOwner);
                            Utils.toPlayer(
                                    player,
                                    // TODO: FIX THIS METHOD
                                    Messages.replaceLocalizedMsg(
                                            player,
                                            claimChunk
                                                    .getMessages()
                                                    .chunkCancelAdjacentPlace
                                                    .replaceAll("%%PLAYER%%", ownerName),
                                            "%%BLOCK%%",
                                            "block."
                                                    + block.getType().getKey().getNamespace()
                                                    + "."
                                                    + block.getType().getKey().getKey()));

                            // Just break here
                            return;
                        }
                    }
                }
            }
        }
    }

    private void onBlockEvent(
            @NotNull Runnable cancel,
            @NotNull Player player,
            @NotNull Material blockType,
            @NotNull Block block,
            @NotNull BlockAccess.BlockAccessType accessType) {
        if (onBlockEvent(player, blockType, block, accessType, true)) {
            cancel.run();
        }
    }

    // Returns whether the event should be cancelled
    private boolean onBlockEvent(
            @NotNull Player player,
            @NotNull Material blockType,
            @NotNull Block block,
            @NotNull BlockAccess.BlockAccessType accessType,
            boolean message) {
        // Get the profile for this world
        ClaimChunkWorldProfile profile =
                claimChunk.getProfileManager().getProfile(block.getWorld().getName());

        // check if the world profile is enabled
        if (profile.enabled) {
            final UUID ply = player.getUniqueId();
            // check if the player has AdminOverride
            // If they do, let the event pass through without being cancelled
            if (claimChunk.getAdminOverride().hasOverride(ply)) return false;

            final UUID chunkOwner = claimChunk.getChunkHandler().getOwner(block.getChunk());
            final boolean isOwner = (chunkOwner != null && chunkOwner.equals(ply));
            final boolean isOwnerOrAccess =
                    isOwner
                            || (chunkOwner != null
                                    && claimChunk.getPlayerHandler().hasAccess(chunkOwner, ply));

            // Delegate event cancellation to the world profile
            if (profile.enabled
                    && !profile.canAccessBlock(
                            chunkOwner != null,
                            isOwnerOrAccess,
                            block.getWorld().getName(),
                            blockType,
                            accessType)) {
                if (message) {
                    // Send cancellation message
                    Messages.sendAccessDeniedBlockMessage(
                            player, claimChunk, blockType.getKey(), accessType, chunkOwner);
                }

                // Cancel the event
                return true;
            }
        }

        // Let the event pass
        return false;
    }

    private void onExplosionForEntityEvent(@NotNull Runnable cancel, @NotNull Entity entity) {
        // Get this name for later usage
        final String worldName = entity.getWorld().getName();

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(worldName);

        // Delegate event cancellation to the world profile
        if (profile.enabled
                && !profile.getEntityAccess(
                                claimChunk
                                        .getChunkHandler()
                                        .isClaimed(entity.getLocation().getChunk()),
                                worldName,
                                entity.getType())
                        .allowExplosion) {
            cancel.run();
        }
    }

    private void onExplosionForBlockEvent(@NotNull Runnable cancel, @NotNull Block block) {
        // Get this name for later usage
        final String worldName = block.getWorld().getName();

        // Get the profile for this world
        ClaimChunkWorldProfile profile = claimChunk.getProfileManager().getProfile(worldName);

        // Delegate event cancellation to the world profile
        if (profile.enabled
                && !profile.getBlockAccess(
                                claimChunk.getChunkHandler().isClaimed(block.getChunk()),
                                worldName,
                                block.getType())
                        .allowExplosion) {
            cancel.run();
        }
    }

    private void onExplosionEvent(@NotNull World world, @NotNull Collection<Block> blockList) {
        // Get the world name
        final String worldName = world.getName();

        // Get the world profile
        final ClaimChunkWorldProfile worldProfile =
                claimChunk.getProfileManager().getProfile(worldName);

        if (worldProfile.enabled) {
            // Get chunk handler
            final ChunkHandler chunkHandler = claimChunk.getChunkHandler();

            // Cache chunks to avoid so many look-ups through the chunk handler
            // The value is a boolean representing whether to cancel the event. `true` means the
            // event
            // will be cancelled
            final HashMap<Chunk, Boolean> cancelChunks = new HashMap<>();
            final ArrayList<Block> blocksCopy = new ArrayList<>(blockList);

            // Loop through all of the blocks
            for (Block block : blocksCopy) {
                // Get the chunk this block is in
                final Chunk chunk = block.getChunk();

                // Check if this type of block should be protected
                if (cancelChunks.computeIfAbsent(
                        chunk,
                        c ->
                                !worldProfile.getBlockAccess(
                                                chunkHandler.isClaimed(c),
                                                worldName,
                                                block.getType())
                                        .allowExplosion)) {

                    // Try to remove the block from the explosion list
                    if (!blockList.remove(block)) {
                        Utils.err(
                                "Failed to remove block of type \"%s\" at %s,%s,%s in world %s",
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

    private void onSpreadEvent(
            @NotNull Runnable cancel,
            @NotNull Block sourceBlock,
            @NotNull Block newBlock,
            @NotNull Function<ClaimChunkWorldProfile, SpreadProfile> spreadProfileGen) {
        // Check chunks
        Chunk sourceChunk = sourceBlock.getChunk();
        Chunk newChunk = newBlock.getChunk();

        // Get the owners of the chunks
        UUID sourceOwner = claimChunk.getChunkHandler().getOwner(sourceChunk);
        UUID newOwner = claimChunk.getChunkHandler().getOwner(newChunk);

        // Get the profile for this world
        ClaimChunkWorldProfile profile =
                claimChunk.getProfileManager().getProfile(sourceChunk.getWorld().getName());

        if (profile.enabled) {
            // Get the spread profile (fire, water, lava, etc)
            SpreadProfile spreadProfile = spreadProfileGen.apply(profile);
            if (spreadProfile == null) {
                Utils.err(
                        "Failed to get spread profile for block spread event from %s,%s,%s to"
                                + " %s,%s,%s in %s.",
                        sourceBlock.getLocation().getBlockX(),
                        sourceBlock.getLocation().getBlockY(),
                        sourceBlock.getLocation().getBlockZ(),
                        newBlock.getLocation().getBlockX(),
                        newBlock.getLocation().getBlockY(),
                        newBlock.getLocation().getBlockZ(),
                        sourceBlock.getWorld().getName());
                return;
            }

            // Check if this needs to be cancelled
            if (spreadProfile.getShouldCancel(sourceOwner, newOwner)) {
                cancel.run();
            }
        }
    }

    private void onPistonAction(
            @NotNull Runnable cancel,
            @NotNull Block piston,
            @NotNull BlockFace direction,
            @NotNull List<Block> blocks) {
        // Get the world for this profile
        ClaimChunkWorldProfile profile =
                claimChunk.getProfileManager().getProfile(piston.getWorld().getName());

        if (profile.enabled) {
            // Get the source and target chunks
            UUID sourceChunkOwner = claimChunk.getChunkHandler().getOwner(piston.getChunk());
            HashMap<Chunk, UUID> targetChunksOwners = new HashMap<>();
            // Keep a set of all blocks that will be moved, including future
            // positions of the moving blocks
            HashSet<Block> allBlocks = new HashSet<>(blocks);
            blocks.stream().map(block -> block.getRelative(direction)).forEach(allBlocks::add);

            // Loop through all of the involved blocks and find all affected
            // chunks
            for (Block block : allBlocks) {
                targetChunksOwners.computeIfAbsent(
                        block.getChunk(), (chunk) -> claimChunk.getChunkHandler().getOwner(chunk));
            }

            // Check if unclaimed to claimed piston actions are protected
            if (sourceChunkOwner == null && !profile.pistonExtend.fromUnclaimedIntoClaimed) {
                for (UUID owner : targetChunksOwners.values()) {
                    if (owner != null) {
                        cancel.run();
                        return;
                    }
                }
            }

            // Check if claimed to unclaimed piston actions are protected
            if (sourceChunkOwner != null && !profile.pistonExtend.fromClaimedIntoUnclaimed) {
                for (UUID owner : targetChunksOwners.values()) {
                    if (owner == null) {
                        cancel.run();
                        return;
                    }
                }
            }

            // Check if claimed to claimed piston actions are protected
            if (sourceChunkOwner != null && !profile.pistonExtend.fromClaimedIntoDiffClaimed) {
                for (UUID owner : targetChunksOwners.values()) {
                    if (owner != null
                            && !owner.equals(sourceChunkOwner)
                            && !claimChunk.getPlayerHandler().hasAccess(owner, sourceChunkOwner)) {
                        cancel.run();
                        return;
                    }
                }
            }
        }
    }

    private void onCommand(
            @NotNull Runnable cancel, @NotNull Player player, @NotNull PluginCommand command) {
        final UUID ply = player.getUniqueId();
        final UUID chunkOwner =
                claimChunk.getChunkHandler().getOwner(player.getLocation().getChunk());

        // Get the profile for this world
        ClaimChunkWorldProfile profile =
                claimChunk.getProfileManager().getProfile(player.getWorld().getName());

        // Skip if ClaimChunk is disabled in this world
        if (!profile.enabled) return;

        // Determine which list of blocked commands to check
        HashSet<String> cmds = profile.blockedCmdsInOwnClaimed;
        if (chunkOwner == null) {
            cmds = profile.blockedCmdsInUnclaimed;
        } else if (!chunkOwner.equals(ply)) {
            cmds = profile.blockedCmdsInDiffClaimed;
        }
        // Cancel if necessary
        if (cmds.contains(command.getName())) {
            cancel.run();
        }
    }

    /**
     * Checks if a given block is currently waterlogged, regardless of whether it <i>can</i> be
     * waterlogged
     *
     * @param block The block to check.
     * @return Whether this block is currently waterlogged.
     */
    private static boolean isWaterlogged(@NotNull Block block) {
        // Get the block data
        BlockData blockData = block.getBlockData();

        // Check if this block can be waterlogged
        if (blockData instanceof Waterlogged) {
            // Check if the block is currently waterlogged
            return ((Waterlogged) blockData).isWaterlogged();
        }

        // Not a waterlog-able block
        return false;
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
        if (possiblePlayer instanceof Projectile
                && ((Projectile) possiblePlayer).getShooter() instanceof Player) {
            return (Player) ((Projectile) possiblePlayer).getShooter();
        }

        // Either unimplemented or no player retrievable
        return null;
    }
}
