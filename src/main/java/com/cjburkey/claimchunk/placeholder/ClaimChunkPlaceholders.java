package com.cjburkey.claimchunk.placeholder;

import com.cjburkey.claimchunk.api.IClaimChunkPlugin;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ClaimChunkPlaceholders extends PlaceholderExpansion {

    private final IClaimChunkPlugin claimChunk;

    private final HashMap<String, Supplier<Object>> placeholders = new HashMap<>();
    private final HashMap<String, Function<OfflinePlayer, Object>> offlinePlayerPlaceholders =
            new HashMap<>();
    private final HashMap<String, Function<Player, Object>> playerPlaceholders = new HashMap<>();
    private final HashMap<String, BiFunction<Player, Optional<UUID>, Object>>
            playerOwnerPlaceholders = new HashMap<>();

    public ClaimChunkPlaceholders(IClaimChunkPlugin claimChunk) {
        this.claimChunk = claimChunk;

        /* General placeholders */

        // ClaimChunk version placeholder
        placeholders.put("version", claimChunk::getVersion);

        // ClaimChunk latest release on GitHub placeholder
        placeholders.put("online_version", claimChunk::getAvailableVersion);

        /* Offline player placeholders */

        // This player's chunk name
        offlinePlayerPlaceholders.put(
                "my_name", ply -> claimChunk.getPlayerHandler().getChunkName(ply.getUniqueId()));

        // This player's total number of claimed chunks
        offlinePlayerPlaceholders.put(
                "my_claims", ply -> claimChunk.getChunkHandler().getClaimed(ply.getUniqueId()));

        /* Online player placeholders */

        // This player's maximum number of claims as calculated by the rank
        // handler
        playerPlaceholders.put(
                "my_max_claims", ply -> claimChunk.getRankHandler().getMaxClaimsForPlayer(ply));

        // This player's remaining number of claims as calculated by the rank handler
        playerPlaceholders.put(
                "my_remaining_claims",
                ply ->
                        claimChunk.getRankHandler().getMaxClaimsForPlayer(ply)
                                - claimChunk.getChunkHandler().getClaimed(ply.getUniqueId()));

        /* Online player with chunk owner UUID placeholders */

        // Whether this player has ANY permissions in the chunk they're inside
        playerOwnerPlaceholders.put(
                "am_trusted",
                (ply, ignoredOwner) -> {
                    return false;
                    /*ChunkPos pos = new ChunkPos(ply.getLocation().getChunk());
                    Map<String, Boolean> permissions =
                            claimChunk.getPlayerHandler().getPermissions(pos, ply.getUniqueId());
                    return permissions == null
                            || permissions.values().stream().anyMatch(Boolean::booleanValue);*/
                });

        // Get the username of the owner for this chunk
        playerOwnerPlaceholders.put(
                "current_owner",
                (ply, owner) ->
                        owner.map(o -> claimChunk.getPlayerHandler().getUsername(o))
                                .orElse(
                                        claimChunk.getMessages()
                                                .placeholderApiUnclaimedChunkOwner));

        // Get the display name for this chunk
        playerOwnerPlaceholders.put(
                "current_name",
                (ply, owner) ->
                        owner.map(o -> claimChunk.getPlayerHandler().getChunkName(o))
                                .orElse(
                                        claimChunk.getMessages()
                                                .placeholderApiUnclaimedChunkOwner));
    }

    @Override
    public @NotNull String getIdentifier() {
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
    public @NotNull String getAuthor() {
        return Arrays.toString(claimChunk.getDescription().getAuthors().toArray(new String[0]));
    }

    @Override
    public @NotNull String getVersion() {
        return claimChunk.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        // Check for a general placeholder
        Optional<Object> replacement =
                Optional.ofNullable(placeholders.get(identifier)).map(Supplier::get);
        if (replacement.isPresent()) {
            return Objects.toString(replacement.get());
        }

        // Check for an offline player placeholder
        replacement =
                Optional.ofNullable(offlinePlayerPlaceholders.get(identifier))
                        .map(f -> f.apply(player));
        if (replacement.isPresent()) {
            return Objects.toString(replacement.get());
        }

        // If the player is online, try some other placeholders
        if (player instanceof Player) {
            return onPlaceholderRequest((Player) player, identifier);
        }

        // No placeholder found
        return null;
    }

    @Override
    public String onPlaceholderRequest(Player onlinePlayer, @NotNull String identifier) {
        // Check for an online player placeholder
        Optional<Object> replacement =
                Optional.ofNullable(playerPlaceholders.get(identifier))
                        .map(f -> f.apply(onlinePlayer));
        if (replacement.isPresent()) {
            return Objects.toString(replacement.get());
        }

        // Get the owner of the chunk in which `onlinePlayer` is standing
        UUID chunkOwner =
                claimChunk.getChunkHandler().getOwner(onlinePlayer.getLocation().getChunk());
        return Optional.ofNullable(playerOwnerPlaceholders.get(identifier))
                .map(f -> f.apply(onlinePlayer, Optional.ofNullable(chunkOwner)))
                .map(Object::toString)
                .orElse(null);
    }

    public static String fillPlaceholders(@Nullable CommandSender player, @NotNull String input) {
        return PlaceholderAPI.setPlaceholders(
                player instanceof Player
                        ? (Player) player
                        : (player instanceof OfflinePlayer ? (OfflinePlayer) player : null),
                input);
    }
}
