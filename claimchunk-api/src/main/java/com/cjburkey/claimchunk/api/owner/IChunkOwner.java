package com.cjburkey.claimchunk.api.owner;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents someone (or something) that owns (or can own) a chunk.
 *
 * @since 1.0.0
 */
public sealed interface IChunkOwner permits OwnerPlayer, OwnerServer {

    /**
     * Get the unique identifier for a given owner.
     *
     * @return The unique ID.
     */
    @NotNull UUID getUniqueId();

    /**
     * Get a generic object referencing the given unique ID to specify as a chunk owner.
     *
     * @param uniqueId The unique identifier for this chunk owner.
     * @return A generic owner for chunks.
     */
    static @NotNull IChunkOwner fromUuid(@NotNull UUID uniqueId) {
        if (uniqueId.getMostSignificantBits() == 0 && uniqueId.getLeastSignificantBits() == 0) {
            return OwnerServer.OWNER_SERVER;
        } else {
            return new OwnerPlayer(uniqueId);
        }
    }
}
