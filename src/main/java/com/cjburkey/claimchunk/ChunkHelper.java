package com.cjburkey.claimchunk;

import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityExplodeEvent;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.player.PlayerHandler;

public final class ChunkHelper {
	
	public static boolean canEdit(World world, int x, int z, UUID player) {
		ChunkHandler ch = ClaimChunk.getInstance().getChunkHandler();
		PlayerHandler ph = ClaimChunk.getInstance().getPlayerHandler();
		if (!ch.isClaimed(world, x, z)) {
			return true;
		}
		if (ch.isOwner(world, x, z, player)) {
			return true;
		}
		if (ph.hasAccess(ch.getOwner(world, x, z), player)) {
			return true;
		}
		return false;
	}
	
	public static void cancelEventIfNotOwned(Player ply, Chunk chunk, Cancellable e) {
		if (Config.getBool("protection", "blockPlayerChanges")) {
			if (!e.isCancelled()) {
				if (!canEdit(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply.getUniqueId())) {
					e.setCancelled(true);
					Utils.toPlayer(ply, Config.getColor("errorColor"), Utils.getMsg("chunkNoEdit"));
				}
			}
		}
	}
	
	public static void cancelExplosionIfConfig(EntityExplodeEvent e) {
		EntityType type = e.getEntityType();
		if(type.equals(EntityType.PRIMED_TNT) && Config.getBool("protection", "blockTnt")) {
			e.setYield(0);
			e.setCancelled(true);
		} else if(type.equals(EntityType.CREEPER) && Config.getBool("protection", "blockCreeper")) {
			e.setYield(0);
			e.setCancelled(true);
		}
	}
	
}