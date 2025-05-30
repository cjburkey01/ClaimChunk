package com.cjburkey.claimchunk.chunk;

import org.bukkit.Chunk;

import java.util.Objects;

public record ChunkPos(String world, int x, int z) {

    /**
     * Create an instance of a chunk position from Spigot's chunk position representation.
     *
     * @param chunk The Spigot chunk representation.
     */
    public ChunkPos(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    /**
     * Helper method to get a chunk north, relative to this one.
     *
     * @since 0.0.23
     */
    public ChunkPos north() {
        return new ChunkPos(world, x, z - 1);
    }

    /**
     * Helper method to get a chunk south, relative to this one.
     *
     * @since 0.0.23
     */
    public ChunkPos south() {
        return new ChunkPos(world, x, z + 1);
    }

    /**
     * Helper method to get a chunk east, relative to this one.
     *
     * @since 0.0.23
     */
    public ChunkPos east() {
        return new ChunkPos(world, x - 1, z);
    }

    /**
     * Helper method to get a chunk west, relative to this one.
     *
     * @since 0.0.23
     */
    public ChunkPos west() {
        return new ChunkPos(world, x + 1, z);
    }

    @Override
    public String toString() {
        return String.format("%s, %s in %s", x, z, world);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x && z == chunkPos.z && Objects.equals(world, chunkPos.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}
