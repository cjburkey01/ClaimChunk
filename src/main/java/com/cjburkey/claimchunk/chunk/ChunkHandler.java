package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public final class ChunkHandler implements ICCChunkHandler {

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
     * @return The chunk position variable or {@code null} if the chunk is already claimed
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @return The chunk position variable or {@code null} if the chunk is already claimed
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public int getClaimed(UUID ply) {
        int count = 0;

        // Loop through all chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            // Increment for all chunks owned by this player
            if (chunk.player.equals(ply)) count++;
        }

        return count;
    }

    // Fucky business until I implement the new data handlers

    @Override
    public @Nonnull Optional<UUID> setOwner(@Nonnull ChunkPos chunkPos, @Nullable UUID newOwner) {
        Optional<UUID> oldOwner = getOwner(chunkPos);
        if (oldOwner.isPresent()) unclaimChunk(chunkPos.getWorld(), chunkPos.getX(), chunkPos.getZ());
        if (newOwner != null) claimChunk(chunkPos.getWorld(), chunkPos.getX(), chunkPos.getZ(), newOwner);
        return oldOwner;
    }

    @Override
    public boolean getHasOwner(@Nonnull ChunkPos chunkPos) {
        return getOwner(chunkPos).isPresent();
    }

    @Override
    public @Nonnull Optional<UUID> getOwner(@Nonnull ChunkPos chunkPos) {
        return Optional.ofNullable(getOwner(Objects.requireNonNull(Bukkit.getWorld(chunkPos.getWorld())),
                chunkPos.getX(),
                chunkPos.getZ()));
    }

    @Override
    public int getClaimedChunksCount(@Nonnull UUID owner) {
        return getClaimed(owner);
    }

    @Override
    public @Nonnull Collection<ChunkPos> getClaimedChunks(@Nonnull UUID ply) {
        // Create a set for the chunks
        Set<ChunkPos> chunks = new HashSet<>();

        // Loop through all chunks
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            // Add chunks that are owned by this player
            if (chunk.player.equals(ply)) chunks.add(chunk.chunk);
        }

        // Convert the set into an array
        return chunks;
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
    public boolean isClaimed(String world, int x, int z) {
        return dataHandler.isChunkClaimed(new ChunkPos(world, x, z));
    }

    /**
     * Check if the provided chunk is claimed.
     *
     * @param chunk The Spigot chunk position.
     * @return Whether this chunk is currently claimed.
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
    public boolean isOwner(World world, int x, int z, Player ply) {
        return isOwner(world, x, z, ply.getUniqueId());
    }

    /**
     * Check if the provided player is the owner of the provided chunk
     *
     * @param chunk The Spigot chunk position.
     * @param ply   The UUID of the player.
     * @return Whether this player owns this chunk.
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
    public boolean isOwner(Chunk chunk, UUID ply) {
        return isOwner(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply);
    }

    /**
     * Check if the provided player is the owner of the provided chunk
     *
     * @param chunk The Spigot chunk position.
     * @param ply   The player.
     * @return Whether this player owns this chunk.
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
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
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public UUID getOwner(World world, int x, int z) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }


    /**
     * Get the UUID of the owner of the provided chunk.
     *
     * @param chunk The Spigot chunk position.
     * @return The UUID of the owner of this chunk.
     * @deprecated Use methods defined in {@link com.cjburkey.claimchunk.data.hyperdrive.chunk.ICCChunkHandler}
     */
    @Deprecated
    public UUID getOwner(Chunk chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    // TODO: MOVE TNT TO PER-PLAYER/PER-CHUNK SETTING HANDLER WHEN THAT COMES
    //       TO FRUITION.

    /**
     * Toggles whether TNT is enabled in the provided chunk.
     *
     * @param chunk The Spigot chunk position.
     * @return Whether TNT is now (after the toggle) enabled in this chunk.
     * @deprecated TODO
     */
    @Deprecated
    public boolean toggleTnt(Chunk chunk) {
        return dataHandler.toggleTnt(new ChunkPos(chunk));
    }

    /**
     * Checks whether TNT is enabled in the provided chunk.
     *
     * @param chunk The Spigot chunk position.
     * @return Whether TNT is currently enabled in this chunk.
     * @deprecated TODO
     */
    @Deprecated
    public boolean isTntEnabled(Chunk chunk) {
        return dataHandler.isTntEnabled(new ChunkPos(chunk));
    }

}
