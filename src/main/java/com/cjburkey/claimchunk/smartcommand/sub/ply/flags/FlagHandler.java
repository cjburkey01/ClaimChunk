package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

public class FlagHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final ClaimChunk claimChunk;

    public FlagHandler(IClaimChunkDataHandler dataHandler, ClaimChunk claimChunk) {
        this.dataHandler = dataHandler;
        this.claimChunk = claimChunk;
    }
}
