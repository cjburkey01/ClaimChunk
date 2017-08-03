package com.cjburkey.claimchunk.data;

import java.util.UUID;
import com.cjburkey.claimchunk.chunk.ChunkPos;

public class DataChunk {

	public final ChunkPos chunk;
	public final UUID player;
	
	public DataChunk(ChunkPos chunk, UUID player) {
		this.chunk = chunk;
		this.player = player;
	}
	
}