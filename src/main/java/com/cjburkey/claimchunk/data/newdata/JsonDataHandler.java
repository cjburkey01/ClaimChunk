package com.cjburkey.claimchunk.data.newdata;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.cjburkey.claimchunk.player.SimplePlayerData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class JsonDataHandler implements IClaimChunkDataHandler {

    // Matches: `FILENAME_yyyy-MM-dd-HH-mm-ss-SSS.json`
    private static final Pattern BACKUP_PATTERN = Pattern.compile("^\\w*?_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{3}\\.json$");

    private final HashMap<ChunkPos, DataChunk> claimedChunks = new HashMap<>();
    private final HashMap<UUID, FullPlayerData> joinedPlayers = new HashMap<>();

    private final File claimedChunksFile;
    private final File joinedPlayersFile;
    private boolean init;

    public JsonDataHandler(File claimedChunksFile, File joinedPlayersFile) {
        this.claimedChunksFile = claimedChunksFile;
        this.joinedPlayersFile = joinedPlayersFile;
    }

    @Override
    public void init() {
        init = true;
        // No initialization necessary
    }

    @Override
    public boolean getHasInit() {
        return init;
    }

    @Override
    public void exit() {
        // No cleanup necessary
    }

    @Override
    public void save() throws Exception {
        if (claimedChunksFile != null) saveJsonFile(claimedChunksFile, getClaimedChunks());
        if (joinedPlayersFile != null) saveJsonFile(joinedPlayersFile, joinedPlayers.values());
    }

    @Override
    public void load() throws Exception {
        if (claimedChunksFile != null && claimedChunksFile.exists()) {
            claimedChunks.clear();
            for (DataChunk chunk : loadJsonFile(claimedChunksFile, DataChunk[].class)) {
                claimedChunks.put(chunk.chunk, chunk);
            }
        }

        if (joinedPlayersFile != null && joinedPlayersFile.exists()) {
            joinedPlayers.clear();
            for (FullPlayerData player : loadJsonFile(joinedPlayersFile, FullPlayerData[].class)) {
                joinedPlayers.put(player.player, player);
            }
        }
    }

    public void deleteFiles() {
        if (claimedChunksFile != null && !claimedChunksFile.delete()) Utils.err("Failed to delete claimed chunks file");
        if (joinedPlayersFile != null && !joinedPlayersFile.delete()) Utils.err("Failed to delete joined players file");
    }

    void clearData() {
        claimedChunks.clear();
        joinedPlayers.clear();
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        claimedChunks.put(pos, new DataChunk(pos, player, false));
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        for (DataChunk chunk : chunks) addClaimedChunk(chunk.chunk, chunk.player);
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        claimedChunks.remove(pos);
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        return claimedChunks.containsKey(pos);
    }

    @Override
    @Nullable
    public UUID getChunkOwner(ChunkPos pos) {
        if (claimedChunks.containsKey(pos)) {
            return claimedChunks.get(pos).player;
        }
        return null;
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        return this.claimedChunks
                .entrySet()
                .stream()
                .map(claimedChunk -> new DataChunk(claimedChunk.getKey(), claimedChunk.getValue().player, claimedChunk.getValue().tnt))
                .toArray(DataChunk[]::new);
    }

    @Override
    public boolean toggleTnt(ChunkPos pos) {
        DataChunk chunk = claimedChunks.get(pos);
        if (chunk == null) return false;
        return (chunk.tnt = !chunk.tnt);
    }

    @Override
    public boolean isTntEnabled(ChunkPos pos) {
        return claimedChunks.containsKey(pos) && claimedChunks.get(pos).tnt;
    }

    @Override
    public void addPlayer(UUID player,
                          String lastIgn,
                          Set<UUID> permitted,
                          String chunkName,
                          long lastOnlineTime,
                          boolean alerts) {
        joinedPlayers.put(player, new FullPlayerData(player, lastIgn, permitted, chunkName, lastOnlineTime, alerts));
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        for (FullPlayerData player : players) addPlayer(player);
    }

    @Override
    @Nullable
    public String getPlayerUsername(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        return ply == null ? null : ply.lastIgn;
    }

    @Override
    @Nullable
    public UUID getPlayerUUID(String username) {
        for (FullPlayerData player : joinedPlayers.values()) {
            if (player.lastIgn.equals(username)) return player.player;
        }
        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.lastOnlineTime = time;
    }

    @Override
    public void setPlayerChunkName(UUID player, String name) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.chunkName = name;
    }

    @Override
    @Nullable
    public String getPlayerChunkName(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) return ply.chunkName;
        return null;
    }

    @Override
    public void setPlayerAccess(UUID owner, UUID accessor, boolean access) {
        FullPlayerData ply = joinedPlayers.get(owner);
        if (ply != null) {
            if (access) ply.permitted.add(accessor);
            else ply.permitted.remove(accessor);
        }
    }

    @Override
    public void givePlayersAcess(UUID owner, UUID[] accessors) {
        FullPlayerData ply = joinedPlayers.get(owner);
        if (ply != null) Collections.addAll(ply.permitted, accessors);
    }

    @Override
    public void takePlayersAcess(UUID owner, UUID[] accessors) {
        FullPlayerData ply = joinedPlayers.get(owner);
        if (ply != null) ply.permitted.removeAll(Arrays.asList(accessors));
    }

    @Override
    public UUID[] getPlayersWithAccess(UUID owner) {
        FullPlayerData ply = joinedPlayers.get(owner);
        if (ply != null) {
            return ply.permitted.toArray(new UUID[0]);
        }
        return new UUID[0];
    }

    @Override
    public boolean playerHasAccess(UUID owner, UUID accessor) {
        FullPlayerData ply = joinedPlayers.get(owner);
        if (ply != null) {
            return ply.permitted.contains(accessor);
        }
        return false;
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean alert) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) ply.alert = alert;
    }

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        FullPlayerData ply = joinedPlayers.get(player);
        if (ply != null) {
            return ply.alert;
        }
        return false;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return joinedPlayers.containsKey(player);
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        return joinedPlayers.values()
                .stream()
                .map(FullPlayerData::toSimplePlayer)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        return joinedPlayers.values().toArray(new FullPlayerData[0]);
    }

    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        return builder
                .serializeNulls()
                .create();
    }

    private void saveJsonFile(File file, Object data) throws Exception {
        // Don't continue if the file isn't valid.
        if (file == null || file.getParentFile() == null) {
            return;
        }

        // If the folder in which this file resides doesn't exist and we can't create it, throw an error.
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Failed to create directory: " + file.getParentFile());
        }

        // If the file already exists and we have backups enabled.
        if (file.exists() && Config.getBool("data", "keepJsonBackups")) {
            // Get the filename without the extension.
            String filename = file.getName().substring(0, file.getName().lastIndexOf('.'));

            // Get the backups folder.
            File backupFolder = new File(file.getParentFile(), "/backups/" + filename);

            // Get the max backup age
            int maxAge = Config.getInt("", "");

            // If the backup folder already exists and the maxAge is larger than 0 (deleting old backups is enabled),
            // then try to clear some out.
            if (backupFolder.exists() && maxAge > 0) {
                // Try to clean out old backup versions
                if (!BackupCleaner.deleteBackups(backupFolder, BACKUP_PATTERN, maxAge)) {
                    Utils.err("Failed to delete old backup files");
                }
            } else if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                // Try to create the backups folder if it doesn't exist.
                throw new IOException("Failed to create directory: " + backupFolder);
            }

            // Determine the new name for the backup file.
            String backupName = String.format(
                    "%s_%s.json",
                    filename,
                    new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())
            );

            // Try to move the file into its backup location.
            Files.move(
                    file.toPath(),
                    new File(backupFolder, backupName).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

        }

        // Try to delete the old file if backups aren't enabled (because the file wouldn't be moved).
        if (file.exists() && !file.delete()) {
            throw new IOException("Failed to clear old offline JSON data: " + file);
        }

        // Write the new file data
        Files.write(file.toPath(), Collections.singletonList(getGson().toJson(data)),
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);
    }

    private <T> T[] loadJsonFile(File file, Class<T[]> referenceClass) throws Exception {
        return getGson().fromJson(String.join("", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)).trim(), referenceClass);
    }

}
