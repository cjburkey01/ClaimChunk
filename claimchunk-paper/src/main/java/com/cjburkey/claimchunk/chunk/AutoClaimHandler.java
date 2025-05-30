package com.cjburkey.claimchunk.chunk;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

// This is not saved across server launches
public class AutoClaimHandler {

    // Keep a temporary list (set) of all players with auto claiming enabled
    private static final HashSet<UUID> current = new HashSet<>();

    public static boolean inList(Player ply) {
        // Check if the player is within the list
        return current.contains(ply.getUniqueId());
    }

    @SuppressWarnings("unused")
    private static boolean enable(Player ply) {
        // Enable for the provided player
        return current.add(ply.getUniqueId());
    }

    public static boolean disable(Player ply) {
        // Disable for the provided player
        return current.remove(ply.getUniqueId());
    }

    /**
     * Toggles whether or not the player has autoclaim enabled.
     *
     * @param ply The player to toggle.
     * @return Whether or not the mode is NOW enabled.
     */
    public static boolean toggle(Player ply) {
        // Toggle for the provided player
        if (disable(ply)) return false;
        return enable(ply);
    }
}
