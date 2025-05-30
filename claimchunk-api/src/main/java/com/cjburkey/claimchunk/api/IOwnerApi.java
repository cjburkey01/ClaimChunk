package com.cjburkey.claimchunk.api;

import com.cjburkey.claimchunk.api.data.OwnerData;
import com.cjburkey.claimchunk.api.owner.IChunkOwner;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @since 1.0.0
 */
public interface IOwnerApi {

    /**
     * Add or update an owner's associated data.
     *
     * @return {@code true} if the owner didn't previously have any data stored, and {@code false}
     *     if data was already associated and has been updated. You should probably check that
     *     {@link #hasOwnerData} is false before constructing your own.
     */
    boolean addOrUpdateOwnerData(@NotNull OwnerData newPlayer);

    /**
     * Check whether the given chunk owner has data stored with ClaimChunk.
     *
     * @param chunkOwner The chunk owner to check for.
     * @return Whether the player has join and/or has data stored.
     */
    boolean hasOwnerData(@NotNull IChunkOwner chunkOwner);

    /**
     * Get the data associated with the provided chunk owner.
     *
     * @param chunkOwner The owner whose data to check for.
     * @return An optional value either of the player/owner data, or {@code empty} if the player
     *     doesn't have any data.
     */
    @NotNull Optional<OwnerData> getOwnerData(@NotNull IChunkOwner chunkOwner);
}
