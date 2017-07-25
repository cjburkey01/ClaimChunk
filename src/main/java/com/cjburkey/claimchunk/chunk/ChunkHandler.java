package com.cjburkey.claimchunk.chunk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;

public final class ChunkHandler {
	
	private final Map<ChunkPos, UUID> claimed = new ConcurrentHashMap<>();
	
	/**
	 * Claims a specific chunk for a player if that chunk is not already owned.
	 * @param world The current world.
	 * @param x The chunk x-coord.
	 * @param z The chunk z-coord.
	 * @param player The player for whom to claim the chunk.
	 * @return Whether or not the chunk was claimed.
	 */
	public boolean claimChunk(World world, int x, int z, Player player) {
		if (isClaimed(world, x, z)) {
			return false;
		}
		claimed.put(new ChunkPos(world.getName(), x, z), player.getUniqueId());
		reload();
		return true;
	}
	
	/**
	 * Unclaims a specific chunk if that chunk is currently owned.
	 * @param world The current world.
	 * @param x The chunk x-coord.
	 * @param z The chunk z-coord.
	 * @return Whether or not the chunk was unclaimed.
	 */
	public boolean unclaimChunk(World world, int x, int z) {
		if (!isClaimed(world, x, z)) {
			return false;
		}
		claimed.remove(new ChunkPos(world.getName(), x, z));
		reload();
		return true;
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
	
	public void reload() {
		try {
			writeToDisk(ClaimChunk.getInstance().getChunkFile());
			readFromDisk(ClaimChunk.getInstance().getChunkFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToDisk(File file) throws IOException {
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
	
	// 1000000000ns = 1s
	// 1000ms = 1s
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
	}
	
}