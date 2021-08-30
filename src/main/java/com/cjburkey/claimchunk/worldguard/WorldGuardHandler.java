package com.cjburkey.claimchunk.worldguard;

import static com.cjburkey.claimchunk.worldguard.WorldGuardApi.*;

import com.cjburkey.claimchunk.ClaimChunk;

import org.bukkit.Chunk;

/** Safe wrapper for {@link com.cjburkey.claimchunk.worldguard.WorldGuardApi} */
public class WorldGuardHandler {

    private static boolean loaded = false;

    public static boolean init(ClaimChunk claimChunk) {
        try {
            return (loaded = _init(claimChunk));
        } catch (NoClassDefFoundError ignored) {
        }
        return false;
    }

    public static boolean isAllowedClaim(ClaimChunk claimChunk, Chunk chunk) {
        try {
            // If the WorldGuard api never loaded, just allow the claim
            return (!loaded || _isAllowedClaim(claimChunk, chunk));
        } catch (NoClassDefFoundError ignored) {
        }
        // This should never happen, but better safe than sorry
        return true;
    }
}
