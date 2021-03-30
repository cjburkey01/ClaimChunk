package com.cjburkey.claimchunk.data.hyperdrive.ply;

import com.cjburkey.claimchunk.data.hyperdrive.ply.ICCJoinedPlayer;
import org.bukkit.entity.Player;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An interface representing all the methods a PlayerHandler should implement
 * for ClaimChunk
 *
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface ICCPlayerHandler<JoinedPlayerType extends ICCJoinedPlayer> {

    /**
     * Adds a given player to this player handler. This method will be called
     * by ClaimChunk when a new player joins.
     *
     * @param player The player to add to this player handler (will not be null).
     * @return {@code true} if the player was just added to this player handler, or {@code false} if the player has already joined.
     * @since 0.1.0
     */
    boolean onPlayerJoin(@Nonnull Player player);

    /**
     * Retrieves the complete collection of all players who are registered with
     * this player handler.
     *
     * @return A non-null collection of all the players stored in this player handler
     * @since 0.1.0
     */
    @Nonnull Collection<JoinedPlayerType> getJoinedPlayers();

    /**
     * Performs a query for usernames of players in this data handler that
     * start with a provided partial username.
     *
     * @param partialUsername The partial username to search (will not be null).
     * @return A non-null list of usernames that begin with the provided partial name.
     * @since 0.1.0
     */
    default @Nonnull List<String> getJoinedPlayersByUsername(@Nonnull String partialUsername) {
        return getJoinedPlayers().stream()
                .map(ICCJoinedPlayer::getLastIngameName)
                .filter(name -> name.startsWith(partialUsername))
                .collect(Collectors.toList());
    }

    /**
     * Toggles access for the given accessor in the given owner's chunks.
     *
     * @param owner The person whose chunks for which access will be toggled (will not be null).
     * @param accessor The person for whom access is toggled (will not be null).
     * @return Whether the given accessor now has access.
     * @since 0.1.0
     */
    boolean toggleAccess(@Nonnull UUID owner, @Nonnull UUID accessor);

    /**
     * Checks if the given accessor has access to the given owner's chunks.
     *
     * @param owner The person whose chunks for which accessibility will be determined (will not be null).
     * @param accessor The person for whom access is being queried (will not be null).
     * @return Whether the given accessor has access to the given owner's chunks.
     * @since 0.1.0
     */
    boolean hasAccess(@Nonnull UUID owner, @Nonnull UUID accessor);

    /**
     * Retrieves a collection of the UUIDs of all players who have access to
     * the given owner's chunks.
     *
     * @param owner The person for whom we wish to find accessors (will not be null).
     * @return A collection of UUIDs for accessors of the given owner's chunks.
     * @since 0.1.0
     */
    @Nonnull Collection<UUID> getAccessPermitted(@Nonnull UUID owner);

    /**
     * Toggles whether the given player will receive alerts when another player
     * enters their territory.
     *
     * @param player The player whose alerts are to be toggled (will not be null).
     * @return Whether the given player now has alerts enabled.
     * @since 0.1.0
     */
    boolean toggleAlerts(@Nonnull UUID player);

    /**
     * Checks whether the given player has alerts enabled.
     *
     * @param player The player whose alert status is to be queried (will not be null).
     * @return Whether the given player has alerts enabled.
     * @since 0.1.0
     */
    boolean hasAlerts(@Nonnull UUID player);

    /**
     * Changes the given player's chunk display name, or resets it to their
     * username if the given name is {@code null}.
     *
     * @param player The player whose chunk display name to modify (will not be null).
     * @param name The new name for the given owner's chunks, or {@code null} to clear the current name.
     * @since 0.1.0
     */
    void setChunkName(@Nonnull UUID player, @Nullable String name);

    /**
     * Checks whether the given player has a custom chunk name set.
     *
     * @param player The player (will not be null).
     * @return Whether this player has a custom chunk name.
     * @since 0.1.0
     */
    boolean hasChunkName(@Nonnull UUID player);

    /**
     * Tries to retrieve the chunk name for the given player.
     *
     * @param player The player (will not be null).
     * @return An optional representing the chunk name if one is present, or empty if the player hasn't joined or hasn't set a custom chunk display name.
     * @since 0.1.0
     */
    @Nonnull Optional<String> getChunkName(@Nonnull UUID player);

    /**
     * Retrieves the username for a given player
     *
     * @param player The player (will not be null).
     * @return The given player's username, or {@code null} if the player has not joined.
     * @since 0.1.0
     */
    @Nullable String getUsername(@Nonnull UUID player);

    /**
     * Retrieves a player's unique ID based on their username.
     *
     * @param username The given player's username (will not be null).
     * @return The player's UUID, or {@code null} if the given username has no UUID mapping.
     * @since 0.1.0
     */
    @Nullable UUID getUUID(@Nonnull String username);

    /**
     * Gets the display name to be shown. If the given player has a custom
     * chunk name, that will be shown, otherwise, their username will be.
     *
     * @param player The player (will not be null).
     * @return An optional representing the player's display name or username if present, otherwise empty if the given player has not joined.
     * @since 0.1.0
     */
    default Optional<String> getChunkDisplayName(@Nonnull UUID player) {
        return Optional.ofNullable(getChunkName(player).orElseGet(() -> getUsername(player)));
    }

    /**
     * Updates the given player's last joined time to the given time.
     *
     * @param player The player (will not be null).
     * @param time The Unix timestamp representing when the given player last joined, in milliseconds, since the start of the epoch.
     * @since 0.1.0
     */
    void setLastJoinedTime(@Nonnull UUID player, long time);

}
