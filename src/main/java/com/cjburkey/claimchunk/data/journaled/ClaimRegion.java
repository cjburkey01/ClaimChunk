package com.cjburkey.claimchunk.data.journaled;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A region of chunk claiming data within the world. Each region is a 16x16 array of claim data for
 * each chunk within it.
 *
 * @since 0.0.25
 */
public class ClaimRegion {

    public static final int CLAIM_REGION_WIDTH = 16;

    public final RegionPos regionPos;
    private final DataChunk[] claims;

    public ClaimRegion(@NotNull RegionPos regionPos) {
        this.regionPos = regionPos;
        this.claims = new DataChunk[CLAIM_REGION_WIDTH * CLAIM_REGION_WIDTH];
    }

    /**
     * Retrieve the claim information for a given chunk within this region.
     *
     * @param posInRegion The position within the region.
     * @return The chunk for the given position, or {@code null} if no data exists for it.
     */
    public @Nullable DataChunk getClaimInfo(RegionInnerPos posInRegion) {
        return claims[posInRegion.index()];
    }

    /**
     * Assign the chunk data at the given position within this region to the provided new chunk
     * data.
     *
     * @param newChunk The ClaimChunk information to insert into this region.
     * @return The previous chunk, or {@code null} if no information exists for the given claim OR
     *     the provided chunk does not exist within this region, i.e. a chunk is being inserted into
     *     the wrong region, given its position within the world.
     */
    public @Nullable DataChunk setClaimInfo(@NotNull DataChunk newChunk) {
        ChunkPos chunkPos = newChunk.chunk;
        // Return null if the provided chunk is outside of this region.
        if (!regionPos.equals(new RegionPos(chunkPos))) {
            Utils.warn("Uh oh, the provided chunk at %s isn't within the region %s".formatted(chunkPos, regionPos));
            return null;
        }
        return setClaimInfo(new RegionInnerPos(chunkPos), newChunk);
    }

    // Unsafe version of above methods.
    // May be exposed later if necessary, but everything should be possible through the safer API
    private @Nullable DataChunk setClaimInfo(
            @NotNull RegionInnerPos chunkPos, @Nullable DataChunk newChunk) {
        int chunkIndex = chunkPos.index();
        DataChunk previousChunk = claims[chunkIndex];
        claims[chunkIndex] = newChunk;
        return previousChunk;
    }

    /**
     * @param chunkPos The position of the chunks whose data we need to remove.
     * @return The previous data at this position, or {@code null} if no data existed.
     */
    public @Nullable DataChunk removeClaimInfo(@NotNull RegionInnerPos chunkPos) {
        return setClaimInfo(chunkPos, null);
    }
}
