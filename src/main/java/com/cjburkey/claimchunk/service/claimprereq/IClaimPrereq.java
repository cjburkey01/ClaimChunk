package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.Comparator;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 * An interface representing some class that performs a check before a chunk
 * can be claimed.
 *
 * @since 0.0.20
 */
public interface IClaimPrereq {

    /**
     * Gets the priority of this prerequisite. A smaller number would be
     * checked first.
     *
     * @return The signed integer weight of this prerequisite.
     * @since 0.0.20
     */
    int getWeight();

    /**
     * Determines whether the provided player should be able to claim the
     * provided chunk. Note: this method should not change the state of
     * anything as it's currently unknown whether the chunk claim could fail
     * after this check. If a subsequent check fails, then the claim doesn't
     * not occur, so it's important to make sure nothing is changed in this
     * method.
     *
     * @param claimChunk The current instance of ClaimChunk. This should be
     *                   equivalent to {@link com.cjburkey.claimchunk.ClaimChunk#getInstance()}.
     * @param player     The player attempting to claim the chunk.
     * @param location   The chunk currently being claimed.
     * @return Whether this prerequisite was met.
     * @since 0.0.20
     */
    boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location);

    /**
     * Gets the error message that should be displayed if this prerequisite is not met.
     * If a prerequisite is not met, the claim process stops there and displays the
     * error message returned by the failing check.
     *
     * @param claimChunk The current instance of ClaimChunk. This should be
     *                   equivalent to {@link com.cjburkey.claimchunk.ClaimChunk#getInstance()}.
     * @param player     The player attempting to claim the chunk.
     * @param location   The chunk currently being claimed.
     * @return An optional error message describing why the claim has failed.
     * @since 0.0.20
     */
    default Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.empty();
    }

    /**
     * If this prerequisite succeeds, it will override the current success
     * message with this one if it is supplied.
     *
     * @param claimChunk The current instance of ClaimChunk. This should be
     *                   equivalent to {@link com.cjburkey.claimchunk.ClaimChunk#getInstance()}.
     * @param player     The player attempting to claim the chunk.
     * @param location   The chunk currently being claimed.
     * @return An optional success message.
     * @since 0.0.20
     */
    default Optional<String> getSuccessMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.empty();
    }

    /**
     * Called after the provided chunk has been successfully claimed for the
     * provided player.
     *
     * @param claimChunk The current instance of ClaimChunk. This should be
     *                   equivalent to {@link com.cjburkey.claimchunk.ClaimChunk#getInstance()}.
     * @param player     The player attempting to claim the chunk.
     * @param location   The chunk currently being claimed.
     * @since 0.0.20
     */
    default void onClaimSuccess(ClaimChunk claimChunk, Player player, Chunk location) {
    }

    final class ClaimPrereqComparator implements Comparator<IClaimPrereq> {

        public int compare(IClaimPrereq o1, IClaimPrereq o2) {
            return Integer.compare(o1.getWeight(), o2.getWeight());
        }

    }

}
