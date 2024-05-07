package com.cjburkey.claimchunk.data.journaled;

import static com.cjburkey.claimchunk.data.journaled.ClaimRegion.CLAIM_REGION_WIDTH;

import com.cjburkey.claimchunk.chunk.ChunkPos;

import java.util.Objects;

/**
 * The position of a region, guaranteed to be within the 0 < p < {@code CLAIM_REGION_WIDTH}.
 *
 * @since 0.0.25
 */
public class RegionInnerPos {

    /** The X position of the region. */
    public final int x;

    /** The Y position of the region. */
    public final int z;

    public RegionInnerPos(int chunkX, int chunkZ) {
        this.x = Math.floorMod(chunkX, CLAIM_REGION_WIDTH);
        this.z = Math.floorMod(chunkZ, CLAIM_REGION_WIDTH);
    }

    public RegionInnerPos(ChunkPos chunkPos) {
        this(chunkPos.x(), chunkPos.z());
    }

    /**
     * @return the unique index for this position in a {@code CLAIM_REGION_WIDTH}^2 sized array.
     */
    public int index() {
        return x * CLAIM_REGION_WIDTH + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionInnerPos that = (RegionInnerPos) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
