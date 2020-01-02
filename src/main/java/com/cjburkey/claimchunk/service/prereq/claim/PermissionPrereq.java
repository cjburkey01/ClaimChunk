package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import java.util.Optional;

public class PermissionPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        // Try to check permissions as early as possible
        return -100;
    }

    @Override
    public boolean getPassed(PrereqClaimData data) {
        return data.player.filter(player -> Utils.hasPerm(player, true, "claim")).isPresent();
    }

    @Override
    public Optional<String> getErrorMessage(PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimNoPerm);
    }

}
