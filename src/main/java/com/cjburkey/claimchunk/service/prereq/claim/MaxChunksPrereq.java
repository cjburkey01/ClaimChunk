package com.cjburkey.claimchunk.service.prereq.claim;

import java.util.Optional;

public class MaxChunksPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 200;
    }

    @Override
    public boolean getPassed(PrereqClaimData data) {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        int max = data.claimChunk.getRankHandler().getMaxClaimsForPlayer(data.player.get());
        return !(max > 0 && data.claimChunk.getChunkHandler().getClaimed(data.playerId) >= max);
    }

    @Override
    public Optional<String> getErrorMessage(PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimTooMany);
    }

}
