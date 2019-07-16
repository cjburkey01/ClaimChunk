package com.cjburkey.claimchunk.data.n;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.DataPlayer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// TODO: NOTHING IS IMPLEMENTED YET: THIS IS A DUMMY CLASS
@SuppressWarnings("unused")
public class MySQLDataHandler implements IClaimChunkDataHandler {

    @Override
    public void init() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID getChunkOwner(ChunkPos pos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<ChunkPos, UUID>> getClaimedChunksSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPlayer(DataPlayer player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPlayer(UUID player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataPlayer getPlayer(UUID player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<DataPlayer> getPlayers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException();
    }

}
