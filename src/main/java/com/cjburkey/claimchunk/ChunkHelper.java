package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class ChunkHelper {

    private static boolean cannotEdit(World world, int x, int z, UUID player) {
        if (Bukkit.getPlayer(player).hasPermission("claimchunk.admin")) {
            return false;
        }
        ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
        PlayerHandler ph = ClaimChunk.getInstance().getPlayerHandler();
        if (!ch.isClaimed(world, x, z)) {
            return Config.getBool("protection", "blockUnclaimedChunks");
        }
        if (ch.isOwner(world, x, z, player)) {
            return false;
        }
        return !ph.hasAccess(ch.getOwner(world, x, z), player);
    }

    public static void cancelEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
        if (ply.hasPermission("claimchunk.admin")) {
            return;
        }
        if (Config.getBool("protection", "blockPlayerChanges")) {
            if (!e.isCancelled()) {
                if (cannotEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) {
                    e.setCancelled(true);
                    Utils.toPlayer(ply, Config.getColor("errorColor"), Utils.getMsg("chunkNoEdit"));
                }
            }
        }
    }

    public static void cancelExplosionIfConfig(EntityExplodeEvent e) {
        EntityType type = e.getEntityType();
        if (type.equals(EntityType.PRIMED_TNT) && Config.getBool("protection", "blockTnt")) {
            e.setYield(0);
            e.setCancelled(true);
        } else if (type.equals(EntityType.CREEPER) && Config.getBool("protection", "blockCreeper")) {
            e.setYield(0);
            e.setCancelled(true);
        }
    }

    public static void cancelAnimalDamage(Player damager, Chunk chunk, EntityDamageByEntityEvent e) {
        if (damager.hasPermission("claimchunk.admin"))
            return;
        if (Config.getBool("protection", "protectAnimals")) {
            if (cannotEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), damager.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

}
