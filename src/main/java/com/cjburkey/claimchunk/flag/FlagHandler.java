package com.cjburkey.claimchunk.flag;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

/**
 * Interface between flag data and the rest of the plugin :)
 *
 * @since 0.0.26
 */
public class FlagHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final ClaimChunk claimChunk;

    public FlagHandler(IClaimChunkDataHandler dataHandler, ClaimChunk claimChunk) {
        this.dataHandler = dataHandler;
        this.claimChunk = claimChunk;
    }
}
