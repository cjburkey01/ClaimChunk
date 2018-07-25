package com.cjburkey.claimchunk.chunk;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.entity.Player;

public class AutoClaimHandler {

    private static final Queue<UUID> current = new ConcurrentLinkedQueue<>();

    public static boolean inList(Player ply) {
        return current.contains(ply.getUniqueId());
    }

    public static void enable(Player ply) {
        current.add(ply.getUniqueId());
    }

    public static void disable(Player ply) {
        current.remove(ply.getUniqueId());
    }

    /**
     * Toggles whether or not the player has autoclaim enabled.
     * 
     * @param ply
     *            The player to toggle.
     * @return Whether or not the mode is NOW enabled.
     */
    public static boolean toggle(Player ply) {
        if (current.remove(ply.getUniqueId())) {
            return false;
        }
        return current.add(ply.getUniqueId());
    }

}