package com.cjburkey.claimchunk.api.owner;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a player that may own a chunk.
 *
 * @param playerId The player's Mojang-assigned unique ID.
 * @since 1.0.0
 */
public record OwnerPlayer(@NotNull UUID playerId) implements IChunkOwner {

    @Override
    public @NotNull UUID getUniqueId() {
        return this.playerId;
    }
}
