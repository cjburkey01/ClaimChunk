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
import java.util.*;

/*
 * I've actually just decided that we're gonna do it this way:
 * - SQLite backing database *file* similar to current MySQL integration (which will
 *   be removed and automatically converted).
 * - Have some intermediary layer that can
 *   - Keep some regions in memory and unload when no players are within them
 *     for a minute or two.
 *   - Respond immediately and asynchronously update database.
 */

/**
 * The SHINY, NEW........data handler that tries to fix the data loss issues by which this project
 * has been plagued since its conception.
 *
 * <p>I hope this is better :)
 *
 * @since 0.0.25
 */
public class JournaledDataHandler implements IClaimChunkDataHandler {

    @Getter private final File claimChunkDb;
    private boolean init = false;
    private HashMap<UUID, FullPlayerData> joinedPlayers;
    private HashMap<RegionPos, ClaimRegion> claimRegions;
    private SqLiteWrapper sqLiteWrapper;

    public JournaledDataHandler(@NotNull File claimChunkDb) {
        this.claimChunkDb = claimChunkDb;
    }

    @Override
    public void init() {
        joinedPlayers = new HashMap<>();
        claimRegions = new HashMap<>();
        sqLiteWrapper = new SqLiteWrapper(claimChunkDb);

        init = true;
    }

    @Override
    public boolean getHasInit() {
        return init;
    }

    @Override
    public void exit() {}

    @Override
    public void save() {
        // Don't do anything, async handler should have us safe
    }

    @Override
    public void load() throws Exception {
        // TODO: THIS
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

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
            int extraMaxClaims) {
        joinedPlayers.put(
                player,
                new FullPlayerData(
                        player, lastIgn, chunkName, lastOnlineTime, alerts, extraMaxClaims));

        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        Arrays.stream(players).forEach(player -> joinedPlayers.put(player.player, player));

        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public @Nullable String getPlayerUsername(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        return ply == null ? null : ply.lastIgn;
    }

    @Override
    public @Nullable UUID getPlayerUUID(String username) {
        // TODO: THIS

        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.lastOnlineTime = time;

        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void setPlayerChunkName(UUID player, @Nullable String name) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.chunkName = name;

        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public @Nullable String getPlayerChunkName(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        return ply == null ? null : ply.chunkName;
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean alerts) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        return false;
    }

    @Override
    public void setPlayerExtraMaxClaims(UUID player, int maxClaims) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void addPlayerExtraMaxClaims(UUID player, int numToAdd) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void takePlayerExtraMaxClaims(UUID player, int numToTake) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

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
            ChunkPos chunk, UUID accessor, ChunkPlayerPermissions permissions) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public void takePlayerAccess(ChunkPos chunk, UUID accessor) {
        // TODO: mutating methods must call sqLiteWrapper methods
    }

    @Override
    public Map<UUID, ChunkPlayerPermissions> getPlayersWithAccess(ChunkPos chunk) {
        return null;
    }
}
