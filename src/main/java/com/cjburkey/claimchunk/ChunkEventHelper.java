package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

// TODO: TEST ALL OF THESE!
//       EVENT REWRITE!
public final class ChunkEventHelper {

    private static boolean getCanEdit(@Nonnull World world, int x, int z, @Nonnull UUID player) {
        // If the user is an admin, they have permission to override chunk claims.
        if (Utils.hasAdmin(Bukkit.getPlayer(player))) {
            return true;
        }

        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();

        // If the chunk isn't claimed, users can't edit if the server has
        // protections in unclaiemd chunks.
        if (!CHUNK.isClaimed(world, x, z) && Config.getBool("protection", "blockUnclaimedChunks")) {
            return false;
        }

        // If the player is the owner, they can edit it. Obviously.
        if (CHUNK.isOwner(world, x, z, player)) {
            return true;
        }

        final PlayerHandler PLY = ClaimChunk.getInstance().getPlayerHandler();

        // Check if the chunk is owned by an offline player and if players
        // should be allowed to edit in chunks with offline owners.
        boolean isOfflineAndUnprotected = Config.getBool("protection", "disableOfflineProtect") && Bukkit.getPlayer(player) == null;

        // If the player has access or if the server allows editing offline
        // players' chunks, this player can edit.
        return PLY.hasAccess(CHUNK.getOwner(world, x, z), player) || isOfflineAndUnprotected;
    }

    private static void cancelEventIfNotOwned(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull Cancellable e, String config) {
        if (e.isCancelled()) return;

        // Check if this isn't protected within the config.
        if (!Config.getBool("protection", config)) return;

        // If the user is permitted to edit here, then they bypass protections.
        if (getCanEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) return;

        // Cancel the event
        e.setCancelled(true);

        // Display cancellation message
        String username = ClaimChunk.getInstance().getPlayerHandler().getUsername(ClaimChunk.getInstance().getChunkHandler().getOwner(chunk));
        Utils.toPlayer(ply, ClaimChunk.getInstance().getMessages().chunkNoEdit.replace("%%PLAYER%%", username));
    }

    public static void cancelBlockEventIfNotOwned(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull Cancellable e) {
        cancelEventIfNotOwned(ply, chunk, e, "blockPlayerChanges");
    }

    public static void cancelInteractionEventIfNotOwned(@Nonnull Player player, @Nonnull Chunk chunk, @Nonnull Cancellable e) {
        cancelEventIfNotOwned(player, chunk, e, "blockInteractions");
    }

    private static void doCancelExplosionEvent(@Nonnull EntityExplodeEvent e) {
        e.setYield(0);
        e.setCancelled(true);
    }

    public static void cancelExplosionIfConfig(@Nonnull EntityExplodeEvent e) {
        if (e.isCancelled()) return;

        final EntityType TYPE = e.getEntityType();
        final Chunk CHUNK = e.getLocation().getChunk();

        // If the chunk is claimed or the server has protection in unclaimed
        // chunks, we need to keep checking if the event should be cancelled.
        boolean inClaimedChunk = ClaimChunk.getInstance().getChunkHandler().isClaimed(CHUNK);
        if (!(inClaimedChunk || Config.getBool("protection", "blockUnclaimedChunks"))) return;

        // If the event is TNT/Mincart TNT related, it should be cancelled
        boolean isTnt = TYPE == EntityType.PRIMED_TNT || TYPE == EntityType.MINECART_TNT;
        boolean protectTnt = isTnt && Config.getBool("protection", "blockTnt");
        // If TNT is blocked, check if the user has used `/chunk tnt` to enable
        // it in this chunk.
        if (protectTnt && !ClaimChunk.getInstance().getChunkHandler().isTntEnabled(CHUNK)) {
            protectTnt = false;
        }
        if (protectTnt) {
            doCancelExplosionEvent(e);
            return;
        }

        // Cancel crepper explosions if protection against them is enabled
        // within the config.
        if (TYPE == EntityType.CREEPER && Config.getBool("protection", "blockCreeper")) {
            doCancelExplosionEvent(e);
            return;
        }

        // If wither damage is prevented within the config, cancel this event
        // if it's a wither event.
        boolean isWither = TYPE == EntityType.WITHER || TYPE == EntityType.WITHER_SKULL;
        if (isWither && Config.getBool("protection", "blockWither")) {
            doCancelExplosionEvent(e);
        }
    }

    public static void cancelEntityEvent(@Nonnull Player ply, @Nonnull Entity ent, @Nonnull Chunk chunk, @Nonnull Cancellable e) {
        if (e.isCancelled()) return;

        // If entities aren't protected, we don't need to check if this
        // one is -_-
        if (!Config.getBool("protection", "protectEntities")) return;

        // Admins have permission to do anything in claimed chunks.
        if (Utils.hasAdmin(ply)) return;

        // Check if the player is able to edit in both the chunk they're in as
        // well as the chunk the animal is in.
        final Chunk CHUNK = ent.getLocation().getChunk();
        boolean canPlayerEditEntityChunk = getCanEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId());
        boolean canPlayerEditTheirChunk = getCanEdit(CHUNK.getWorld(), CHUNK.getX(), CHUNK.getZ(), ply.getUniqueId());
        if (canPlayerEditEntityChunk && canPlayerEditTheirChunk) return;

        e.setCancelled(true);
    }

    public static void cancelEntityDamageEvent(@Nonnull EntityDamageByEntityEvent e) {
        // This is actually checked in the `cancelEntityEvent` method too, but
        // it's probably better not to do all these checks if the event is
        // already cancelled.
        if (e.isCancelled()) return;

        final Entity ENTITY = e.getEntity();
        final Entity DAMAGER = e.getDamager();

        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();

        // If neither the chunk that the entity is in nor the chunk the player
        // is in is claimed, we don't need to protected the entity.
        if (!CHUNK.isClaimed(ENTITY.getLocation().getChunk()) && !CHUNK.isClaimed(DAMAGER.getLocation().getChunk())) {
            return;
        }

        Player damagingPlayer = null;
        if (ENTITY.getType() == EntityType.PLAYER) {
            // The player is directly causing damage.
            damagingPlayer = (Player) ENTITY;
        } else if (ENTITY instanceof Projectile) {
            // This is an arrow or some projectile that causes damage.
            Projectile projectileEntity = (Projectile) ENTITY;

            // If this projectile was launched by a player (like a bow from an
            // arrow), the shooter is the player.
            if (projectileEntity.getShooter() instanceof Player) {
                damagingPlayer = (Player) projectileEntity.getShooter();
            }
        }
        // The damager was not a player, so we don't need to protect the
        // entity.
        if (damagingPlayer == null) return;

        // If PvP is disabled, all entities (including players) are protected.
        // If PvP is enabled, all entities except players are protected.
        if (Config.getBool("protection", "blockPvp") || ENTITY.getType() != EntityType.PLAYER) {
            cancelEntityEvent(damagingPlayer, ENTITY, DAMAGER.getLocation().getChunk(), e);
        }
    }

    public static void cancelCommandEvent(@Nonnull Player ply, @Nonnull Chunk chunk, @Nonnull PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;

        // If the user is an admin, they can run any command.
        if (Utils.hasAdmin(ply)) return;

        // If the user has permission to edit within this chunk, they can use
        // any command they have permissions to use.
        if (getCanEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) return;

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

}
