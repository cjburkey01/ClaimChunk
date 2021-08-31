package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PermissionPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        // Try to check permissions as early as possible
        return -100;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        return Utils.hasPerm(data.player, true, "claim");
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimNoPerm);
    }
}
