package com.cjburkey.claimchunk.service.prereq.claim;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MaxChunksPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 200;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        return !(data.maxClaimed > 0 && data.claimedBefore >= data.maxClaimed);
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimTooMany);
    }

}
