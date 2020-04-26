package com.cjburkey.claimchunk.service.prereq.claim;

import java.util.Optional;
import javax.annotation.Nonnull;

public class MaxChunksPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 200;
    }

    @Override
    public boolean getPassed(@Nonnull PrereqClaimData data) {
        int max = data.claimChunk.getRankHandler().getMaxClaimsForPlayer(data.player);
        return !(max > 0 && data.claimChunk.getChunkHandler().getClaimed(data.playerId) >= max);
    }

    @Override
    public Optional<String> getErrorMessage(@Nonnull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimTooMany);
    }

}
