package com.cjburkey.claimchunk.config;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ClaimChunkWorldProfileManager {

    private final HashMap<String, ClaimChunkWorldProfile> profiles = new HashMap<>();

    public @Nullable ClaimChunkWorldProfile getProfile(String worldName) {
        return profiles.containsKey(worldName)
                       ? profiles.get(worldName)
                       : createDefaultProfile(worldName);
    }

    // TODO: MAYBE CHANGE DEFAULTS?
    private ClaimChunkWorldProfile createDefaultProfile(String worldName) {
        return ClaimChunkWorldProfile.newFromWorld(worldName)
                                     .setBlocksListAllow(true)
                                     .setEntitiesListAllow(true)
                                     .setTreatUnclaimedLikeUnownedPlayers(false)
                                     .build();
    }

}
