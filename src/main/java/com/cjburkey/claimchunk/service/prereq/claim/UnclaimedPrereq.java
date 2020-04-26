package com.cjburkey.claimchunk.service.prereq.claim;

import java.util.Optional;
import javax.annotation.Nonnull;

public class UnclaimedPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean getPassed(@Nonnull PrereqClaimData data) {
        return !data.claimChunk.getChunkHandler().isClaimed(data.chunk);
    }

    @Override
    public Optional<String> getErrorMessage(@Nonnull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimAlreadyOwned);
    }

}
