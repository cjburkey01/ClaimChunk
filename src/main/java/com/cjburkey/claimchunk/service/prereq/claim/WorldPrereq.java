package com.cjburkey.claimchunk.service.prereq.claim;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return -50;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        return data.claimChunk.getProfileHandler().getProfile(data.chunk.world()).enabled;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimWorldDisabled);
    }
}
