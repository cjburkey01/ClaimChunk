package com.cjburkey.claimchunk.api.data;

import com.cjburkey.claimchunk.api.ChunkPos;
import com.cjburkey.claimchunk.api.owner.IChunkOwner;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Information associated with a claimed chunk at some point in time.
 *
 * <p>This class's members <b>may not refresh automatically</b> if the data is changed somewhere
 * else. If you rely on new data, you should query again from the {@link
 * com.cjburkey.claimchunk.api.IChunkApi#getClaimedChunk} method when you need it.
 *
 * @since 1.0.0
 */
public class ClaimedChunk {

    /** The position of the claimed chunk. */
    public final @NotNull ChunkPos chunkPos;

    /** The owner for this chunk */
    public @NotNull IChunkOwner chunkOwner;

    public ClaimedChunk(@NotNull ChunkPos chunkPos, @NotNull IChunkOwner chunkOwner) {
        this.chunkPos = chunkPos;
        this.chunkOwner = chunkOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClaimedChunk that)) return false;
        return Objects.equals(chunkPos, that.chunkPos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chunkPos);
    }
}
