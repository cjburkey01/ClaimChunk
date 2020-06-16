package com.cjburkey.claimchunk.placeholder;

import com.cjburkey.claimchunk.ClaimChunk;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ClaimChunkPlaceholders extends PlaceholderExpansion {

    private final ClaimChunk claimChunk;

    public ClaimChunkPlaceholders(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    @Override
    public String getIdentifier() {
        return "claimchunk";
    }

    @Override
    public String getAuthor() {
        return "CJ Burkey";
    }

    @Override
    public String getVersion() {
        return claimChunk.getVersion()
                         .toString();
    }

    @Override
    public String onRequest(@Nonnull OfflinePlayer player, @Nonnull String identifier) {
        // If it doesn't start with `claimchunk_`, it isn't one of these anyway; so stop early
        if (!identifier.startsWith("claimchunk_")) {
            return null;
        }

        // This player's chunk name
        if (identifier.equals("claimchunk_my_name")) {
            return claimChunk.getPlayerHandler()
                             .getChunkName(player.getUniqueId());
        }

        // This player's total number of claimed chunks
        if (identifier.equals("claimchunk_my_claims")) {
            return "" + claimChunk.getChunkHandler()
                                  .getClaimed(player.getUniqueId());
        }

        // Check if the player is online
        if (player.isOnline()) {
            return onPlaceholderRequest((Player) player, identifier);
        }
        return null;
    }

    @Override
    public String onPlaceholderRequest(@Nonnull Player onlinePlayer, @Nonnull String identifier) {
        // Get the owner's username of the chunk the player is currently standing on
        if (identifier.equals("claimchunk_current_owner")) {
            return claimChunk.getPlayerHandler()
                             .getUsername(claimChunk.getChunkHandler()
                                                    .getOwner(onlinePlayer.getLocation()
                                                                          .getChunk()));
        }

        // Get the owner's chunk display name based on the chunk the player is currently standing on
        if (identifier.equals("claimchunk_current_name")) {
            return claimChunk.getPlayerHandler()
                             .getChunkName(claimChunk.getChunkHandler()
                                                     .getOwner(onlinePlayer.getLocation()
                                                                           .getChunk()));
        }

        // Not a valid placeholder for ClaimChunk
        return null;
    }
}
