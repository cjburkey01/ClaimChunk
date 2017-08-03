package com.cjburkey.claimchunk.chunk;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.DataChunk;
import com.cjburkey.claimchunk.data.DataStorage;

public final class ChunkHandler {
	
	private final Map<ChunkPos, UUID> claimed = new ConcurrentHashMap<>();
	private final DataStorage<DataChunk> data;
	
	public ChunkHandler(File saveFile) {
		data = new DataStorage<>(DataChunk[].class, saveFile);
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
	
	public void writeToDisk() throws IOException {
		data.emptyObjects();
		for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
			data.addObject(new DataChunk(entry.getKey(), entry.getValue()));
		}
		data.write();
	}
	
	public void readFromDisk() throws IOException {
		data.read();
		claimed.clear();
		for (DataChunk c : data.getObjects()) {
			claimed.put(c.chunk, c.player);
		}
	}
	
	/*public void writeToDisk(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		FileWriter writer = null;
		StringBuilder out = new StringBuilder();
		out.append(";;-This is a comment, it is not read. It doesn't matter, though, this file is reset every time.");
		out.append("\n");
		for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
			out.append(entry.getKey().toString());
			out.append(';');
			out.append(entry.getValue().toString());
			out.append('\n');
		}
		try {
			writer = new FileWriter(file, false);
			writer.write(out.toString());
			writer.close();
		} catch (IOException e) {
			throw e;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	public boolean readFromDisk(File file) throws IOException {
		if (file.exists()) {
			long start = System.nanoTime();
			BufferedReader reader = null;
			List<String> lines = new ArrayList<>();
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
				reader.close();
				loadLines(start, lines.toArray(new String[lines.size()]));
				return true;
			} catch (FileNotFoundException e) {
				throw e;
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} else {
			Utils.err("File not found: " + file);
		}
		return false;
	}
	
	private void loadLines(long start, String[] lines) {
		claimed.clear();
		for (String line : lines) {
			if (!line.startsWith(";;-")) {
				String[] split = line.split(Pattern.quote(";"));
				if (split.length == 2) {
					ChunkPos pos = ChunkPos.fromString(split[0].trim());
					UUID ply = UUID.fromString(split[1].trim());
					if (pos != null && ply != null) {
						claimed.put(pos, ply);
					}
				}
			}
		}
		long done = System.nanoTime();
		double takenNs = (double) start / (double) done;
		double takenMs = takenNs / 1000000.0d;
		Utils.log("Read " + lines.length + " lines in " + NumberFormat.getInstance().format(takenMs) + "ms.");
	}*/
}