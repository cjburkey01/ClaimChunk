package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.ClaimChunkConfig;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

// TODO: MOVE FLOOD FILL TO MAIN HANDLER AND REQUIRE RECIPIENT TO BE ONLINE TO
//       GUARANTEE QUOTA ISN'T EXCEEDED.

public final class ChunkHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final ClaimChunk claimChunk;

    public ChunkHandler(IClaimChunkDataHandler dataHandler, ClaimChunk claimChunk) {
        this.dataHandler = dataHandler;
        this.claimChunk = claimChunk;
    }

    /**
     * Result returned by the {@link #fillClaimInto(String, int, int, int, int, UUID, Collection)}
     * and {@link #fillClaim(String, int, int, int, UUID)} methods.
     */
    public static enum FloodClaimResult {

        /**
         * The method completed without issues.
         */
        SUCCESSFULL,

        /**
         * The method recursed too many times and aborted due to that.
         */
        TOO_MANY_RECUSIONS,

        /**
         * The collection got too big and the method aborted due to that.
         */
        COLLECTION_TOO_BIG,

        /**
         * The algorithm hit a claimed chunk that did not belong to the player and aborted
         * due to this
         */
        HIT_NONPLAYER_CLAIM;
    }

    /**
     * Claims several chunks at once for a player.
     * This method is very unsafe at it's own as it does not test whether
     * the player can actually claim that chunk. This means that ownership can
     * be overridden. And the player can go over it's quota as well
     *
     * @param chunks The chunks to claim
     * @param player The player that claims these chunks
     */
    private void claimAll(Collection<ChunkPos> chunks, UUID player) {
        for (ChunkPos chunk : chunks) {
            dataHandler.addClaimedChunk(chunk, player);
        }
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     * This method doesn't do any checks other than previous ownership.
     * It is not generally safe to use this method. Other public API methods
     * should be used to claim chunks.
     *
     * @param world     The current world.
     * @param x         The chunk x-coord.
     * @param z         The chunk z-coord.
     * @param player    The player for whom to claim the chunk.
     * @param floodfill Whether or not flood filling should be attempted
     * @return The chunk position variable or {@code null} if the chunk is already claimed
     */
    public ChunkPos claimChunk(String world, int x, int z, UUID player, boolean floodfill) {
        if (isClaimed(world, x, z)) {
            // If the chunk is already claimed, return null
            return null;
        }

        // Create a chunk position representation
        ChunkPos pos = new ChunkPos(world, x, z);

        // Add the chunk to the claimed chunk
        dataHandler.addClaimedChunk(pos, player);

        if (claimChunk.chConfig().getFloodClaimEnabled()) { // Optionally attempt flood filling
            int amountClaimed = 0;
            for (int x2 = -1; x2 <= 1; x2++) {
                for (int y2 = -1; y2 <= 1; y2++) {
                    if (player.equals(getOwner(new ChunkPos(world, x+ x2, z + y2)))) {
                        amountClaimed++;
                    }
                }
            }
            if (amountClaimed > 1) {
                Player ply = Bukkit.getPlayer(player);

                ClaimChunkConfig ccc = claimChunk.chConfig();

                int maxArea = Math.min(ccc.getFloodClaimMaxArea(),
                        (ply == null
                                ? ccc.getDefaultMaxChunksClaimed()
                                : claimChunk.getRankHandler().getMaxClaimsForPlayer(ply)) - getClaimed(player));
                Map.Entry<Collection<ChunkPos>, FloodClaimResult> result = fillClaim(world, x - 1, z, maxArea, player);
                if (result.getValue() == FloodClaimResult.SUCCESSFULL) {
                    claimAll(result.getKey(), player);
                } else {
                    result = fillClaim(world, x + 1, z, maxArea, player);
                    if (result.getValue() == FloodClaimResult.SUCCESSFULL) {
                        claimAll(result.getKey(), player);
                    } else {
                        result = fillClaim(world, x, z - 1, maxArea, player);
                        if (result.getValue() == FloodClaimResult.SUCCESSFULL) {
                            claimAll(result.getKey(), player);
                        } else {
                            result = fillClaim(world, x, z + 1, maxArea, player);
                            if (result.getValue() == FloodClaimResult.SUCCESSFULL) {
                                claimAll(result.getKey(), player);
                            }
                        }
                    }
                }
            }
        }

        // Return the chunk position
        return pos;
    }

    /**
     * Fills claims near the given positions that are connected to the current claim via unclaimed claims.
     * If it hits a claim that was claimed by any other player, this method will abort and will return
     * {@link FloodClaimResult#HIT_NONPLAYER_CLAIM},
     * but will still have populated the collection to some extent. It will also abort a similar way if the
     * maxSize has been reached or when recursions is equal 0.
     * Note that the chunks are not claimed as this method needs to be performed later due to the abort reasons
     * within the method.
     *
     * @param world The world name, used for claim checking
     * @param x the x-position of the current chunk
     * @param z the z-position of the current chunk
     * @param recursions The maximum amount of recursions remaining
     * @param maxSize The maximum size of the collection until the method aborts
     * @param player The player to claim the chunks for
     * @param collector The collection to drop the chunks into
     * @return How the method completed
     */
    private FloodClaimResult fillClaimInto(String world, int x, int z, int recursions, int maxSize, UUID player, Collection<ChunkPos> collector) {
        if (recursions == 0 ) {
            return FloodClaimResult.TOO_MANY_RECUSIONS;
        }
        if (collector.size() > maxSize) {
            return FloodClaimResult.COLLECTION_TOO_BIG;
        }
        ChunkPos claimingPosition = new ChunkPos(world, x, z);
        if (collector.contains(claimingPosition)) {
            return FloodClaimResult.SUCCESSFULL;
        }
        UUID owner = getOwner(claimingPosition);
        if (owner != null) {
            if (owner.equals(player)) {
                return FloodClaimResult.SUCCESSFULL; // Hit player claim, do not claim it
            } else {
                return FloodClaimResult.HIT_NONPLAYER_CLAIM; // Hit player claim, do not claim it
            }
        }
        collector.add(claimingPosition);
        FloodClaimResult result = fillClaimInto(world, x - 1, z, --recursions, maxSize, player, collector);
        if (result != FloodClaimResult.SUCCESSFULL) {
            return result;
        }
        result = fillClaimInto(world, x + 1, z, recursions, maxSize, player, collector);
        if (result != FloodClaimResult.SUCCESSFULL) {
            return result;
        }
        result = fillClaimInto(world, x, z - 1, recursions, maxSize, player, collector);
        if (result != FloodClaimResult.SUCCESSFULL) {
            return result;
        }
        return fillClaimInto(world, x, z + 1, recursions, maxSize, player, collector);
    }

    private Map.Entry<Collection<ChunkPos>, FloodClaimResult> fillClaim(String world, int x, int z, int maxFillArea, UUID player) {
        HashSet<ChunkPos> positions = new HashSet<>(maxFillArea);
        FloodClaimResult result = fillClaimInto(world, x, z, claimChunk.chConfig().getFloodClaimMaxIter(),
                maxFillArea, player, positions);
        return new AbstractMap.SimpleEntry<>(positions, result);
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     * This method doesn't do any checks other than previous ownership.
     * It is not generally safe to use this method. Other public API methods
     * should be used to claim chunks. Does not perform flood filling.
     *
     * @param world  The current world.
     * @param x      The chunk x-coord.
     * @param z      The chunk z-coord.
     * @param player The player for whom to claim the chunk.
     * @return The chunk position variable or {@code null} if the chunk is already claimed
     */
    public ChunkPos claimChunk(String world, int x, int z, UUID player) {
        return claimChunk(world, x, z, player, false);
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     * This method doesn't do any checks other than previous ownership.
     * It is not generally safe to use this method. Other public API methods
     * should be used to claim chunks.
     *
     * @param world     The current world.
     * @param x         The chunk x-coord.
     * @param z         The chunk z-coord.
     * @param player    The player for whom to claim the chunk.
     * @param floodfill Whether or not flood filling should be attempted
     * @return The chunk position variable or {@code null} if the chunk is already claimed
     */
    public ChunkPos claimChunk(World world, int x, int z, UUID player, boolean floodfill) {
        return claimChunk(world.getName(), x, z, player, floodfill);
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     * This method doesn't do any checks other than previous ownership.
     * It is not generally safe to use this method. Other public API methods
     * should be used to claim chunks. Does not perform flood filling.
     *
     * @param world  The current world.
     * @param x      The chunk x-coord.
     * @param z      The chunk z-coord.
     * @param player The player for whom to claim the chunk.
     * @return The chunk position variable or {@code null} if the chunk is already claimed
     */
    public ChunkPos claimChunk(World world, int x, int z, UUID player) {
        return claimChunk(world.getName(), x, z, player);
    }

    /**
     * Tries to unclaim a specific chunk and does nothing if the chunk is unowned.
     * This method doesn't do any checks other than ownership.
     * It is not generally safe to use this method (which is why it's private).
     * Other, public API methods should be used to claim chunks.
     *
     * @param world The current world.
     * @param x     The chunk x-coord.
     * @param z     The chunk z-coord.
     */
    public void unclaimChunk(World world, int x, int z) {
        if (isClaimed(world, x, z)) {
            // If the chunk is claimed, remove it from the claimed chunks list
            dataHandler.removeClaimedChunk(new ChunkPos(world.getName(), x, z));
        }
    }

    /**
     * Tries to unclaim a specific chunk and does nothing if the chunk is unowned.
     * This method doesn't do any checks other than ownership.
     * It is not generally safe to use this method (which is why it's private).
     * Other, public API methods should be used to claim chunks.
     *
     * @param world The current world name.
     * @param x     The chunk x-coord.
     * @param z     The chunk z-coord.
     */
    public void unclaimChunk(String world, int x, int z) {
        if (isClaimed(world, x, z)) {
            // If the chunk is claimed, remove it from the claimed chunks list
            dataHandler.removeClaimedChunk(new ChunkPos(world, x, z));
        }
    }

    /**
     * Gets the count of claimed chunks the provided player has.
     *
     * @param ply The UUID of the player.
     * @return The integer count of chunks this player has claimed in total across worlds.
     */
    public int getClaimed(UUID ply) {
        int count = 0;

        // Loop through all chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            // Increment for all chunks owned by this player
            if (chunk.player.equals(ply)) count++;
        }

        return count;
    }

    /**
     * Creates an array with all claimed chunks that the provided player has.
     *
     * @param ply The UUID of the player.
     * @return An array containing chunk positions for all this player's claimed chunks.
     */
    public ChunkPos[] getClaimedChunks(UUID ply) {
        // Create a set for the chunks
        Set<ChunkPos> chunks = new HashSet<>();

        // Loop through all chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            // Add chunks that are owned by this player
            if (chunk.player.equals(ply)) chunks.add(chunk.chunk);
        }

        // Convert the set into an array
        return chunks.toArray(new ChunkPos[0]);
    }

    /**
     * Checks if the provided player has already claimed all the chunks that
     * they can claim for free.
     *
     * @param ply The UUID of the player.
     * @return Whether the player has already claimed the maximum number of chunks that they can claim for free.
     */
    public boolean getHasAllFreeChunks(UUID ply) {
        return getHasAllFreeChunks(ply, claimChunk.chConfig().getFirstFreeChunks());
    }

    /**
     * Checks if the provided player has already claimed all the chunks that
     * they can claim for free.
     *
     * @param ply The UUID of the player.
     * @param count The number of free chunks the player should be allowed to have.
     * @return Whether the player has already claimed the maximum number of chunks that they can claim for free.
     */
    public boolean getHasAllFreeChunks(UUID ply, int count) {
        // Counter
        int total = 0;

        // If there are no free chunks, there's no point in checking
        if (count <= 0) return true;

        // Loop through all claimed chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            if (chunk.player.equals(ply)) {
                // If this player is the owner, increment the counter
                total++;

                // If they have the max (or more), they have claimed all the
                // chunks they can for free
                if (total >= count) return true;
            }
        }

        // They have not claimed all the chunks that they can claim for free
        return false;
    }

    /**
     * Check if the provided chunk is claimed.
     *
     * @param world The world in which this chunk is located.
     * @param x     The x-coordinate (in chunk coordinates) of the chunk.
     * @param z     The z-coordinate (in chunk coordinates) of the chunk.
     * @return Whether this chunk is currently claimed.
     */
    public boolean isClaimed(World world, int x, int z) {
        return dataHandler.isChunkClaimed(new ChunkPos(world.getName(), x, z));
    }

    /**
     * Check if the provided chunk is claimed.
     *
     * @param world The world in which this chunk is located.
     * @param x     The x-coordinate (in chunk coordinates) of the chunk.
     * @param z     The z-coordinate (in chunk coordinates) of the chunk.
     * @return Whether this chunk is currently claimed.
     */
    public boolean isClaimed(String world, int x, int z) {
        return dataHandler.isChunkClaimed(new ChunkPos(world, x, z));
    }

    /**
     * Check if the provided chunk is claimed.
     *
     * @param chunk The Spigot chunk position.
     * @return Whether this chunk is currently claimed.
     */
    public boolean isClaimed(Chunk chunk) {
        return isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    /**
     * Check if the provided player is the owner of the provided chunk
     *
     * @param world The world in which this chunk is located.
     * @param x     The x-coordinate (in chunk coordinates) of the chunk.
     * @param z     The z-coordinate (in chunk coordinates) of the chunk.
     * @param ply   The UUID of the player.
     * @return Whether this player owns this chunk.
     */
    public boolean isOwner(World world, int x, int z, UUID ply) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        UUID owner = dataHandler.getChunkOwner(pos);
        return owner != null && owner.equals(ply);
    }

    /**
     * Check if the provided player is the owner of the provided chunk
     *
     * @param world The world in which this chunk is located.
     * @param x     The x-coordinate (in chunk coordinates) of the chunk.
     * @param z     The z-coordinate (in chunk coordinates) of the chunk.
     * @param ply   The player.
     * @return Whether this player owns this chunk.
     */
    public boolean isOwner(World world, int x, int z, Player ply) {
        return isOwner(world, x, z, ply.getUniqueId());
    }

    /**
     * Check if the provided player is the owner of the provided chunk
     *
     * @param chunk The Spigot chunk position.
     * @param ply   The UUID of the player.
     * @return Whether this player owns this chunk.
     */
    public boolean isOwner(Chunk chunk, UUID ply) {
        return isOwner(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply);
    }

    /**
     * Check if the provided player is the owner of the provided chunk
     *
     * @param chunk The Spigot chunk position.
     * @param ply   The player.
     * @return Whether this player owns this chunk.
     */
    public boolean isOwner(Chunk chunk, Player ply) {
        return isOwner(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply);
    }

    /**
     * Get the UUID of the owner of the provided chunk.
     *
     * @param world The world in which this chunk is located.
     * @param x     The x-coordinate (in chunk coordinates) of the chunk.
     * @param z     The z-coordinate (in chunk coordinates) of the chunk.
     * @return The UUID of the owner of this chunk.
     */
    public UUID getOwner(World world, int x, int z) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }


    /**
     * Get the UUID of the owner of the provided chunk.
     *
     * @param chunk The Spigot chunk position.
     * @return The UUID of the owner of this chunk.
     */
    public UUID getOwner(Chunk chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    /**
     * Get the UUID of the owner of the provided chunk.
     * Returns null if not claimed.
     *
     * @param pos The ClaimChunk chunk position.
     * @return The UUID of the owner of this chunk.
     */
    public UUID getOwner(ChunkPos pos) {
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    /**
     * Toggles whether TNT is enabled in the provided chunk.
     *
     * @param chunk The Spigot chunk position.
     * @return Whether TNT is now (after the toggle) enabled in this chunk.
     */
    public boolean toggleTnt(Chunk chunk) {
        return dataHandler.toggleTnt(new ChunkPos(chunk));
    }

    /**
     * Checks whether TNT is enabled in the provided chunk.
     *
     * @param chunk The Spigot chunk position.
     * @return Whether TNT is currently enabled in this chunk.
     */
    public boolean isTntEnabled(Chunk chunk) {
        return dataHandler.isTntEnabled(new ChunkPos(chunk));
    }

}
