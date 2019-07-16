package com.cjburkey.claimchunk.data.n;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.DataPlayer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IClaimChunkDataHandler {

    void init() throws Exception;

    void addClaimedChunk(ChunkPos pos, UUID player);

    void removeClaimedChunk(ChunkPos pos);

    boolean isChunkClaimed(ChunkPos pos);

    UUID getChunkOwner(ChunkPos pos);

    DataChunk[] getClaimedChunks();

    Set<Map.Entry<ChunkPos, UUID>> getClaimedChunksSet();

    void addPlayer(DataPlayer player);

    boolean hasPlayer(UUID player);

    DataPlayer getPlayer(UUID player);

    Collection<DataPlayer> getPlayers();

    void save() throws Exception;

    void load() throws Exception;

}
