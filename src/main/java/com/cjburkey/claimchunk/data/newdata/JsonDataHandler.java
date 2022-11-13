package com.cjburkey.claimchunk.data.newdata;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.cjburkey.claimchunk.player.Pre0024FullPlayerData;
import com.cjburkey.claimchunk.player.SimplePlayerData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonDataHandler implements IClaimChunkDataHandler {

    // Matches: `FILENAME_yyyy-MM-dd-HH-mm-ss-SSS.json`
    private static final Pattern BACKUP_PATTERN =
            Pattern.compile("^\\w*?_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{3}\\.json$");

    private final HashMap<ChunkPos, DataChunk> claimedChunks = new HashMap<>();
    private final HashMap<UUID, FullPlayerData> joinedPlayers = new HashMap<>();

    private final ClaimChunk claimChunk;
    private final File claimedChunksFile;
    private final File joinedPlayersFile;
    private boolean init;

    public JsonDataHandler(ClaimChunk claimChunk, File claimedChunksFile, File joinedPlayersFile) {
        this.claimChunk = claimChunk;
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

        if (claimedChunks.values().stream().allMatch(c -> c.playerPermissions == null)) {
            // If all playerPermissions are null, then the JSON files are in the pre 0.0.24 format
            loadPre0024Data();
        } else if (joinedPlayersFile != null && joinedPlayersFile.exists()) {
            joinedPlayers.clear();
            for (FullPlayerData player : loadJsonFile(joinedPlayersFile, FullPlayerData[].class)) {
                joinedPlayers.put(player.player, player);
            }
        }
    }

    public void deleteFiles() {
        if (claimedChunksFile != null && !claimedChunksFile.delete())
            Utils.err("Failed to delete claimed chunks file");
        if (joinedPlayersFile != null && !joinedPlayersFile.delete())
            Utils.err("Failed to delete joined players file");
    }

    void clearData() {
        claimedChunks.clear();
        joinedPlayers.clear();
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        claimedChunks.put(pos, new DataChunk(pos, player, new HashMap<>(), false));
    }

    private void addClaimedChunkWithPerms(
            ChunkPos pos, UUID player, Map<UUID, ChunkPlayerPermissions> playerPermissions) {
        claimedChunks.put(pos, new DataChunk(pos, player, playerPermissions, false));
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        for (DataChunk chunk : chunks)
            addClaimedChunkWithPerms(chunk.chunk, chunk.player, chunk.playerPermissions);
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
        return this.claimedChunks.entrySet().stream()
                .map(
                        claimedChunk ->
                                new DataChunk(
                                        claimedChunk.getKey(),
                                        claimedChunk.getValue().player,
                                        claimedChunk.getValue().playerPermissions,
                                        claimedChunk.getValue().tnt))
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
    public void addPlayer(
            UUID player, String lastIgn, String chunkName, long lastOnlineTime, boolean alerts) {
        joinedPlayers.put(
                player, new FullPlayerData(player, lastIgn, chunkName, lastOnlineTime, alerts));
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
    public void givePlayerAccess(
            ChunkPos chunk, UUID accessor, ChunkPlayerPermissions permissions) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null) chunkData.playerPermissions.put(accessor, permissions);
    }

    @Override
    public void takePlayerAccess(ChunkPos chunk, UUID accessor) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null) chunkData.playerPermissions.remove(accessor);
    }

    @Override
    public Map<UUID, ChunkPlayerPermissions> getPlayersWithAccess(ChunkPos chunk) {
        DataChunk chunkData = claimedChunks.get(chunk);
        if (chunkData != null) return chunkData.playerPermissions;
        return null;
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
        return joinedPlayers.values().stream()
                .map(FullPlayerData::toSimplePlayer)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        return joinedPlayers.values().toArray(new FullPlayerData[0]);
    }

    private Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        return builder.serializeNulls().create();
    }

    private void saveJsonFile(File file, Object data) throws Exception {
        // Don't continue if the file isn't valid.
        if (file == null || file.getParentFile() == null) {
            return;
        }

        // If the folder in which this file resides doesn't exist and we can't create it, throw an
        // error.
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Failed to create directory: " + file.getParentFile());
        }

        // Try to save the backup if the file already exists and the server has backups enabled.
        if (file.exists() && claimChunk.getConfigHandler().getKeepJsonBackups()) {
            tryToSaveBackup(file);
        }

        // Write the file data, overwriting existing data in the file.
        Files.write(
                file.toPath(),
                Collections.singletonList(getGson().toJson(data)),
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void tryToSaveBackup(@NotNull File existingFile) throws IOException {
        // Get the filename without the extension.
        String filename =
                existingFile.getName().substring(0, existingFile.getName().lastIndexOf('.'));

        // Get the backups folder.
        File backupFolder = new File(existingFile.getParentFile(), "/backups/" + filename);

        // Get the max backup age
        int maxAgeInMinutes = claimChunk.getConfigHandler().getDeleteOldBackupsAfterMinutes();

        // The last time the files was backed up
        long lastBackupTime = 0L;

        // If the backup folder already exists and the maxAge is larger than 0 (deleting old backups
        // is
        // enabled),
        // then try to clear some out.
        if (backupFolder.exists() && maxAgeInMinutes > 0) {
            // Get the newest backup
            for (File ff : Objects.requireNonNull(backupFolder.listFiles())) {
                if (ff.lastModified() > lastBackupTime) {
                    lastBackupTime = ff.lastModified();
                }
            }

            // Try to clean out old backup versions
            if (!BackupCleaner.deleteBackups(backupFolder, BACKUP_PATTERN, maxAgeInMinutes)) {
                Utils.err("Failed to delete old backup files");
            }
        } else if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            // Try to create the backups folder if it doesn't exist.
            throw new IOException("Failed to create directory: " + backupFolder);
        }

        // Make sure backups don't happen too frequently
        long backupFrequencyInMins = claimChunk.getConfigHandler().getMinBackupIntervalInMinutes();
        if (backupFrequencyInMins <= 0
                || System.currentTimeMillis() - lastBackupTime >= 60000 * backupFrequencyInMins) {
            doBackup(existingFile);
        }
    }

    private void doBackup(File existingFile) throws IOException {
        String filename =
                existingFile.getName().substring(0, existingFile.getName().lastIndexOf('.'));

        // Get the backups folder.
        File backupFolder = new File(existingFile.getParentFile(), "/backups/" + filename);

        // Determine the new name for the backup file.
        String backupName =
                String.format(
                        "%s_%s.json",
                        filename,
                        new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()));

        // Try to move the file into its backup location.
        if (backupFolder.exists() || backupFolder.mkdirs()) {
            Files.move(
                    existingFile.toPath(),
                    new File(backupFolder, backupName).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } else {
            throw new IOException("Failed to create directory: " + backupFolder);
        }

        Utils.debug("Created backup \"%s\"", backupName);
    }

    private <T> T[] loadJsonFile(File file, Class<T[]> referenceClass) throws Exception {
        return getGson()
                .fromJson(
                        String.join("", Files.readAllLines(file.toPath(), StandardCharsets.UTF_8))
                                .trim(),
                        referenceClass);
    }

    private void loadPre0024Data() throws Exception {
        if (joinedPlayersFile != null && joinedPlayersFile.exists()) {
            Utils.debug("Converting JSON data to 0.0.24+ format");
            joinedPlayers.clear();

            Map<UUID, List<DataChunk>> chunksSortedByOwner = new HashMap<>();
            for (DataChunk chunk : claimedChunks.values()) {
                if (!chunksSortedByOwner.containsKey(chunk.player)) {
                    chunksSortedByOwner.put(chunk.player, new ArrayList<>());
                }
                chunksSortedByOwner.get(chunk.player).add(chunk);
            }

            for (Pre0024FullPlayerData player :
                    loadJsonFile(joinedPlayersFile, Pre0024FullPlayerData[].class)) {
                joinedPlayers.put(
                        player.player,
                        new FullPlayerData(
                                player.player,
                                player.lastIgn,
                                player.chunkName,
                                player.lastOnlineTime,
                                player.alert));
                // Grant default permissions on all this player's chunks to all players in
                // "permitted"
                for (DataChunk chunk :
                        chunksSortedByOwner.getOrDefault(player.player, new ArrayList<>())) {
                    chunk.playerPermissions = new HashMap<>();
                    for (UUID permittedPlayer : player.permitted) {
                        chunk.playerPermissions.put(
                                permittedPlayer,
                                ChunkPlayerPermissions.fromPermissionsMap(
                                        Utils.getDefaultPermissionsMap()));
                    }
                }
            }

            // Backup existing files
            doBackup(joinedPlayersFile);
            doBackup(claimedChunksFile);
        }
    }
}
