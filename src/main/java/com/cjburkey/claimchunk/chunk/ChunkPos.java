package com.cjburkey.claimchunk.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.packet.ParticleHandler;

public final class ChunkPos {
	
	private final String world;
	private final int x;
	private final int z;
	
	public ChunkPos(String world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
	}
	
	public ChunkPos(Chunk chunk) {
		this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}
	
	public String getWorld() {
		return world;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public void outlineChunk(Player showTo, int timeToShow) {
		List<Location> blocksToDo = new ArrayList<>();
		World world = ClaimChunk.getInstance().getServer().getWorld(this.world);
		
		int showTimeInSeconds = Utils.clamp(timeToShow, 1, 10);
		
		int xStart = x * 16;
		int zStart = z * 16;
		int yStart = (int) showTo.getLocation().getY() - 1;
		for (int ys = 0; ys < 3; ys ++) {
			int y = yStart + ys;
			for (int i = 1; i < 16; i ++) {
				blocksToDo.add(new Location(world, xStart + i, y, zStart));
				blocksToDo.add(new Location(world, xStart + i, y, zStart + 16));
			}
			for (int i = 0; i < 17; i ++) {
				blocksToDo.add(new Location(world, xStart, y, zStart + i));
				blocksToDo.add(new Location(world, xStart + 16, y, zStart + i));
			}
		}
		
		for (Location loc : blocksToDo) {
			for (int i = 0; i < showTimeInSeconds * 2 + 1; i ++) {
				ClaimChunk.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(ClaimChunk.getInstance(), () -> {
					if (showTo != null && showTo.isOnline()) {
						ParticleHandler.spawnParticleForPlayers(loc, ParticleHandler.Particles.SMOKE_LARGE, showTo);
					}
				}, i * 10);
			}
		}
	}
	
	public String toString() {
		return world + "," + x + "," + z;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((world == null) ? 0 : world.hashCode());
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChunkPos other = (ChunkPos) obj;
		if (world == null) {
			if (other.world != null)
				return false;
		} else if (!world.equals(other.world))
			return false;
		if (x != other.x)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	public static ChunkPos fromString(String in) {
		String[] split = in.split(Pattern.quote(","));
		if(split.length == 3) {
			try {
				int x = Integer.parseInt(split[1].trim());
				int z = Integer.parseInt(split[2].trim());
				return new ChunkPos(split[0], x, z);
			} catch(Exception e) {
			}
		}
		return null;
	}
	
}