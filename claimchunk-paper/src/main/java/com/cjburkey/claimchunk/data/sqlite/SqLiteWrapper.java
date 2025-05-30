package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteDataSource;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: REPLACE CHUNK_ID KEY WITH CHUNK COORDINATE KEY

public record SqLiteWrapper(File dbFile, boolean usesTransactionManager) implements Closeable {

    private static final String SELECT_CHUNK_ID_SQL =
            """
            (
                SELECT chunk_id
                FROM chunk_data
                WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
            )
            """;
    private static final Pattern SELECT_CHUNK_ID_SQL_PATTERN =
            Pattern.compile(Pattern.quote("%%SELECT_CHUNK_ID_SQL%%"));

    public SqLiteWrapper(@NotNull File dbFile, boolean usesTransactionManager) {
        this.dbFile = dbFile;
        this.usesTransactionManager = usesTransactionManager;

        try {
            //noinspection ResultOfMethodCallIgnored
            dbFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to create new database file even though it didn't exist!", e);
        }

        // Make sure the SQLite driver exists and get it in the classpath
        // for the DriverManager to search.
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFile);
        if (usesTransactionManager) q2o.initializeTxSimple(dataSource);
        else q2o.initializeTxNone(dataSource);

        // Initialize the tables and perform any changes to them
        SqLiteTableMigrationManager.go();
    }

    @Override
    public void close() {
        q2o.deinitialize();
    }

    // -- DATABASE INTEGRATIONS! -- //

    public void addClaimedChunk(DataChunk chunk) {
        SqlClosure.sqlExecute(
                connection -> {
                    // Make sure the player already exists!
                    // If there isn't a player with their UUID as a primary key, I'm pretty sure
                    // inserting into the chunk data would fail. This only really matters during
                    // loading, as there could be a chance the player is missing, somehow?
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT OR IGNORE INTO player_data (
                                        player_uuid,
                                        last_ign,
                                        chunk_name,
                                        last_online_time,
                                        alerts_enabled,
                                        extra_max_claims
                                    ) VALUES (
                                        ?, "", NULL, 0, TRUE, 0
                                    )
                                    """)) {
                        statement.setString(1, chunk.player().toString());
                        statement.execute();
                    }

                    // Add the chunk
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT INTO chunk_data (
                                        chunk_world,
                                        chunk_x,
                                        chunk_z,
                                        owner_uuid
                                    ) VALUES (
                                        ?, ?, ?, ?
                                    )
                                    """)) {
                        int next = setChunkPosParams(statement, 1, chunk.chunk());
                        statement.setString(next, chunk.player().toString());
                        statement.execute();
                    }

                    return null;
                });
    }

    public void removeClaimedChunk(ChunkPos chunk) {
        SqlClosure.sqlExecute(
                connection -> {
                    // Remove all granted permissions for the chunk
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    chunkIdQuery(
                                            """
                                            DELETE FROM permission_flags
                                            WHERE chunk_id=%%SELECT_CHUNK_ID_SQL%%
                                            """))) {
                        setChunkPosParams(statement, 1, chunk);
                        statement.execute();
                    }

                    // Remove chunk
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    DELETE FROM chunk_data
                                    WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
                                    """)) {
                        setChunkPosParams(statement, 1, chunk);
                        statement.execute();
                    }

                    return null;
                });
    }

    // The provided player data will replace an existing row
    public void addPlayer(FullPlayerData playerData) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT OR REPLACE INTO player_data (
                                        player_uuid,
                                        last_ign,
                                        chunk_name,
                                        last_online_time,
                                        alerts_enabled,
                                        extra_max_claims
                                    ) VALUES (
                                        ?, ?, ?, ?, ?, ?
                                    )
                                    """)) {
                        statement.setString(1, playerData.player.toString());
                        statement.setString(2, playerData.lastIgn);
                        statement.setString(3, playerData.chunkName);
                        statement.setLong(4, playerData.lastOnlineTime);
                        statement.setBoolean(5, playerData.alert);
                        statement.setInt(6, playerData.extraMaxClaims);
                        statement.execute();
                        return null;
                    }
                });
    }

    public void setPlayerLastOnline(UUID player, long time) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    UPDATE player_data
                                    SET last_online_time=?
                                    WHERE player_uuid=?
                                    """)) {
                        statement.setLong(1, time);
                        statement.setString(2, player.toString());
                        statement.execute();
                        return null;
                    }
                });
    }

    public void setPlayerChunkName(UUID player, String chunkName) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    UPDATE player_data
                                    SET chunk_name=?
                                    WHERE player_uuid=?
                                    """)) {
                        statement.setString(1, chunkName);
                        statement.setString(2, player.toString());
                        statement.execute();
                        return null;
                    }
                });
    }

    public void setPlayerReceiveAlerts(UUID player, boolean receiveAlerts) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    UPDATE player_data
                                    SET receiveAlerts=?
                                    WHERE player_uuid=?
                                    """)) {
                        statement.setBoolean(1, receiveAlerts);
                        statement.setString(2, player.toString());
                        statement.execute();
                        return null;
                    }
                });
    }

    public void setPlayerExtraMaxClaims(UUID player, int extraMaxClaims) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    UPDATE player_data
                                    SET extra_max_claims=?
                                    WHERE player_uuid=?
                                    """)) {
                        statement.setInt(1, extraMaxClaims);
                        statement.setString(2, player.toString());
                        statement.execute();
                        return null;
                    }
                });
    }

    // -- TODO: TEST ALL THIS

    public void setPermissionFlags(
            @NotNull UUID owner,
            @Nullable UUID accessor,
            @Nullable ChunkPos chunk,
            @NotNull HashMap<String, Boolean> newFlags) {
        String valueList =
                newFlags.values().stream()
                        .map(
                                ignored ->
                                        "(?,"
                                                + (accessor != null ? "?" : "''")
                                                + ","
                                                + (chunk != null ? SELECT_CHUNK_ID_SQL : "-1")
                                                + ",?,?)")
                        .collect(Collectors.joining(","));
        String sql =
                """
                INSERT INTO permission_flags (
                    player_uuid,
                    other_player_uuid,
                    chunk_id,
                    flag_name,
                    allow_deny
                ) VALUES \s\
                """
                        + valueList
                        + """
                        ON CONFLICT (
                            player_uuid,
                            other_player_uuid,
                            chunk_id,
                            flag_name
                        ) DO UPDATE SET allow_deny=excluded.allow_deny
                        """;

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        int param = 1;
                        for (Map.Entry<String, Boolean> entry : newFlags.entrySet()) {
                            statement.setString(param++, owner.toString());
                            if (accessor != null) {
                                statement.setString(param++, accessor.toString());
                            }
                            if (chunk != null) {
                                param = setChunkPosParams(statement, param, chunk);
                            }
                            statement.setString(param++, entry.getKey());
                            statement.setBoolean(param++, entry.getValue());
                        }
                        statement.executeUpdate();
                        return null;
                    }
                });
    }

    public void clearPermissionFlags(
            @NotNull UUID owner,
            @Nullable UUID accessor,
            @Nullable ChunkPos chunk,
            @NotNull String... flagNames) {
        String clauses =
                Arrays.stream(flagNames)
                        .map(ignored -> "flag_name=?")
                        .collect(Collectors.joining(" OR ", "(", ")"));
        String where =
                "WHERE player_uuid=? AND other_player_uuid="
                        + (accessor == null ? "''" : "?")
                        + " AND chunk_id="
                        + (chunk == null ? "-1" : SELECT_CHUNK_ID_SQL)
                        + " AND "
                        + clauses;

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement("DELETE FROM permission_flags " + where)) {
                        int param = 1;
                        statement.setString(param++, owner.toString());
                        if (accessor != null) {
                            statement.setString(param++, accessor.toString());
                        }
                        if (chunk != null) {
                            param = setChunkPosParams(statement, param, chunk);
                        }
                        for (String flagName : flagNames) {
                            statement.setString(param++, flagName);
                        }
                        statement.executeUpdate();
                        return null;
                    }
                });
    }

    // -- Loading stuff -- //

    public List<FullPlayerData> getAllPlayers() {
        HashMap<UUID, FullPlayerData> playerData =
                Q2ObjList.fromClause(SqlDataPlayer.class, null).stream()
                        .map(FullPlayerData::new)
                        .collect(
                                HashMap::new,
                                (acc, ply) -> acc.put(ply.player, ply),
                                HashMap::putAll);

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    SELECT *
                                    FROM permission_flags
                                    WHERE chunk_id=-1
                                    """)) {
                        ResultSet resultSet = statement.executeQuery();
                        while (resultSet.next()) {
                            UUID playerUuid = UUID.fromString(resultSet.getString("player_uuid"));
                            String otherPlyUuid = resultSet.getString("other_player_uuid");
                            String flagName = resultSet.getString("flag_name");
                            boolean allowDeny = resultSet.getBoolean("allow_deny");

                            FullPlayerData thisPly = playerData.get(playerUuid);
                            if (thisPly == null) {
                                Utils.err(
                                        "Failed to load player %s for permission %s",
                                        playerUuid, flagName);
                                continue;
                            }

                            UUID otherPly = null;
                            try {
                                otherPly = UUID.fromString(otherPlyUuid);
                            } catch (Exception ignored) {
                            }

                            if (otherPly != null) {
                                thisPly.playerFlags
                                        .computeIfAbsent(otherPly, ignored -> new HashMap<>())
                                        .put(flagName, allowDeny);
                            } else {
                                thisPly.globalFlags.put(flagName, allowDeny);
                            }
                        }
                    }
                    return null;
                });

        return playerData.values().stream().toList();
    }

    public Collection<DataChunk> getAllChunks() {
        return SqlClosure.sqlExecute(
                connection -> {
                    HashMap<ChunkPos, DataChunk> chunks = new HashMap<>();

                    // Load chunks
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    SELECT * FROM chunk_data
                                    """)) {
                        ResultSet resultSet = statement.executeQuery();
                        while (resultSet.next()) {
                            String world = resultSet.getString("chunk_world");
                            int chunk_x = resultSet.getInt("chunk_x");
                            int chunk_z = resultSet.getInt("chunk_z");
                            String ownerUuid = resultSet.getString("owner_uuid");

                            ChunkPos pos = new ChunkPos(world, chunk_x, chunk_z);
                            chunks.put(pos, new DataChunk(pos, UUID.fromString(ownerUuid)));
                        }
                    }

                    // Load chunk-based permissions
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    SELECT * FROM permission_flags
                                    LEFT JOIN chunk_data
                                    ON permission_flags.chunk_id=chunk_data.chunk_id
                                    WHERE permission_flags.chunk_id!=-1
                                    """)) {
                        ResultSet resultSet = statement.executeQuery();
                        while (resultSet.next()) {
                            String playerUuid = resultSet.getString("player_uuid");
                            String ignoredChunkOwnerUuid = resultSet.getString("owner_uuid");
                            if (!Objects.equals(playerUuid, ignoredChunkOwnerUuid)) {
                                Utils.err(
                                        "Player %s has permissions for a chunk owned by %s",
                                        playerUuid, ignoredChunkOwnerUuid);
                                continue;
                            }

                            String chunkWorld = resultSet.getString("chunk_world");
                            int chunkX = resultSet.getInt("chunk_x");
                            int chunkZ = resultSet.getInt("chunk_z");
                            ChunkPos chunkPos = new ChunkPos(chunkWorld, chunkX, chunkZ);
                            // May be empty!
                            String otherPlyUuid = resultSet.getString("other_player_uuid");
                            String flagName = resultSet.getString("flag_name");
                            boolean allowDeny = resultSet.getBoolean("allow_deny");

                            DataChunk chunk = chunks.get(Objects.requireNonNull(chunkPos));
                            if (chunk == null) {
                                Utils.err(
                                        "Tried to load permissions for unclaimed chunk at %s",
                                        chunkPos);
                                continue;
                            }

                            UUID otherPlayer = null;
                            try {
                                otherPlayer = UUID.fromString(otherPlyUuid);
                            } catch (Exception ignored) {
                            }

                            if (otherPlayer != null) {
                                chunk.specificFlags()
                                        .computeIfAbsent(otherPlayer, ignored -> new HashMap<>())
                                        .put(flagName, allowDeny);
                            } else {
                                chunk.defaultFlags().put(flagName, allowDeny);
                            }
                        }
                    }

                    return chunks.values();
                });
    }

    // -- Queries -- //

    // Returns the index of the next parameter!
    private int setChunkPosParams(
            PreparedStatement statement, int worldParameterNum, ChunkPos chunkPos)
            throws SQLException {
        statement.setString(worldParameterNum, chunkPos.world());
        statement.setInt(worldParameterNum + 1, chunkPos.x());
        statement.setInt(worldParameterNum + 2, chunkPos.z());
        return worldParameterNum + 3;
    }

    @SuppressWarnings("SameParameterValue")
    private String chunkIdQuery(String sql) {
        return SELECT_CHUNK_ID_SQL_PATTERN.matcher(sql).replaceAll(SELECT_CHUNK_ID_SQL);
    }
}
