package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class ChunkEventHelper {

    private static boolean cannotEdit(World world, int x, int z, UUID player) {
        if (Utils.hasPerm(Bukkit.getPlayer(player), false, "admin")) return false;
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        PlayerHandler ph = ClaimChunk.getInstance().getPlayerHandler();
        if (!ch.isClaimed(world, x, z)) return Config.getBool("protection", "blockUnclaimedChunks");
        if (ch.isOwner(world, x, z, player)) return false;
        return !(ph.hasAccess(ch.getOwner(world, x, z), player)
                || (Config.getBool("protection", "disableOfflineProtect") && Bukkit.getPlayer(player) == null));
    }

    private static void cancelEventIfNotOwned(Player ply, Chunk chunk, Cancellable e, String config) {
        if (e != null
                && !e.isCancelled()
                && !Utils.hasPerm(ply, false, "admin")
                && Config.getBool("protection", config)
                && cannotEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) {
            e.setCancelled(true);
            Utils.toPlayer(ply, Config.errorColor(), Utils.getMsg("chunkNoEdit").replace("%%PLAYER%%",
                    ClaimChunk.getInstance().getPlayerHandler().getUsername(ClaimChunk.getInstance().getChunkHandler().getOwner(chunk))));
        }
    }

    public static void cancelBlockEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
        cancelEventIfNotOwned(ply, chunk, e, "blockPlayerChanges");
    }

    public static void cancelInteractionEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
        cancelEventIfNotOwned(ply, chunk, e, "blockInteractions");
    }

    public static void cancelExplosionIfConfig(EntityExplodeEvent e) {
        if (e == null) return;
        EntityType type = e.getEntityType();
        if (!e.isCancelled()
                && (((type.equals(EntityType.PRIMED_TNT) || type.equals(EntityType.MINECART_TNT)) && Config.getBool("protection", "blockTnt"))
                || (type.equals(EntityType.CREEPER) && Config.getBool("protection", "blockCreeper")))) {
            e.setYield(0);
            e.setCancelled(true);
        }
    }

    public static void cancelEntityEvent(Player ply, Entity ent, Chunk chunk, Cancellable e) {
        Chunk entChunk = ent.getLocation().getChunk();
        if (e != null
                && !e.isCancelled()
                && !Utils.hasPerm(ply, false, "admin")
                && Config.getBool("protection", "protectAnimals")
                && (cannotEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())
                || cannotEdit(entChunk.getWorld(), entChunk.getX(), entChunk.getZ(), ply.getUniqueId()))) {
            e.setCancelled(true);
        }
    }

    public static void cancelCommandEvent(Player ply, Chunk chunk, PlayerCommandPreprocessEvent e) {
        if (e != null
                && !e.isCancelled()
                && !Utils.hasPerm(ply, false, "admin")
                && cannotEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) {
            final String[] cmds = e.getMessage()
                    .trim()
                    .substring(1)
                    .split(" ", 1);
            if (cmds.length == 1) {
                if (Config.getList("protection", "blockedCmds").contains(cmds[0])) e.setCancelled(true);
            }
        }
    }

}
