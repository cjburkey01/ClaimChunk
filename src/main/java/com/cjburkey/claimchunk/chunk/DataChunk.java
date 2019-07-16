package com.cjburkey.claimchunk.chunk;

import java.util.Objects;
import java.util.UUID;

public class DataChunk {

    public final ChunkPos chunk;
    public final UUID player;

    public DataChunk(ChunkPos chunk, UUID player) {
        this.chunk = chunk;
        this.player = player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataChunk dataChunk = (DataChunk) o;
        return Objects.equals(chunk, dataChunk.chunk) &&
                Objects.equals(player, dataChunk.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunk, player);
    }

}
