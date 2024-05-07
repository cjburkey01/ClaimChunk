package com.cjburkey.claimchunk.data.journaled;

import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.cjburkey.claimchunk.player.SimplePlayerData;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * The SHINY, NEW........data handler that tries to fix the data loss issues by which this project
 * has been plagued since its conception.
 * <p>
 * I've actually just decided that we're gonna do it this way:
 *  - SQLite backing database similar to current MySQL integration (which will
 *    be removed and automatically converted).
 *  - Keep some regions in memory and unload when no players are within them
 *    for a minute or two.
 * I hope this is better :)
 *
 * @since 0.0.25
 */
public class JournaledDataHandler implements IClaimChunkDataHandler {

    private boolean init = false;

    @Getter private final File claimChunkDb;

    public JournaledDataHandler(@NotNull File claimChunkDb) {
        this.claimChunkDb = claimChunkDb;
    }

    @Override
    public void init() {
        init = true;
    }

    @Override
    public boolean getHasInit() {
        return init;
    }

    @Override
    public void exit() {}

    @Override
    public void save() throws Exception {}

    @Override
    public void load() throws Exception {}

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {}

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {}

    @Override
    public void removeClaimedChunk(ChunkPos pos) {}

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        return false;
    }

    @Override
    public @Nullable UUID getChunkOwner(ChunkPos pos) {
        return null;
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        return new DataChunk[0];
    }

    @Override
    public boolean toggleTnt(ChunkPos pos) {
        return false;
    }

    @Override
    public boolean isTntEnabled(ChunkPos pos) {
        return false;
    }

    @Override
    public void addPlayer(
            UUID player,
            String lastIgn,
            @Nullable String chunkName,
            long lastOnlineTime,
            boolean alerts,
            int maxClaims) {}

    @Override
    public void addPlayers(FullPlayerData[] players) {}

    @Override
    public @Nullable String getPlayerUsername(UUID player) {
        return null;
    }

    @Override
    public @Nullable UUID getPlayerUUID(String username) {
        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {}

    @Override
    public void setPlayerChunkName(UUID player, @Nullable String name) {}

    @Override
    public @Nullable String getPlayerChunkName(UUID player) {
        return null;
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean alerts) {}

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        return false;
    }

    @Override
    public void setPlayerExtraMaxClaims(UUID player, int maxClaims) {}

    @Override
    public void addPlayerExtraMaxClaims(UUID player, int numToAdd) {}

    @Override
    public void takePlayerExtraMaxClaims(UUID player, int numToTake) {}

    @Override
    public int getPlayerExtraMaxClaims(UUID player) {
        return 0;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return false;
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        return null;
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        return new FullPlayerData[0];
    }

    @Override
    public void givePlayerAccess(
            ChunkPos chunk, UUID accessor, ChunkPlayerPermissions permissions) {}

    @Override
    public void takePlayerAccess(ChunkPos chunk, UUID accessor) {}

    @Override
    public Map<UUID, ChunkPlayerPermissions> getPlayersWithAccess(ChunkPos chunk) {
        return null;
    }
}
