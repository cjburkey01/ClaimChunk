package com.cjburkey.claimchunk.data.newdata;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.cjburkey.claimchunk.player.SimplePlayerData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a class that may act as a data handler for ClaimChunk. Such a class is responsible for
 * keeping track of all claimed chunks and their owners as well as all joined players and their
 * associated data.
 *
 * @since 0.0.13
 */
public interface IClaimChunkDataHandler {

    // -- DATA -- //

    /**
     * Initializes the data handler.
     *
     * @throws Exception Any exception thrown during initialization that should require the plugin
     *     to be disabled
     * @since 0.0.13
     */
    void init() throws Exception;

    /**
     * Retrieves whether the data handler has already been initialized.
     *
     * @return Whether the data handler is initialized
     * @since 0.0.16
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean getHasInit();

    /**
     * Cleans up anything that needs to be cleaned up.
     *
     * @throws Exception Any exception thrown during cleanup
     * @since 0.0.13
     */
    void exit() throws Exception;

    /**
     * Saves data for this data handler. This method should not call any Bukkit methods as it runs
     * async.
     *
     * @throws Exception Any exception thrown during the saving process
     * @since 0.0.13
     */
    void save() throws Exception;

    /**
     * Loads data for this data handler. This method should not call any Bukkit methods as it runs
     * async.
     *
     * @throws Exception Any exception thrown during the loading process
     * @since 0.0.13
     */
    void load() throws Exception;

    // -- CHUNKS -- //

    /**
     * Registers that the given chunk was claimed by the given player. This WILL override the
     * previous owner if set.
     *
     * @param pos The position of the chunk
     * @param player The new owner of the chunk
     * @since 0.0.13
     */
    void addClaimedChunk(ChunkPos pos, UUID player);

    /**
     * Registers all the provided chunks.
     *
     * @param chunks The chunks' owners and positions
     * @since 0.0.13
     */
    void addClaimedChunks(DataChunk[] chunks);

    /**
     * Removes the owner from the given chunk. Does nothing if the chunk is already not owned.
     *
     * @param pos The position of the chunk
     * @since 0.0.13
     */
    void removeClaimedChunk(ChunkPos pos);

    /**
     * Checks whether the given chunk is currently claimed.
     *
     * @param pos The position of the chunk
     * @return Whether the chunk is claimed
     * @since 0.0.13
     */
    boolean isChunkClaimed(ChunkPos pos);

    /**
     * Retrieves the current owner of the given chunk.
     *
     * @param pos The position of the chunk
     * @return The current owner of the chunk or {@code null} if it is unclaimed
     * @since 0.0.13
     */
    @Nullable UUID getChunkOwner(ChunkPos pos);

    /**
     * Retrieves all claimed chunks and their owners across all worlds.
     *
     * @return An array of all claimed chunks
     * @since 0.0.13
     */
    DataChunk[] getClaimedChunks();

    // -- PLAYERS -- //

    /**
     * Adds a new player to the player tracking system.
     *
     * @param player The UUID of the player
     * @param lastIgn The in-game name of the player
     * @param chunkName The display name for this player's chunks
     * @param lastOnlineTime The last time (in ms since January 1, 1970, UTC) that the player was
     *     online
     * @param alerts Whether to send this player alerts when someone enters their chunks
     * @since 0.0.24
     */
    void addPlayer(
            UUID player,
            String lastIgn,
            @Nullable String chunkName,
            long lastOnlineTime,
            boolean alerts,
            int maxClaims);

    /**
     * Adds a new player to the player tracking system.
     *
     * @param playerData The player to add
     * @since 0.0.24
     */
    default void addPlayer(FullPlayerData playerData) {
        this.addPlayer(
                playerData.player,
                playerData.lastIgn,
                playerData.chunkName,
                playerData.lastOnlineTime,
                playerData.alert,
                playerData.extraMaxClaims);
    }

    /**
     * Adds a new player to the player tracking system.
     *
     * @param player The UUID of the player
     * @param lastIgn The in-game name of the player
     * @param alerts Whether to send this player alerts when someone enters their chunks
     * @since 0.0.24
     */
    default void addPlayer(UUID player, String lastIgn, boolean alerts, int defaultMaxClaims) {
        this.addPlayer(player, lastIgn, null, System.currentTimeMillis(), alerts, defaultMaxClaims);
    }

    /**
     * Adds all provided players.
     *
     * @param players An array of the players' data
     * @since 0.0.13
     */
    void addPlayers(FullPlayerData[] players);

    /**
     * Retrieves the username for the given player UUID.
     *
     * @param player The UUID for which to determine the player's UUID
     * @return The username for the player or {@code null} if that player has not joined the server
     * @since 0.0.13
     */
    @Nullable String getPlayerUsername(UUID player);

