package com.cjburkey.claimchunk.chunk;

import java.util.UUID;

public class DataChunk {

	public final ChunkPos chunk;
	public final UUID player;
	
	public DataChunk(ChunkPos chunk, UUID player) {
		this.chunk = chunk;
		this.player = player;
	}
	
}