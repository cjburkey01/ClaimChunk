package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class ChunkEventHelper {

    public static boolean getCanEdit(@Nonnull Chunk chunk, @Nonnull UUID plyEditor) {
        // If the user is an admin, they have permission to override chunk claims.
        if (Utils.hasAdmin(Bukkit.getPlayer(plyEditor))) {
            return true;
        }

        // Glboal chunk handler
        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();

        // This chunk's owner
        final UUID PLY_OWNER = CHUNK.getOwner(chunk);

        // If the chunk isn't claimed, users can't edit if the server has
        // protections in unclaiemd chunks.
        if (PLY_OWNER == null) {
            return !Config.getBool("protection", "blockUnclaimedChunks");
        }

        // If the player is the owner, they can edit it. Obviously.
        if (PLY_OWNER.equals(plyEditor)) {
            return true;
        }

        // Check if the chunk is owned by an offline player and if players
        // should be allowed to edit in chunks with offline owners.
        boolean isOfflineAndUnprotected = Config.getBool("protection", "disableOfflineProtect")
                && Bukkit.getPlayer(PLY_OWNER) == null;

        // If the player has access or if the server allows editing offline
        // players' chunks, this player can edit.
        return ClaimChunk.getInstance().getPlayerHandler().hasAccess(PLY_OWNER, plyEditor) || isOfflineAndUnprotected;
    }

    private static void handlePlayerEvent(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull Cancellable e, @Nonnull String config) {
        if (e.isCancelled()) {
            return;
        }

        // Check if this isn't protected within the config.
        if (!Config.getBool("protection", config)) {
            return;
        }

        // If the user is permitted to edit here, then they bypass protections.
        if (getCanEdit(chunk, ply.getUniqueId())) {
            return;
        }

        // Cancel the event
        e.setCancelled(true);

        // Display cancellation message;
        String username = ClaimChunk.getInstance().getPlayerHandler().getUsername(ClaimChunk.getInstance().getChunkHandler().getOwner(chunk));

        // Send the not allowed to edit message
        if (username != null) {
            Utils.toPlayer(ply, ClaimChunk.getInstance().getMessages().chunkNoEdit.replace("%%PLAYER%%", username));
        }
    }

    public static void handleBlockEvent(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull Cancellable e) {
        handlePlayerEvent(ply, chunk, e, "blockPlayerChanges");
    }

    public static void handleInteractionEvent(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull Cancellable e) {
        handlePlayerEvent(ply, chunk, e, "blockInteractions");
    }

    private static void cancelExplosionEvent(boolean hardCancel, @Nonnull EntityExplodeEvent e) {
        if (hardCancel) {
            // This explosion event occurred within a claimed chunk.
            e.setYield(0);
            e.setCancelled(true);
        } else {
            // This explosion occurred outside of a claimed chunk but it might
            // interfere with claimed blocks.
            final ChunkHandler CHUNK_HANDLER = ClaimChunk.getInstance().getChunkHandler();

            // Remove all of the blocks within claimed chunks from this event
            // so they are not destroyed.
            // Unfortunately, there is no way to remove specific entities from
            // the damage list, so entities will not be protected from
            // explosions by this method.
            e.blockList().removeIf(block -> CHUNK_HANDLER.isClaimed(block.getChunk()));
        }
    }

    public static void handleExplosionIfConfig(@Nonnull EntityExplodeEvent e) {
        if (e.isCancelled()) return;

        final ChunkHandler CHUNK_HANLDE = ClaimChunk.getInstance().getChunkHandler();

        final EntityType TYPE = e.getEntityType();
        final Chunk CHUNK = e.getLocation().getChunk();

        // If the explosion is within a claimed chunk, it will cancel the whole
        // event.
        boolean inClaimedChunk = CHUNK_HANLDE.isClaimed(CHUNK);
        boolean hardCancel = inClaimedChunk || Config.getBool("protection", "blockUnclaimedChunks");

        // If the event is TNT/Mincart TNT related, it should be cancelled
        boolean isTnt = TYPE == EntityType.PRIMED_TNT || TYPE == EntityType.MINECART_TNT;
        boolean protectTnt = isTnt && Config.getBool("protection", "blockTnt");
        // If TNT is blocked, check if the user has used `/chunk tnt` to enable
        // it in this chunk.
        if (protectTnt && CHUNK_HANLDE.isTntEnabled(CHUNK)) {
            protectTnt = false;
        }
        if (protectTnt) {
            cancelExplosionEvent(hardCancel, e);
            return;
        }

        // Cancel crepper explosions if protection against them is enabled
        // within the config.
        boolean isCreeper = TYPE == EntityType.CREEPER;
        if (isCreeper && Config.getBool("protection", "blockCreeper")) {
            cancelExplosionEvent(hardCancel, e);
            return;
        }

        // If wither damage is prevented within the config, cancel this event
        // if it's a wither event.
        boolean isWither = TYPE == EntityType.WITHER || TYPE == EntityType.WITHER_SKULL;
        if (isWither && Config.getBool("protection", "blockWither")) {
            cancelExplosionEvent(hardCancel, e);
        }
    }

    public static void handleEntityEvent(@Nonnull Player ply, @Nonnull Entity ent, @Nonnull Cancellable e) {
        if (e.isCancelled()) return;

        // If entities aren't protected, we don't need to check if this
        // one is -_-
        // If PvP is disabled, all entities (including players) are protected.
        // If PvP is enabled, all entities except players are protected.
        boolean protectEntities = Config.getBool("protection", "protectEntities");
        boolean thisIsPvp = ent.getType() == EntityType.PLAYER;
        if (!protectEntities && !(thisIsPvp && Config.getBool("protection", "blockPvp"))) {
            return;
        }

        // Admins have permission to do anything in claimed chunks.
        if (Utils.hasAdmin(ply)) return;

        // Check if the player is able to edit in both the chunk they're in as
        // well as the chunk the animal is in.
        boolean canPlayerEditEntityChunk = getCanEdit(ent.getLocation().getChunk(), ply.getUniqueId());
        if (canPlayerEditEntityChunk) return;

        // Cancel the event
        e.setCancelled(true);
    }

    public static void handleEntityDamageEvent(@Nonnull EntityDamageByEntityEvent e) {
        // This is actually checked in the `cancelEntityEvent` method too, but
        // it's probably better not to do all these checks if the event is
        // already cancelled.
        if (e.isCancelled()) return;

        final Entity ENTITY = e.getEntity();
        final Entity DAMAGER = e.getDamager();

        final ChunkHandler CHUNK_HANDLE = ClaimChunk.getInstance().getChunkHandler();

        // If neither the chunk that the entity is in nor the chunk the player
        // is in is claimed, we don't need to protected the entity.
        if (!CHUNK_HANDLE.isClaimed(ENTITY.getLocation().getChunk()) && !CHUNK_HANDLE.isClaimed(DAMAGER.getLocation().getChunk())) {
            return;
        }

        // Get the damaging player even if they used a projectile
        Player damagingPlayer = null;
        if (DAMAGER.getType() == EntityType.PLAYER) {
            // The player is directly causing damage.
            damagingPlayer = (Player) DAMAGER;
        } else if (DAMAGER instanceof Projectile) {
            // This is an arrow or some projectile that causes damage.
            Projectile projectileEntity = (Projectile) DAMAGER;

            // If this projectile was launched by a player (like a bow from an
            // arrow), the shooter is the player.
            if (projectileEntity.getShooter() instanceof Player) {
                damagingPlayer = (Player) projectileEntity.getShooter();
            }
        }
        // The damager was not a player, so we don't need to protect the
        // entity.
        if (damagingPlayer == null) return;

        // Go ahead and handle this entity event
        handleEntityEvent(damagingPlayer, ENTITY, e);
    }

    public static void handleCommandEvent(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;

        // If the user is an admin, they can run any command.
        if (Utils.hasAdmin(ply)) return;

        // If the user has permission to edit within this chunk, they can use
        // any command they have permissions to use.
        if (getCanEdit(chunk, ply.getUniqueId())) return;

        // Get the command from the message.
        final String[] cmdAndArgs = e.getMessage()
                .trim()
                .substring(1)
                .split(" ", 1);

        // Length should never be >1 but it could be 0.
        if (cmdAndArgs.length == 1) {
            // Cancel the event if the blocked commands list contains this
            // command.
            if (Config.getList("protection", "blockedCmds").contains(cmdAndArgs[0])) e.setCancelled(true);
        }
    }

    private static void cancelWorldEvent(@Nonnull Chunk chunk, @Nonnull Cancellable e, @SuppressWarnings("SameParameterValue") @Nonnull String config) {
        if (e.isCancelled()) return;

        // If this type of thing isn't protected against, we can skip the
        // checks.
        if (!Config.getBool("protection", config)) return;

        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();

        // If the chunk is claimed, prevent the spreading.
        if (CHUNK.isClaimed(chunk)) {
            e.setCancelled(true);
        }
    }

    public static void handleSpreadEvent(@Nonnull BlockSpreadEvent e) {
        if (e.isCancelled()) return;

        // Only cancel fire spreading
        if (e.getBlock().getType() == Material.FIRE) {
            cancelWorldEvent(e.getBlock().getChunk(), e, "blockFireSpread");
        }
    }

    // A little safe helper method because the chunks might be null?
    private static boolean getChunksEqual(Chunk a, Chunk b) {
        if (Objects.equals(a, b)) return true;
        if (a == null || b == null) return false;

        return (Objects.equals(a.getWorld(), b.getWorld())
                && a.getX() == b.getX()
                && a.getZ() == b.getZ());
    }

    private static boolean getChunksSameOwner(ChunkHandler handler, Chunk a, Chunk b) {
        return getChunksEqual(a, b) || Objects.equals(handler.getOwner(b), handler.getOwner(a));
    }

    public static void handleToFromEvent(@Nonnull BlockFromToEvent e) {
        if (e.isCancelled()) return;

        // Only continue if we should stop the spread from the config.
        if (!Config.getBool("protection", "blockFluidSpreadIntoClaims")) return;

        // If the block isn't water or lava, we don't protect it.
        Material blockType = e.getBlock().getType();
        if (blockType != Material.WATER && blockType != Material.LAVA) return;

        Chunk from = e.getBlock().getChunk();
        Chunk to = e.getToBlock().getChunk();

        // If the from and to chunks have the same owner or if the to chunk is
        // unclaimed, the flow is allowed.
        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();
        if (getChunksSameOwner(CHUNK, from, to) || !CHUNK.isClaimed(to)) return;

        // Cancel the flow
        e.setCancelled(true);
    }

    private static void handlePistonEvent(@Nonnull Block piston, @Nonnull List<Block> blocks, @Nonnull BlockFace direction, @Nonnull Cancellable e) {
        if (e.isCancelled()) return;

        // If we don't protect against pistons, no work is needed.
        if (!Config.getBool("protection", "blockPistonsIntoClaims")) return;

        Chunk pistonChunk = piston.getChunk();
        List<Chunk> blockChunks = new ArrayList<>();
        blockChunks.add(piston.getRelative(direction).getChunk());

        // Add to the list of chunks possible affected by this piston
        // extension. This list should never be >2 in size but the world 
        // is a weird place.
        for (Block block : blocks) {
            Chunk to = block.getRelative(direction).getChunk();

            boolean added = false;
            for (Chunk ablockChunk : blockChunks) {
                if (getChunksEqual(ablockChunk, to)) {
                    added = true;
                    break;
                }
            }
            if (!added) blockChunks.add(to);
        }

        // If the from and to chunks have the same owner or if the to chunk is
        // unclaimed, the piston can extend into the blockChunk.
        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();
        boolean die = false;
        for (Chunk blockChunk : blockChunks) {
            if (!getChunksSameOwner(CHUNK, pistonChunk, blockChunk) && CHUNK.isClaimed(blockChunk)) {
                die = true;
                break;
            }
        }
        if (!die) return;

        e.setCancelled(true);
    }

    public static void handlePistonExtendEvent(@Nonnull BlockPistonExtendEvent e) {
        handlePistonEvent(e.getBlock(), e.getBlocks(), e.getDirection(), e);
    }

    public static void handlePistonRetractEvent(@Nonnull BlockPistonRetractEvent e) {
        handlePistonEvent(e.getBlock(), e.getBlocks(), e.getDirection(), e);
    }

}
