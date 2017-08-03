package com.cjburkey.claimchunk.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.player.PlayerHandler;

public class DataConversion {
	
	public static void check(File chunk, File cache, File access, File names, ClaimChunk c) throws Exception {
		Utils.log("Checking for old data...");
		if (chunk.exists()) {
			convertChunks(chunk, c.getChunkHandler());
		}
		if (cache.exists()) {
			convertCache(cache, c.getPlayerHandler());
		}
		if (access.exists()) {
			convertAccess(access, c.getPlayerHandler());
		}
		if (names.exists()) {
			convertNames(names, c.getPlayerHandler());
		}
	}
	
	private static void convertChunks(File file, ChunkHandler handler) throws IOException {
		Utils.log("Updating chunks.");
		readChunks(file, handler);
		file.delete();
	}
	
	private static void convertCache(File file, PlayerHandler handler) {
		Utils.log("Updating cache.");
	}
	
	private static void convertAccess(File file, PlayerHandler handler) {
		Utils.log("Updating access.");
	}
	
	private static void convertNames(File file, PlayerHandler handler) {
		Utils.log("Updating names.");
	}
	
	private static void readChunks(File file, ChunkHandler handler) throws IOException {
		if (file.exists()) {
			BufferedReader reader = null;
			List<String> lines = new ArrayList<>();
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}
				reader.close();
				loadChunkLines(lines.toArray(new String[lines.size()]), handler);
				return;
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
	}
	
	private static void loadChunkLines(String[] lines, ChunkHandler handler) throws IOException {
		for (String line : lines) {
			if (!line.startsWith(";;-")) {
				String[] split = line.split(Pattern.quote(";"));
				if (split.length == 2) {
					ChunkPos pos = ChunkPos.fromString(split[0].trim());
					UUID ply = UUID.fromString(split[1].trim());
					if (pos != null && ply != null) {
						handler.claimChunk(pos.getWorld(), pos.getX(), pos.getZ(), ply);
					}
				}
			}
		}
	}
	
}