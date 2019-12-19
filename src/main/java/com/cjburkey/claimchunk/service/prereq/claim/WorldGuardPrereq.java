package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.util.Optional;

public class WorldGuardPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 100;
    }

    @Override
    public boolean getPassed(PrereqClaimData data) {
        boolean allowedToClaimWG = WorldGuardHandler.isAllowedClaim(data.chunk);
        boolean worldAllowsClaims = !Config.getList("chunks", "disabledWorlds").contains(data.chunk.getWorld().getName());
        boolean adminOverride = Config.getBool("worldguard", "allowAdminOverride");
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        boolean hasAdmin = Utils.hasAdmin(data.player.get());

        return (worldAllowsClaims || allowedToClaimWG) && (hasAdmin && adminOverride);
    }

    @Override
    public Optional<String> getErrorMessage(PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().claimLocationBlock);
    }

}
