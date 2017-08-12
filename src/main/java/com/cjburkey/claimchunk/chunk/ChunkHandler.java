package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.IDataStorage;
import com.cjburkey.claimchunk.data.JsonDataStorage;
import com.cjburkey.claimchunk.data.SqlDataStorage;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChunkHandler {
	
	private final Map<ChunkPos, UUID> claimed = new ConcurrentHashMap<>();
	private final IDataStorage<DataChunk> data;
	
	public ChunkHandler(boolean sql, File saveFile) {
		data = (sql) ? new SqlDataStorage<>() : new JsonDataStorage<>(DataChunk[].class, saveFile);
	}
	
	/**
	 * Claims a specific chunk for a player if that chunk is not already owned.
	 * @param world The current world.
	 * @param x The chunk x-coord.
	 * @param z The chunk z-coord.
	 * @param player The player for whom to claim the chunk.
	 * @return The chunk position variable
	 * @throws IOException Data could not be saved to disk.
	 */
	public ChunkPos claimChunk(World world, int x, int z, UUID player) throws IOException {
		return claimChunk(world.getName(), x, z, player);
	}
	
	/**
	 * Claims a specific chunk for a player if that chunk is not already owned.
	 * @param world The current world.
	 * @param x The chunk x-coord.
	 * @param z The chunk z-coord.
	 * @param player The player for whom to claim the chunk.
	 * @return The chunk position variable
	 * @throws IOException Data could not be saved to disk.
	 */
	public ChunkPos claimChunk(String world, int x, int z, UUID player) throws IOException {
		if (isClaimed(ClaimChunk.getInstance().getServer().getWorld(world), x, z)) {
			return null;
		}
		ChunkPos pos = new ChunkPos(world, x, z);
		claimed.put(pos, player);
		return pos;
	}
	
	/**
	 * Unclaims a specific chunk if that chunk is currently owned.
	 * @param world The current world.
	 * @param x The chunk x-coord.
	 * @param z The chunk z-coord.
	 * @return Whether or not the chunk was unclaimed.
	 * @throws IOException Data could not be saved from disk.
	 */
	public boolean unclaimChunk(World world, int x, int z) throws IOException {
		if (!isClaimed(world, x, z)) {
			return false;
		}
		claimed.remove(new ChunkPos(world.getName(), x, z));
		return true;
	}
	
	public int getClaimed(UUID ply) {
		int i = 0;
		for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
			if (entry.getValue().equals(ply)) {
				i ++;
			}
		}
		return i;
	}
	
	public boolean isClaimed(World world, int x, int z) {
		return claimed.containsKey(new ChunkPos(world.getName(), x, z));
	}
	
	public boolean isOwner(World world, int x, int z, UUID uuid) {
		return claimed.get(new ChunkPos(world.getName(), x, z)).equals(uuid);
	}
	
	public boolean isOwner(World world, int x, int z, Player ply) {
		return isOwner(world, x, z, ply.getUniqueId());
	}
	
	public UUID getOwner(World world, int x, int z) {
		return claimed.get(new ChunkPos(world.getName(), x, z));
	}

	public boolean isClaimed(Chunk chunk) {
		return isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}

	public boolean hasChunk(UUID uniqueId) {
		for (UUID uuid : claimed.values()) {
			if (uniqueId.equals(uuid))
				return true;
		}
		return false;
	}
	
	public void writeToDisk() throws Exception {
		data.clearData();
		for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
			data.addData(new DataChunk(entry.getKey(), entry.getValue()));
		}
		data.saveData();
	}
	
	public void readFromDisk() throws Exception {
		data.reloadData();
		claimed.clear();
		for (DataChunk c : data.getData()) {
			claimed.put(c.chunk, c.player);
		}
	}
	
}