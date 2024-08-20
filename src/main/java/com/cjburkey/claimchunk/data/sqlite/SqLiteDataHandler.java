package com.cjburkey.claimchunk.data.sqlite;

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
import java.util.stream.Collectors;

/*
 * I've actually just decided that we're gonna do it this way:
 * - SQLite backing database *file* similar to current MySQL integration (which will
 *   be removed and automatically converted).
 * - Have some intermediary layer that can Respond immediately and asynchronously update database.
 */

/**
 * The SHINY, NEW........data handler that tries to fix the data loss issues by which this project
 * has been plagued since its conception.
 *
 * <p>I hope this is better :)
 *
 * <p>Mutations are written to disk immediately, but data is kept in memory
 *
 * @since 0.0.25
 */
public class SqLiteDataHandler implements IClaimChunkDataHandler {

    @Getter private final File claimChunkDb;
    private boolean init = false;
    private HashMap<ChunkPos, DataChunk> claimedChunks;
    private HashMap<UUID, FullPlayerData> joinedPlayers;
    private SqLiteWrapper sqLiteWrapper;

    public SqLiteDataHandler(@NotNull File claimChunkDb) {
        this.claimChunkDb = claimChunkDb;
    }

    @Override
    public void init() {
        joinedPlayers = new HashMap<>();
        claimedChunks = new HashMap<>();
        sqLiteWrapper = new SqLiteWrapper(claimChunkDb, false);

        init = true;
    }

    @Override
    public boolean getHasInit() {
        return init;
    }

    @Override
    public void exit() {
        sqLiteWrapper.close();
    }

    @Override
    public void save() {
        // Don't do anything, we save as we go
    }

    @Override
    public void load() throws Exception {
        for (FullPlayerData player : sqLiteWrapper.getAllPlayers()) {
            joinedPlayers.putIfAbsent(player.player, player);
        }
        for (DataChunk chunk : SqLiteWrapper.getAllChunks()) {
            claimedChunks.putIfAbsent(chunk.chunk(), chunk);
        }
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        DataChunk chunk = new DataChunk(pos, player);
        claimedChunks.put(pos, chunk);
        sqLiteWrapper.addClaimedChunk(chunk);
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        Arrays.stream(chunks).forEach(chunk -> addClaimedChunk(chunk.chunk(), chunk.player()));
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        claimedChunks.remove(pos);
        sqLiteWrapper.removeClaimedChunk(pos);
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        return claimedChunks.containsKey(pos);
    }

    @Override
    public @Nullable UUID getChunkOwner(ChunkPos pos) {
        DataChunk chunk = claimedChunks.get(pos);
        return chunk == null ? null : chunk.player();
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        return claimedChunks.values().toArray(new DataChunk[0]);
    }

    @Override
    public void addPlayer(FullPlayerData playerData) {
        joinedPlayers.put(playerData.player, playerData);
        sqLiteWrapper.addPlayer(playerData);
    }

    @Override
    public void addPlayer(
            UUID player,
            String lastIgn,
            @Nullable String chunkName,
            long lastOnlineTime,
            boolean alerts,
            int extraMaxClaims) {
        addPlayer(
                new FullPlayerData(
                        player, lastIgn, chunkName, lastOnlineTime, alerts, extraMaxClaims));
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        // this::addPlayer calls SQLite mutation
        Arrays.stream(players).forEach(this::addPlayer);
    }

    @Override
    public @Nullable String getPlayerUsername(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        return ply == null ? null : ply.lastIgn;
    }

    @Override
    public @Nullable UUID getPlayerUUID(String username) {
        for (FullPlayerData ply : joinedPlayers.values()) {
            if (username.equals(ply.lastIgn)) return ply.player;
        }
        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.lastOnlineTime = time;
        sqLiteWrapper.setPlayerLastOnline(player, time);
    }