    /**
     * Retrieves the UUID for the given player username.
     *
     * @param username The UUID for which to determine the player's username
     * @return The UUID for the player or {@code null} if that player has not joined the server
     * @since 0.0.13
     */
    @Nullable UUID getPlayerUUID(String username);

    /**
     * Set the last time (in ms since January 1, 1970, UTC) that the player was online.
     *
     * @param player The player whose time should be updated
     * @param time The new time since the player was last online
     * @since 0.0.13
     */
    void setPlayerLastOnline(UUID player, long time);

    /**
     * Sets the given player's chunk's display name.
     *
     * @param player The player whose chunks should have the new display name
     * @param name The new display name for this player's chunks or {@code null} to clear it
     * @since 0.0.13
     */
    void setPlayerChunkName(UUID player, @Nullable String name);

    /**
     * Retrieves the given player's chunk's display name.
     *
     * @param player The player whose chunks' name to check
     * @return The new display name for this player's chunks or {@code null} if the player has not
     *     joined or the chunks are unnamed
     * @since 0.0.13
     */
    @Nullable String getPlayerChunkName(UUID player);

    /**
     * Set whether the given player will receive an alert when they are online and a player enters
     * their territory.
     *
     * @param player The player's UUID
     * @param alerts Whether to send the player alerts
     * @since 0.0.13
     */
    void setPlayerReceiveAlerts(UUID player, boolean alerts);

    /**
     * Retrieve whether the given player should receive an alert when they are online and a player
     * enters their territory.
     *
     * @param player The player's UUID
     * @return Whether to send alerts to this player
     * @since 0.0.13
     */
    boolean getPlayerReceiveAlerts(UUID player);

    /**
     * Set the maximum number of claims the given player can have.
     *
     * @param player The player's UUID
     * @param maxClaims The new number of maximum claims a player can have.
     * @since 0.0.24
     */
    void setPlayerExtraMaxClaims(UUID player, int maxClaims);

    /**
     * Add the given number of claims to the maximum number of claims the given player can have.
     * PERFORM ABS ON PROVIDED NUMBER
     *
     * @param player The player's UUID
     * @param numToAdd Number of claims to add
     * @since 0.0.24
     */
    void addPlayerExtraMaxClaims(UUID player, int numToAdd);

    /**
     * Add the given number of claims to the maximum number of claims the given player can have.
     * Clamp to 0
     *
     * @param player The player's UUID
     * @param numToTake Number of claims to add
     * @since 0.0.24
     */
    void takePlayerExtraMaxClaims(UUID player, int numToTake);

    /**
     * Get the maximum number of claims the given player can have.
     *
     * @param player The player's UUID
     * @return Max claims
     * @since 0.0.24
     */
    int getPlayerExtraMaxClaims(UUID player);

    /**
     * Whether the given player has joined the server and is registered in the player tracker.
     *
     * @param player The player's UUID
     * @return Whether the player exists in this system
     * @since 0.0.13
     */
    boolean hasPlayer(UUID player);

    /**
     * Retrieves all players within this system.
     *
     * @return A collection with all players within this system
     * @since 0.0.13
     */
    Collection<SimplePlayerData> getPlayers();

    /**
     * Retrieves all players within this system with all their information.
     *
     * @return An array with all players within this system
     * @since 0.0.13
     */
    FullPlayerData[] getFullPlayerData();

    // -- ACCESS -- //

    /**
     * Enable the given permission flag(s) for the provided player
     *
     * @param owner Owner of the chunks granting access. Cannot be null.
     * @param accessor Other player having access granted to them, or null to apply defaults for all
     *     players.
     * @param chunk The chunk to grant access to, or null to apply as defaults for all chunks.
     * @param flags Mapping of flags to their allow/deny boolean value. Must not be null.
     * @since 1.0.0
     */
    void setPermissionFlags(
            @NotNull UUID owner,
            @Nullable UUID accessor,
            @Nullable ChunkPos chunk,
            @NotNull HashMap<String, Boolean> flags);

    /**
     * Unset the provided permissions to defer to defaults
     *
     * @param owner Owner of the chunks granting access. Cannot be null
     * @param accessor Other player having access revoked from them. May be null to set flags for
     *     all chunks.
     * @param chunk Chunk to revoke access from, or null to clear the all chunk default
     * @param flagNames The name(s) of the flag(s) to revoke. May be null to set flags for all
     *     players.
     * @since 1.0.0
     */
    void clearPermissionFlags(
            @NotNull UUID owner,
            @Nullable UUID accessor,
            @Nullable ChunkPos chunk,
            @NotNull String... flagNames);

    /**
     * TODO: JAVADOC
     *
     * @param owner
     * @param accessor
     * @param chunk
     * @return
     * @since 1.0.0
     */
    @NotNull Map<String, Boolean> getPlyFlags(
            @NotNull UUID owner, @Nullable UUID accessor, @Nullable ChunkPos chunk);
}
