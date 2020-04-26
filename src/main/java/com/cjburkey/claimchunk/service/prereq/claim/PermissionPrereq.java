package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import java.util.Optional;
import javax.annotation.Nonnull;

public class PermissionPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        // Try to check permissions as early as possible
        return -100;
    }

    @Override
    public boolean getPassed(@Nonnull PrereqClaimData data) {
        return Utils.hasPerm(data.player, true, "claim");
    }

    @Override
    public Optional<String> getErrorMessage(@Nonnull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimNoPerm);
    }

}