    @Override
    public void setPlayerChunkName(UUID player, @Nullable String name) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.chunkName = name;
        sqLiteWrapper.setPlayerChunkName(player, name);
    }

    @Override
    public @Nullable String getPlayerChunkName(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        return ply == null ? null : ply.chunkName;
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean receiveAlerts) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.alert = receiveAlerts;
        sqLiteWrapper.setPlayerReceiveAlerts(player, receiveAlerts);
    }

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        return ply != null && ply.alert;
    }

    @Override
    public void setPlayerExtraMaxClaims(UUID player, int maxClaims) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.extraMaxClaims = maxClaims;
        sqLiteWrapper.setPlayerExtraMaxClaims(player, maxClaims);
    }

    @Override
    public void addPlayerExtraMaxClaims(UUID player, int numToAdd) {
        // This method executes database modification
        setPlayerExtraMaxClaims(player, getPlayerExtraMaxClaims(player) + Math.abs(numToAdd));
    }

    @Override
    public void takePlayerExtraMaxClaims(UUID player, int numToTake) {
        // This method executes database modification
        setPlayerExtraMaxClaims(player, Math.max(0, getPlayerExtraMaxClaims(player) - numToTake));
    }

    @Override
    public int getPlayerExtraMaxClaims(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) return ply.extraMaxClaims;
        return 0;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return joinedPlayers.containsKey(player);
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        return joinedPlayers.values().stream()
                .map(FullPlayerData::toSimplePlayer)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        return joinedPlayers.values().toArray(new FullPlayerData[0]);
    }

    @Override
    public void grantPermissionFlagsGlobalDefault(UUID owner, String... flagNames) {
        FullPlayerData player = joinedPlayers.get(owner);
        if (player != null) {
            Collections.addAll(player.globalFlags, flagNames);
            sqLiteWrapper.grantPermissionFlagsGlobalDefault(owner, flagNames);
        }
    }

    @Override
    public void revokePermissionFlagsGlobalDefault(UUID owner, String... flagNames) {
        FullPlayerData player = joinedPlayers.get(owner);
        if (player != null) {
            Arrays.stream(flagNames).forEach(player.globalFlags::remove);
            sqLiteWrapper.revokePermissionFlagsGlobalDefault(owner, flagNames);
        }
    }

    @Override
    public void grantPermissionFlagsChunkDefault(UUID owner, ChunkPos chunk, String... flagNames) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null) {
            Collections.addAll(chunkData.defaultFlags(), flagNames);
            sqLiteWrapper.grantPermissionFlagsChunkDefault(owner, chunk, flagNames);
        }
    }

    @Override
    public void revokePermissionFlagsChunkDefault(UUID owner, ChunkPos chunk, String... flagNames) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null) {
            Arrays.stream(flagNames).forEach(chunkData.defaultFlags()::remove);
            sqLiteWrapper.revokePermissionFlagsChunkDefault(owner, chunk, flagNames);
        }
    }

    @Override
    public void grantPermissionFlagsPlayerDefault(UUID owner, UUID accessor, String... flagNames) {
        FullPlayerData player = joinedPlayers.get(owner);
        if (player != null) {
            Collections.addAll(
                    player.playerFlags.computeIfAbsent(accessor, ignored -> new HashSet<>()),
                    flagNames);
            sqLiteWrapper.grantPermissionFlagsPlayerDefault(owner, accessor, flagNames);
        }
    }

    @Override
    public void revokePermissionFlagsPlayerDefault(UUID owner, UUID accessor, String... flagNames) {
        FullPlayerData player = joinedPlayers.get(owner);
        if (player != null) {
            HashSet<String> plyFlags = player.playerFlags.get(accessor);
            if (plyFlags != null) {
                Arrays.stream(flagNames).forEach(plyFlags::remove);
                sqLiteWrapper.revokePermissionFlagsPlayerDefault(owner, accessor, flagNames);
            }
        }
    }

    @Override
    public void grantPermissionFlagsPlayerChunk(
            UUID owner, UUID accessor, ChunkPos chunk, String... flagNames) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null && chunkData.player().equals(owner)) {
            Collections.addAll(
                    chunkData.specificFlags().computeIfAbsent(accessor, ignored -> new HashSet<>()),
                    flagNames);
            sqLiteWrapper.grantPermissionFlagsPlayerChunk(owner, accessor, chunk, flagNames);
        }
    }

    @Override
    public void revokePermissionFlagsPlayerChunk(
            UUID owner, UUID accessor, ChunkPos chunk, String... flagNames) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null && chunkData.player().equals(owner)) {
            HashSet<String> plyFlags = chunkData.specificFlags().get(accessor);
            if (plyFlags != null) {
                Arrays.stream(flagNames).forEach(plyFlags::remove);
                sqLiteWrapper.revokePermissionFlagsPlayerChunk(owner, accessor, chunk, flagNames);
            }
        }
    }
}
