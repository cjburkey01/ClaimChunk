package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WorldGuardPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 100;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        boolean allowedToClaimWG =
                WorldGuardHandler.isAllowedClaim(data.claimChunk(), data.chunk());
        boolean adminOverride = data.claimChunk().getConfigHandler().getAllowWGAdminOverride();
        boolean hasAdmin = Utils.hasAdmin(data.player());

        return allowedToClaimWG || (adminOverride && hasAdmin);
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk().getMessages().claimLocationBlock);
    }
}
