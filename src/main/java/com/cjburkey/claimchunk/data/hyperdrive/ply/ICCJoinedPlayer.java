package com.cjburkey.claimchunk.data.hyperdrive.ply;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * A class representing a player that has been entered into a ClaimChunk player
 * handler.
 *
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public interface ICCJoinedPlayer {

    /**
     * Gets the unique ID for this player.
     *
     * @return The UUID of this player.
     * @since 0.1.0
     */
    @Nonnull UUID getUUID();

    /**
     * Retrieves the last in-game name recorded for this player.
     *
     * @return The username for a given player.
     * @since 0.1.0
     */
    @Nonnull String getLastIngameName();

    /**
     * Gets the last time this player was online.
     *
     * @return The Unix timestamp for when this player last joined, in milliseconds since the start of the epoch.
     * @since 0.1.0
     */
    long getLastJoinTime();

}
