package com.cjburkey.claimchunk.chunk;

import java.util.HashSet;
import java.util.UUID;
import org.bukkit.entity.Player;

// This is not saved across server launches
public class AutoClaimHandler {

    private static final HashSet<UUID> current = new HashSet<>();

    public static boolean inList(Player ply) {
        return current.contains(ply.getUniqueId());
    }

    @SuppressWarnings("unused")
    private static boolean enable(Player ply) {
        return current.add(ply.getUniqueId());
    }

    public static boolean disable(Player ply) {
        return current.remove(ply.getUniqueId());
    }

    /**
     * Toggles whether or not the player has autoclaim enabled.
     *
     * @param ply The player to toggle.
     * @return Whether or not the mode is NOW enabled.
     */
    public static boolean toggle(Player ply) {
        if (disable(ply)) return false;
        return enable(ply);
    }

}
