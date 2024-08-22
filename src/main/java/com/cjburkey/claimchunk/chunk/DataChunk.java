package com.cjburkey.claimchunk.chunk;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @param chunk The position of the chunk.
 * @param player The UUID of the owning player.
 * @param defaultFlags Which flags the owner has granted to other players.
 * @param specificFlags Flags that the owner has granted to specific other users.
 */
public record DataChunk(
        @NotNull ChunkPos chunk,
        @NotNull UUID player,
        @NotNull HashMap<String, Boolean> defaultFlags,
        @NotNull HashMap<UUID, HashMap<String, Boolean>> specificFlags) {
    public DataChunk(@NotNull ChunkPos chunk, @NotNull UUID player) {
        this(chunk, player, new HashMap<>(), new HashMap<>());
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
