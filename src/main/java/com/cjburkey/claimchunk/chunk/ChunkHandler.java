package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ChunkHandler {

    private final IClaimChunkDataHandler dataHandler;

    public ChunkHandler(IClaimChunkDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     *
     * @param world  The current world.
     * @param x      The chunk x-coord.
     * @param z      The chunk z-coord.
     * @param player The player for whom to claim the chunk.
     * @return The chunk position variable
     */
    public ChunkPos claimChunk(World world, int x, int z, UUID player) {
        return claimChunk(world.getName(), x, z, player);
    }

    /**
     * Claims a specific chunk for a player if that chunk is not already owned.
     *
     * @param world  The current world.
     * @param x      The chunk x-coord.
     * @param z      The chunk z-coord.
     * @param player The player for whom to claim the chunk.
     * @return The chunk posautomaticUnclaimSecondsition variable
     */
    private ChunkPos claimChunk(String world, int x, int z, UUID player) {
        if (isClaimed(Objects.requireNonNull(ClaimChunk.getInstance().getServer().getWorld(world)), x, z)) {
            return null;
        }
        ChunkPos pos = new ChunkPos(world, x, z);
        dataHandler.addClaimedChunk(pos, player);

        return pos;
    }

    /**
     * Tries to unclaim a specific chunk and does nothing if the chunk is unowned.
     *
     * @param world The current world.
     * @param x     The chunk x-coord.
     * @param z     The chunk z-coord.
     */
    public void unclaimChunk(World world, int x, int z) {
        if (isClaimed(world, x, z)) {
            dataHandler.removeClaimedChunk(new ChunkPos(world.getName(), x, z));
        }
    }

    public int getClaimed(UUID ply) {
        int i = 0;
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            if (chunk.player.equals(ply)) i++;
        }
        return i;
    }

    public ChunkPos[] getClaimedChunks(UUID ply) {
        Set<ChunkPos> chunks = new HashSet<>();
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            if (chunk.player.equals(ply)) chunks.add(chunk.chunk);
        }
        return chunks.toArray(new ChunkPos[0]);
    }

    public boolean getHasAllFreeChunks(UUID ply) {
        int total = 0;
        int max = Config.getInt("economy", "firstFreeChunks");
        for (DataChunk chunk : dataHandler.getClaimedChunks()) {
            if (chunk.player.equals(ply) && ++total >= max) return true;
        }
        return false;
    }

    public boolean isClaimed(World world, int x, int z) {
        return dataHandler.isChunkClaimed(new ChunkPos(world.getName(), x, z));
    }

    public boolean isClaimed(Chunk chunk) {
        return isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public boolean isOwner(World world, int x, int z, UUID uuid) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        UUID owner = dataHandler.getChunkOwner(pos);
        return owner != null && owner.equals(uuid);
    }

    public boolean isOwner(World world, int x, int z, Player ply) {
        return isOwner(world, x, z, ply.getUniqueId());
    }

    public boolean isOwner(Chunk chunk, Player ply) {
        return isOwner(chunk.getWorld(), chunk.getX(), chunk.getZ(), ply);
    }

    public UUID getOwner(World world, int x, int z) {
        ChunkPos pos = new ChunkPos(world.getName(), x, z);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    public UUID getOwner(Chunk chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        return !dataHandler.isChunkClaimed(pos) ? null : dataHandler.getChunkOwner(pos);
    }

    public boolean isUnclaimed(Chunk chunk) {
        return !isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public boolean toggleTnt(Chunk chunk) {
        return dataHandler.toggleTnt(new ChunkPos(chunk));
    }

    public boolean isTntEnabled(Chunk chunk) {
        return dataHandler.isTntEnabled(new ChunkPos(chunk));
    }

}
