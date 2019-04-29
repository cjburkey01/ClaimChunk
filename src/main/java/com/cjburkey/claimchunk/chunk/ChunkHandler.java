package com.cjburkey.claimchunk.chunk;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.data.IDataStorage;
import com.cjburkey.claimchunk.player.DataPlayer;
import com.cjburkey.claimchunk.player.PlayerHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class ChunkHandler {

    private final Map<ChunkPos, UUID> claimed = new ConcurrentHashMap<>();
    private final IDataStorage<DataChunk> data;

    public ChunkHandler(IDataStorage<DataChunk> storage) {
        data = storage;
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
    public ChunkPos claimChunk(String world, int x, int z, UUID player) {
        if (isClaimed(Objects.requireNonNull(ClaimChunk.getInstance().getServer().getWorld(world)), x, z)) {
            return null;
        }
        ChunkPos pos = new ChunkPos(world, x, z);
        claimed.put(pos, player);

        try {
            handleDynmapNewChunkClaimed(player);
        } catch (Exception ignored) {
            Utils.err("Failed to add new chunk to player's marker in Dynmap");
        }

        return pos;
    }

    private void handleDynmapNewChunkClaimed(UUID player) {
        PlayerHandler ph = ClaimChunk.getInstance().getPlayerHandler();
        DataPlayer ply = ph.getPlayer(player);
        // TODO: DYNMAP INTEGRATION
        /*if (ply != null) {
            if (!DynmapHandler.updateChunks(player, ply.lastIgn, getClaimedChunks(player), ply.color)) {
                Utils.log("Failed to create Dynmap region for player: %s", ply.lastIgn);
            }
        }*/
    }

    /**
     * Unclaims a specific chunk if that chunk is currently owned.
     *
     * @param world The current world.
     * @param x     The chunk x-coord.
     * @param z     The chunk z-coord.
     * @return Whether or not the chunk was unclaimed.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean unclaimChunk(World world, int x, int z) {
        if (!isClaimed(world, x, z)) return false;
        claimed.remove(new ChunkPos(world.getName(), x, z));
        return true;
    }

    public int getClaimed(UUID ply) {
        int i = 0;
        for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
            if (entry.getValue().equals(ply)) i++;
        }
        return i;
    }

    public ChunkPos[] getClaimedChunks(UUID ply) {
        Set<ChunkPos> chunks = new HashSet<>();
        for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
            if (entry.getValue().equals(ply)) chunks.add(entry.getKey());
        }
        return chunks.toArray(new ChunkPos[0]);
    }

    public boolean isClaimed(World world, int x, int z) {
        return claimed.containsKey(new ChunkPos(world.getName(), x, z));
    }

    public boolean isOwner(World world, int x, int z, UUID uuid) {
        return claimed.get(new ChunkPos(world.getName(), x, z)).equals(uuid);
    }

    public boolean isOwner(World world, int x, int z, Player ply) {
        return isOwner(world, x, z, ply.getUniqueId());
    }

    public UUID getOwner(World world, int x, int z) {
        return claimed.get(new ChunkPos(world.getName(), x, z));
    }

    public UUID getOwner(Chunk chunk) {
        return claimed.get(new ChunkPos(chunk));
    }

    public boolean isUnclaimed(Chunk chunk) {
        return !isClaimed(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public boolean hasNoChunks(UUID uniqueId) {
        for (UUID uuid : claimed.values()) {
            if (uniqueId.equals(uuid)) return false;
        }
        return true;
    }

    public void writeToDisk() throws Exception {
        data.clearData();
        for (Entry<ChunkPos, UUID> entry : claimed.entrySet()) {
            data.addData(new DataChunk(entry.getKey(), entry.getValue()));
        }
        data.saveData();
    }

    public void readFromDisk() throws Exception {
        data.reloadData();
        claimed.clear();
        for (DataChunk c : data.getData()) claimed.put(c.chunk, c.player);
    }

}
