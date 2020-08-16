package com.cjburkey.claimchunk.placeholder;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;

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
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return Arrays.toString(claimChunk.getDescription()
                                         .getAuthors()
                                         .toArray(new String[0]));
    }

    @Override
    public String getVersion() {
        return claimChunk.getDescription()
                         .getVersion();
    }

    @Override
    public String onRequest(@Nonnull OfflinePlayer player, @Nonnull String identifier) {
        Utils.debug("Placeholder %s for player %s", identifier, player.getName());

        // This player's chunk name
        if (identifier.equals("my_name")) {
            return claimChunk.getPlayerHandler()
                             .getChunkName(player.getUniqueId());
        }

        // This player's total number of claimed chunks
        if (identifier.equals("my_claims")) {
            return "" + claimChunk.getChunkHandler()
                                  .getClaimed(player.getUniqueId());
        }

        // If the player is online, try some other placeholders
        if (player instanceof Player) {
            return onPlaceholderRequest((Player) player, identifier);
        }

        // No placeholder found
        return null;
    }

    @Override
    public String onPlaceholderRequest(@Nonnull Player onlinePlayer, @Nonnull String identifier) {
        UUID chunkOwner = claimChunk.getChunkHandler()
                                    .getOwner(onlinePlayer.getLocation()
                                                          .getChunk());

        // Both of the placeholders are the name of the player that owns this
        // chunk, there isn't an owner so no name is necessary
        if (chunkOwner == null) {
            return claimChunk.getMessages().placeholderApiUnclaimedChunkOwner;
        }

        // This player's maximum number of claims as calculated by the rank
        // handler
        if (identifier.equals("my_max_claims")) {
            return "" + claimChunk.getRankHandler()
                                  .getMaxClaimsForPlayer(onlinePlayer);
        }

        // Get the owner's username of the chunk the player is currently standing on
        if (identifier.equals("current_owner")) {
            return claimChunk.getPlayerHandler()
                             .getUsername(chunkOwner);
        }

        // Get the owner's chunk display name based on the chunk the player is currently standing on
        if (identifier.equals("current_name")) {
            return claimChunk.getPlayerHandler()
                             .getChunkName(claimChunk.getChunkHandler()
                                                     .getOwner(onlinePlayer.getLocation()
                                                                           .getChunk()));
        }

        // Not a valid placeholder for ClaimChunk
        return null;
    }
}
