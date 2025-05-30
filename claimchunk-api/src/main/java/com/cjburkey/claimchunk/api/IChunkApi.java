package com.cjburkey.claimchunk.api;

import com.cjburkey.claimchunk.api.data.ClaimedChunk;
import com.cjburkey.claimchunk.api.owner.IChunkOwner;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @since 1.0.0
 */
public interface IChunkApi {

    /**
     * @param chunkPos The position of the chunk to claim for the specified owner.
     * @param chunkOwner The owner for which the given chunk should be claimed.
     * @return {@code true} if the chunk was not already claimed and is claimed now, or {@code
     *     false} if the chunk was already claimed and no changes were made.
     */
    boolean setChunkClaimed(@NotNull ChunkPos chunkPos, @NotNull IChunkOwner chunkOwner);

    /**
     * Check whether the given chunk is currently claimed.
     *
     * @param chunkPos The chunk to query.
     * @return Whether the chunk currently has an owner.
     */
    boolean isChunkClaimed(@NotNull ChunkPos chunkPos);

    /**
     * Retrieve a reference to claim & permission information for a given claimed chunk. Returns an
     * empty {@link java.util.Optional} if the chunk is not claimed.
     *
     * @param chunkPos The position of the chunk to query.
     * @return An optional value either of the claimed chunk data, or {@code empty} if the chunk is
     *     not claimed.
     */
    @NotNull Optional<ClaimedChunk> getClaimedChunk(@NotNull ChunkPos chunkPos);

    /**
     * Update the provided chunk with any changed values. This will be used to save the changed
     * data.
     *
     * @param claimedChunk The claimed chunk data to update.
     */
    void updateClaimedChunk(@NotNull ClaimedChunk claimedChunk);

    /**
     * Delete the claimed chunk information and permissions.
     *
     * @param chunkPos The position of the chunk which should be unclaimed.
     * @return {@code true} if the chunk was previously claimed and is now unclaimed, or {@code
     *     false} if the chunk was not claimed.
     */
    boolean unclaimChunk(@NotNull ChunkPos chunkPos);
}
