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
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

// TODO: TEST ALL THESE!
//       EVENT REWRITE!
public final class ChunkEventHelper {

    private static boolean getCanEdit(World world, int x, int z, UUID player) {
        // If the user is an admin, they have permission to override chunk claims.
        if (Utils.hasPerm(Bukkit.getPlayer(player), false, "admin")) {
            return true;
        }

        final ChunkHandler CHUNK = ClaimChunk.getInstance().getChunkHandler();

        // If the chunk isn't claimed, users can edit if the server hasn't
        // disabled editing in unclaimed chunks.
        if (!CHUNK.isClaimed(world, x, z)) {
            return !Config.getBool("protection", "blockUnclaimedChunks");
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

    private static void cancelEventIfNotOwned(Player ply, Chunk chunk, @Nonnull Cancellable e, String config) {
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

    public static void cancelBlockEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
        cancelEventIfNotOwned(ply, chunk, e, "blockPlayerChanges");
    }

    public static void cancelInteractionEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
        cancelEventIfNotOwned(ply, chunk, e, "blockInteractions");
    }

    public static void cancelExplosionIfConfig(@Nonnull EntityExplodeEvent e) {
        if (e.isCancelled()) return;

        final EntityType TYPE = e.getEntityType();
        final Chunk CHUNK = e.getLocation().getChunk();

        // If the chunk is claimed or the server has protection in unclaimed
        // chunks, we need to keep checking if the event should be cancelled.
        boolean inClaimedChunk = ClaimChunk.getInstance().getChunkHandler().isUnclaimed(CHUNK);
        if (!(inClaimedChunk || Config.getBool("protection", "blockUnclaimedChunks"))) return;

        // If the event is TNT/Mincart TNT related, it should be cancelled
        boolean isTnt = TYPE.equals(EntityType.PRIMED_TNT) || TYPE.equals(EntityType.MINECART_TNT);
        boolean blockTnt = Config.getBool("protection", "blockTnt");
        boolean protectTnt = isTnt && blockTnt;
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
        boolean protectCreeper = TYPE.equals(EntityType.CREEPER) && Config.getBool("protection", "blockCreeper");
        if (protectCreeper) {
            doCancelExplosionEvent(e);
            return;
        }

        // If wither damage is prevented within the config, cancel this event
        // if it's a wither event.
        boolean isWither = TYPE.equals(EntityType.WITHER) || TYPE.equals(EntityType.WITHER_SKULL);
        boolean protectWither = isWither && Config.getBool("protection", "blockWither");
        if (protectWither) {
            doCancelExplosionEvent(e);
        }
    }

    private static void doCancelExplosionEvent(EntityExplodeEvent e) {
        e.setYield(0);
        e.setCancelled(true);
    }

    public static void cancelAnimalEvent(Player ply, Entity ent, Chunk chunk, @Nonnull Cancellable e) {
        if (e.isCancelled()) return;

        // If animals aren't protected, we don't need to check if this
        // one is -_-
        if (!Config.getBool("protection", "protectAnimals")) return;

        // Admins have permission to do anything in claimed chunks.
        if (Utils.hasPerm(ply, false, "admin")) return;

        // Check if the player is able to edit in both the chunk they're in as well as the chunk the animal is in.
        final Chunk CHUNK = ent.getLocation().getChunk();
        boolean canPlayerEditEntityChunk = getCanEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId());
        boolean canPlayerEditTheirChunk = getCanEdit(CHUNK.getWorld(), CHUNK.getX(), CHUNK.getZ(), ply.getUniqueId());
        if (canPlayerEditEntityChunk && canPlayerEditTheirChunk) return;

        e.setCancelled(true);
    }

    public static void cancelCommandEvent(Player ply, Chunk chunk, @Nonnull PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) return;

        // If the user is an admin, they can run any command.
        if (Utils.hasPerm(ply, false, "admin")) return;

        // If the user has permission to edit within this chunk, they can use
        // any command they have permissions to use.
        if (getCanEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) return;

        // Get the command from the message.
        final String[] cmdAndArgs = e.getMessage()
                .trim()
                .substring(1)
                .split(" ", 1);

        if (cmdAndArgs.length == 1) {
            // Cancel the event if the blocked commands list contains this
            // command.
            if (Config.getList("protection", "blockedCmds").contains(cmdAndArgs[0])) e.setCancelled(true);
        }
    }

}
