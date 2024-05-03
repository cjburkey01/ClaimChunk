package com.cjburkey.claimchunk.data.journaled;

import static com.cjburkey.claimchunk.data.journaled.ClaimRegion.CLAIM_REGION_WIDTH;

import com.cjburkey.claimchunk.chunk.ChunkPos;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The position of a claim data region within a world.
 *
 * @since 0.0.24
 */
public record RegionPos(String worldName, int x, int y) {

    public RegionPos(@NotNull ChunkPos chunkPos) {
        this(
                chunkPos.world(),
                Math.floorDiv(chunkPos.x(), CLAIM_REGION_WIDTH),
                Math.floorDiv(chunkPos.z(), CLAIM_REGION_WIDTH));
    }

    @Override
    public String toString() {
        return String.format("%s, %s in %s", x, y, worldName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionPos regionPos = (RegionPos) o;
        return x == regionPos.x
                && y == regionPos.y
                && Objects.equals(worldName, regionPos.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y);
    }
}
