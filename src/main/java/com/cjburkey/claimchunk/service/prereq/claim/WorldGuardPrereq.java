package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.util.Optional;
import javax.annotation.Nonnull;

public class WorldGuardPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 100;
    }

    @Override
    public boolean getPassed(@Nonnull PrereqClaimData data) {
        boolean allowedToClaimWG = WorldGuardHandler.isAllowedClaim(data.claimChunk, data.chunk);
        boolean worldAllowsClaims = !data.claimChunk.chConfig()
                .getList("chunks", "disabledWorlds")
                .contains(data.chunk.getWorld().getName());
        boolean adminOverride = data.claimChunk.chConfig()
                .getBool("worldguard", "allowAdminOverride");
        boolean hasAdmin = Utils.hasAdmin(data.player);

        // This can be simplified but it works and I'm feeling lazy
        // I promise I'll get around to it
        return !(!(worldAllowsClaims || (hasAdmin && adminOverride)) || !(allowedToClaimWG || (hasAdmin && adminOverride)));
    }

    @Override
    public Optional<String> getErrorMessage(@Nonnull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimLocationBlock);
    }

}
