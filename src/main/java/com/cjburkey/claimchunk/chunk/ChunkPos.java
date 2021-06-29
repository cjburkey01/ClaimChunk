package com.cjburkey.claimchunk.chunk;

import java.util.Objects;
import org.bukkit.Chunk;

import javax.annotation.Nonnull;

/**
 * A class representing the position of some chunk in a given world.
 */
public final class ChunkPos {

    /**
     * The name of the world that contains this chunk
     */
    private final String world;

    /**
     * The x-coordinate (in chunk coordinates) of this chunk.
     */
    private final int x;

    /**
     * The z-coordinate (in chunk coordinates) of this chunk.
     */
    private final int z;

    /**
     * Create an instance of a chunk position from raw data.
     *
     * @param world The name of the world that this chunk is in.
     * @param x     The x-coordinate of this chunk (in chunk coordinates).
     * @param z     The y-coordinate of this chunk (in chunk coordinates).
     */
    public ChunkPos(@Nonnull String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    /**
     * Create an instance of a chunk position from Spigot's chunk position
     * representation.
     *
     * @param chunk The Spigot chunk representation.
     */
    public ChunkPos(@Nonnull Chunk chunk) {
        // Make sure
        this(Objects.requireNonNull(chunk.getWorld()).getName(), chunk.getX(), chunk.getZ());
    }

    /**
     * Get the name of the world that contains this chunk.
     *
     * @return The world name of this chunk.
     */
    public String getWorld() {
        return world;
    }

    /**
     * Get the x-coordinate of this chunk in chunk coordinates.
     *
     * @return The x-coordinate of this chunk (in chunk coordinates).
     */
    public int getX() {
        return x;
    }

    /**
     * Get the y-coordinate of this chunk in chunk coordinates.
     *
     * @return The y-coordinate of this chunk (in chunk coordinates).
     */
    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("Chunk { %s,%s in %s }", x, z, world);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x &&
                z == chunkPos.z &&
                Objects.equals(world, chunkPos.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

}
