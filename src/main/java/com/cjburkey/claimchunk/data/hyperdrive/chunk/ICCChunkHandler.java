package com.cjburkey.claimchunk.data.hyperdrive.chunk;

import com.cjburkey.claimchunk.chunk.ChunkPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * An interface representing a ClaimChunk chunk handler.
 *
 * @since 0.1.0
 */
@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted"})
public interface ICCChunkHandler {

    /**
     * Sets or updates the given chunk's owner to the new provided owner.
     *
     * @param chunkPos The chunk to update (will not be null).
     * @param newOwner The UUID of the new owner of the chunk; if this is {@code null}, the chunk should be marked as unclaimed.
     * @return An optional with the given chunk's previous owner's UUID, or empty if the chunk was previously unclaimed.
     * @since 0.1.0
     */
    @Nonnull Optional<UUID> setOwner(@Nonnull ChunkPos chunkPos, @Nullable UUID newOwner);

    /**
     * Checks if the given chunk has an owner.
     *
     * @param chunkPos The chunk to query (will not be null).
     * @return Whether the given chunk currently has an owner (whether this chunk is claimed).
     * @since 0.1.0
     */
    boolean getHasOwner(@Nonnull ChunkPos chunkPos);

    /**
     * Retrieves the current owner of the given chunk.
     *
     * @param chunkPos The chunk to query (will not be null).
     * @return An optional with the given chunk's owner's UUID, or empty if the chunk is unclaimed.
     * @since 0.1.0
     */
    @Nonnull Optional<UUID> getOwner(@Nonnull ChunkPos chunkPos);

    /**
     * Checks whether the given player is the owner of the given chunk.
     *
     * @param chunkPos The chunk to check (will not be null).
     * @param player The player (will not be null).
     * @return Whether the given player has claimed the given chunk.
     * @since 0.1.0
     */
    default boolean isOwner(@Nonnull ChunkPos chunkPos, @Nonnull UUID player) {
        return Objects.equals(getOwner(chunkPos).orElse(null), player);
    }

    /**
     * Retrieves the total number of claims across all worlds that the given
     * player has.
     *
     * @param owner The chunk(s) owner (will not be null).
     * @return An integer representing the number of claims the given player has.
     * @since 0.1.0
     */
    int getClaimedChunksCount(@Nonnull UUID owner);

    /**
     * Retrieves a collection of the positions of all of the chunks that the
     * given player has claimed across all worlds.
     *
     * @param owner The chunk(s) owner (will not be null).
     * @return A non-null collection of the claimed chunks' positions.
     * @since 0.1.0
     */
    @Nonnull Collection<ChunkPos> getClaimedChunks(@Nonnull UUID owner);

}
