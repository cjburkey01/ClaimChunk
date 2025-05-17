package com.cjburkey.claimchunk.api;

/**
 * Location of a chunk within some world.
 *
 * @param worldName The server name of the world containing the chunk.
 * @param x The chunk's X coordinate
 * @param y The chunk's Y coordinate
 * @since 1.0.0
 */
public record ChunkPos(String worldName, int x, int y) {
}
