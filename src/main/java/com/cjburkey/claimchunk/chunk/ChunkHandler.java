package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ChunkHandler {

    private final IClaimChunkDataHandler dataHandler;
    private final ClaimChunk claimChunk;

    public ChunkHandler(IClaimChunkDataHandler dataHandler, ClaimChunk claimChunk) {
        this.dataHandler = dataHandler;
        this.claimChunk = claimChunk;
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     * This method doesn't do any checks other than previous ownership.
     * It is not generally safe to use this method. Other public API methods
     * should be used to claim chunks.
     *
     * @param world  The current world.
     * @param x      The chunk x-coord.
     * @param z      The chunk z-coord.
     * @param player The player for whom to claim the chunk.
     * @return The chunk position variable or {@code null} if the chuk is already claimed
     */
    public ChunkPos claimChunk(String world, int x, int z, UUID player) {
        if (isClaimed(world, x, z)) {
            // If the chunk is already claimed, return null
            return null;
        }

        // Create a chunk position representation
        ChunkPos pos = new ChunkPos(world, x, z);

        // Add the chunk to the claimed chunk
        dataHandler.addClaimedChunk(pos, player);

        // Return the chunk position
        return pos;
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     * This method doesn't do any checks other than previous ownership.
     * It is not generally safe to use this method. Other public API methods
     * should be used to claim chunks.
     *
     * @param world  The current world.
     * @param x      The chunk x-coord.
     * @param z      The chunk z-coord.
     * @param player The player for whom to claim the chunk.
     * @return The chunk position variable or {@code null} if the chuk is already claimed
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

        // Loop throug all chunks
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
