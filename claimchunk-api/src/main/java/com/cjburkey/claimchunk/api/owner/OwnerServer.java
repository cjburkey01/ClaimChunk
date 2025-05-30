package com.cjburkey.claimchunk.api.owner;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a "server" (operator) owner.
 *
 * @since 1.0.0
 */
public record OwnerServer() implements IChunkOwner {

    /**
     * Single static instance of the server owning chunks. This is for literally almost no level of
     * optimization.
     */
    public static final OwnerServer OWNER_SERVER = new OwnerServer();

    @Override
    public @NotNull UUID getUniqueId() {
        return new UUID(0, 0);
    }
}
