package com.cjburkey.claimchunk.data.n;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.DataPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class JsonDataHandler implements IClaimChunkDataHandler {

    private final HashMap<ChunkPos, UUID> claimedChunks = new HashMap<>();
    private final HashMap<UUID, DataPlayer> joinedPlayers = new HashMap<>();

    private final File claimedChunksFile;
    private final File joinedPlayersFile;

    public JsonDataHandler(File claimedChunksFile, File joinedPlayersFile) {
        this.claimedChunksFile = claimedChunksFile;
        this.joinedPlayersFile = joinedPlayersFile;
    }

    @Override
    public void init() {
        // No initialization necessary
    }

    public void addClaimedChunk(ChunkPos pos, UUID player) {
        claimedChunks.put(pos, player);
    }

    public void removeClaimedChunk(ChunkPos pos) {
        claimedChunks.remove(pos);
    }

    public boolean isChunkClaimed(ChunkPos pos) {
        return claimedChunks.containsKey(pos);
    }

    public UUID getChunkOwner(ChunkPos pos) {
        return claimedChunks.get(pos);
    }

    public DataChunk[] getClaimedChunks() {
        return this.claimedChunks
                .entrySet()
                .stream()
                .map(claimedChunk -> new DataChunk(claimedChunk.getKey(), claimedChunk.getValue()))
                .toArray(DataChunk[]::new);
    }

    public Set<Map.Entry<ChunkPos, UUID>> getClaimedChunksSet() {
        return claimedChunks.entrySet();
    }

    public void addPlayer(DataPlayer player) {
        joinedPlayers.put(player.player, player);
    }

    public boolean hasPlayer(UUID player) {
        return joinedPlayers.containsKey(player);
    }

    public DataPlayer getPlayer(UUID player) {
        return joinedPlayers.get(player);
    }

    public Collection<DataPlayer> getPlayers() {
        return joinedPlayers.values();
    }

    public void save() throws Exception {
        saveJsonFile(claimedChunksFile, getClaimedChunks());
        saveJsonFile(joinedPlayersFile, getPlayers());
    }

    public void load() throws Exception {
        claimedChunks.clear();
        for (DataChunk chunk : loadJsonFile(claimedChunksFile, DataChunk[].class)) {
            claimedChunks.put(chunk.chunk, chunk.player);
        }

        joinedPlayers.clear();
        for (DataPlayer player : loadJsonFile(joinedPlayersFile, DataPlayer[].class)) {
            joinedPlayers.put(player.player, player);
        }
    }

    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        return builder
                .serializeNulls()
                .create();
    }

    private void saveJsonFile(File file, Object data) throws Exception {
        if (file == null || file.getParentFile() == null) {
            return;
        }
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Failed to create directory: " + file.getParentFile());
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to clear old offline JSON data: " + file);
        }
        Files.write(file.toPath(), Collections.singletonList(getGson().toJson(data)),
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
    }

    private <T> T[] loadJsonFile(File file, Class<T[]> referenceClass) throws Exception {
        return getGson().fromJson(String.join("", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)).trim(), referenceClass);
    }

}
