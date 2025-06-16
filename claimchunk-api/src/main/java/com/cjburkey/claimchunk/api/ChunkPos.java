package com.cjburkey.claimchunk.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Location of a chunk within some world.
 *
 * @param worldId The unique identifier for the world containing this chunk.
 * @param x The chunk's X coordinate.
 * @param y The chunk's Y coordinate.
 * @since 1.0.0
 */
public record ChunkPos(@NotNull UUID worldId, int x, int y) {}
