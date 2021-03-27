package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Optional;

public class WorldPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return -50;
    }

    @Override
    public boolean getPassed(@Nonnull @NotNull PrereqClaimData data) {
        return data.claimChunk.getProfileManager().getProfile(data.chunk.getWorld().getName()).enabled;
    }

    @Override
    public Optional<String> getErrorMessage(@Nonnull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimWorldDisabled);
    }

}
