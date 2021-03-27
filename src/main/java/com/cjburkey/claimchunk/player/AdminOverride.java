package com.cjburkey.claimchunk.player;

import java.util.HashSet;
import java.util.UUID;

public class AdminOverride {

    // Use a HashSet so we don't have to iterate over each player in the list
    private final HashSet<UUID> overriders = new HashSet<UUID>();

    /**
     * Toggles whether the given player has permission to override admin claims.
     *
     * @param admin The player's unique ID.
     * @return Whether the player *now has* access to admin override
     */
    public boolean toggle(UUID admin) {
        if (overriders.remove(admin)) {
            return false;
        }
        return overriders.add(admin);
    }

    public void remove(UUID admin) {
        overriders.remove(admin);
    }

    public boolean hasOverride(UUID admin) {
        return overriders.contains(admin);
    }

}
