package com.cjburkey.claimchunk.chunk;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DataChunk {

    /** The position of the chunk. */
    public final ChunkPos chunk;

    /** The UUID of the owning player. */
    public final UUID player;

    /** Whether TNT can explode in this chunk if TNT is disabled in the config. */
    // Assignment because I'm not sure if GSON will handle it?
    @SuppressWarnings("UnusedAssignment")
    public boolean tnt = true;

    /** The other players that have access to the chunk, and their permissions * */
    public Map<UUID, ChunkPlayerPermissions> playerPermissions;

    /**
     * Create an instance of chunk data that links a chunk's position and the owning player.
     *
     * @param chunk The position of chunk.
     * @param player The UUID of the owning player.
     * @param tnt Whether TNT is enabled in this chunk.
     */
    public DataChunk(
            ChunkPos chunk,
            UUID player,
            Map<UUID, ChunkPlayerPermissions> playerPermissions,
            boolean tnt) {
        this.chunk = chunk;
        this.player = player;
        this.playerPermissions = playerPermissions;
        this.tnt = tnt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataChunk dataChunk = (DataChunk) o;
        return Objects.equals(chunk, dataChunk.chunk) && Objects.equals(player, dataChunk.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunk, player);
    }
}
