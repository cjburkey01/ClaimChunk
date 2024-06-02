package com.cjburkey.claimchunk.chunk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DataChunk {

    /** The position of the chunk. */
    public final @NotNull ChunkPos chunk;

    /** The UUID of the owning player. */
    public final @NotNull UUID player;

    /** The other players that have access to the chunk, and their permissions * */
    public @NotNull Map<UUID, ChunkPlayerPermissions> playerPermissions;

    /** The default access that players will have in this chunk */
    public @Nullable ChunkPlayerPermissions defaultPermissions;

    /**
     * Create an instance of chunk data that links a chunk's position and the owning player.
     *
     * @param chunk The position of chunk.
     * @param player The UUID of the owning player.
     */
    public DataChunk(
            @NotNull ChunkPos chunk,
            @NotNull UUID player,
            @NotNull Map<UUID, ChunkPlayerPermissions> playerPermissions,
            @Nullable ChunkPlayerPermissions defaultPermissions) {
        this.chunk = chunk;
        this.player = player;
        this.playerPermissions = playerPermissions;
        this.defaultPermissions = defaultPermissions;
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
