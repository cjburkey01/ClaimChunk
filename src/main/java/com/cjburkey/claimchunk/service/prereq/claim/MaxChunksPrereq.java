package com.cjburkey.claimchunk.service.prereq.claim;

import java.util.Optional;

public class MaxChunksPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 200;
    }

    @Override
    public boolean getPassed(PrereqClaimData data) {
        return !(data.maxClaimed > 0 && data.claimedBefore >= data.maxClaimed);
    }

    @Override
    public Optional<String> getErrorMessage(PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimTooMany);
    }

}
