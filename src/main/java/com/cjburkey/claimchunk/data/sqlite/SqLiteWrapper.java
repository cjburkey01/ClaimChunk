package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.zaxxer.q2o.*;

import org.jetbrains.annotations.NotNull;
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
                        statement.setString(1, chunk.player.toString());
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
                        int next = setChunkPosParams(statement, 1, chunk.chunk);
                        statement.setString(next, chunk.player.toString());
                        statement.execute();
                    }

                    // Add the player permissions
                    if (!chunk.playerPermissions.isEmpty()) {
                        String permsInsertPrefixSql =
                                """
                                INSERT INTO chunk_permissions (
                                    chunk_id,
                                    other_player_uuid,
                                    permission_bits
                                ) VALUES
                                """;

                        // Better way to do this?
                        ArrayList<String> params = new ArrayList<>();
                        for (int i = 0; i < chunk.playerPermissions.size(); i++) {
                            params.add("(%%SELECT_CHUNK_ID_SQL%%, ?, ?)");
                        }
                        String finalSql =
                                chunkIdQuery(permsInsertPrefixSql + String.join(",", params));

                        try (PreparedStatement statement = connection.prepareStatement(finalSql)) {
                            int currentParam = 1;
                            for (Map.Entry<UUID, ChunkPlayerPermissions> entry :
                                    chunk.playerPermissions.entrySet()) {
                                currentParam =
                                        setChunkPosParams(statement, currentParam, chunk.chunk);
                                statement.setString(currentParam++, entry.getKey().toString());
                                statement.setInt(currentParam++, entry.getValue().permissionFlags);
                            }
                            statement.execute();
                        }
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
                                            DELETE FROM chunk_permissions
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

    @Deprecated
    public void setPlayerAccess(ChunkPos chunk, UUID accessor, int permissionFlags) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    chunkIdQuery(
                                            """
                                            INSERT INTO chunk_permissions (
                                                chunk_id,
                                                other_player_uuid,
                                                permission_bits
                                            ) VALUES (
                                                %%SELECT_CHUNK_ID_SQL%%, ?, ?
                                            )
                                            ON CONFLICT(chunk_id, other_player_uuid) DO
                                            UPDATE SET permission_bits=excluded.permission_bits
                                            """))) {
                        int next = setChunkPosParams(statement, 1, chunk);
                        statement.setString(next, accessor.toString());
                        statement.setInt(next + 1, permissionFlags);
                        statement.execute();
                    }
                    return null;
                });
    }

    @Deprecated
    public void removePlayerAccess(ChunkPos chunk, UUID accessor) {
        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    chunkIdQuery(
                                            """
                                            DELETE FROM chunk_permissions
                                            WHERE chunk_id=%%SELECT_CHUNK_ID_SQL%%
                                            AND other_player_uuid=?
                                            """))) {
                        int next = setChunkPosParams(statement, 1, chunk);
                        statement.setString(next, accessor.toString());
                        statement.execute();
                        return null;
                    }
                });
    }

    public void grantPermissionFlagsGlobalDefault(UUID owner, String... flagNames) {
        SqlClosure.sqlExecute(
                connection -> {
                    String values =
                            Arrays.stream(flagNames)
                                    .map(ignored -> "(?, ?)")
                                    .collect(Collectors.joining(", "));

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT OR IGNORE INTO flags_player_default_enabled (
                                        player_uuid,
                                        flag_name
                                    ) VALUES
                                    """
                                            + values)) {
                        int param = 1;
                        for (String flagName : flagNames) {
                            statement.setString(param++, owner.toString());
                            statement.setString(param++, flagName);
                        }
                        statement.execute();
                        return null;
                    }
                });
    }

    public void revokePermissionFlagsGlobalDefault(UUID owner, String... flagNames) {
        String clauses =
                Arrays.stream(flagNames)
                        .map(ignored -> "flag_name=?")
                        .collect(Collectors.joining(" OR "));

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    DELETE FROM flags_player_default_enabled
                                    WHERE player_uuid=? AND (
                                    """
                                            + clauses
                                            + ")")) {
                        int param = 1;
                        statement.setString(param++, owner.toString());
                        for (String flagName : flagNames) {
                            statement.setString(param++, flagName);
                        }
                        statement.execute();
                        return null;
                    }
                });
    }

    public void grantPermissionFlagsChunkDefault(UUID owner, ChunkPos chunk, String... flagNames) {
        SqlClosure.sqlExecute(
                connection -> {
                    String values =
                            Arrays.stream(flagNames)
                                    .map(ignored -> chunkIdQuery("(?, %%SELECT_CHUNK_ID_SQL%%, ?)"))
                                    .collect(Collectors.joining(", "));

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT OR IGNORE INTO flags_player_chunk_enabled (
                                        player_uuid,
                                        chunk_id,
                                        flag_name
                                    ) VALUES
                                    """
                                            + values)) {
                        int param = 1;
                        for (String flagName : flagNames) {
                            statement.setString(param++, owner.toString());
                            param = setChunkPosParams(statement, param, chunk);
                            statement.setString(param++, flagName);
                        }

                        statement.execute();
                        return null;
                    }
                });
    }

    public void revokePermissionFlagsChunkDefault(UUID owner, ChunkPos chunk, String... flagNames) {
        String clauses =
                Arrays.stream(flagNames)
                        .map(ignored -> "flag_name=?")
                        .collect(Collectors.joining(" OR "));

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    chunkIdQuery(
                                            """
                                            DELETE FROM flags_player_chunk_enabled
                                            WHERE player_uuid=?
                                            AND chunk_id=%%SELECT_CHUNK_ID_SQL%%
                                            AND (
                                            """
                                                    + clauses
                                                    + ")"))) {
                        int param = 1;
                        statement.setString(param++, owner.toString());
                        param = setChunkPosParams(statement, param, chunk);
                        for (String flagName : flagNames) {
                            statement.setString(param++, flagName);
                        }
                        statement.execute();
                        return null;
                    }
                });
    }

    public void grantPermissionFlagsPlayerDefault(UUID owner, UUID accessor, String... flagNames) {
        SqlClosure.sqlExecute(
                connection -> {
                    String values =
                            Arrays.stream(flagNames)
                                    .map(ignored -> "(?, ?, ?)")
                                    .collect(Collectors.joining(", "));

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT OR IGNORE INTO flags_player_other_player_enabled (
                                        player_uuid,
                                        other_player_uuid,
                                        flag_name
                                    ) VALUES
                                    """
                                            + values)) {
                        int param = 1;
                        for (String flagName : flagNames) {
                            statement.setString(param++, owner.toString());
                            statement.setString(param++, accessor.toString());
                            statement.setString(param++, flagName);
                        }
                        statement.execute();
                        return null;
                    }
                });
    }

    public void revokePermissionFlagsPlayerDefault(UUID owner, UUID accessor, String... flagNames) {
        String clauses =
                Arrays.stream(flagNames)
                        .map(ignored -> "flag_name=?")
                        .collect(Collectors.joining(" OR "));

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    chunkIdQuery(
                                            """
                                            DELETE FROM flags_player_other_player_enabled
                                            WHERE player_uuid=?
                                            AND other_player_uuid=?
                                            AND (
                                            """
                                                    + clauses
                                                    + ")"))) {
                        int param = 1;
                        statement.setString(param++, owner.toString());
                        statement.setString(param++, accessor.toString());
                        for (String flagName : flagNames) {
                            statement.setString(param++, flagName);
                        }
                        statement.execute();
                        return null;
                    }
                });
    }

    public void grantPermissionFlagsPlayerChunk(
            UUID owner, UUID accessor, ChunkPos chunk, String... flagNames) {
        SqlClosure.sqlExecute(
                connection -> {
                    String values =
                            Arrays.stream(flagNames)
                                    .map(
                                            ignored ->
                                                    chunkIdQuery(
                                                            "(?, ?, %%SELECT_CHUNK_ID_SQL%%, ?)"))
                                    .collect(Collectors.joining(", "));

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    INSERT OR IGNORE INTO flags_player_chunk_player_enabled (
                                        player_uuid,
                                        other_player_uuid,
                                        chunk_id,
                                        flag_name
                                    ) VALUES
                                    """
                                            + values)) {
                        int param = 1;
                        for (String flagName : flagNames) {
                            statement.setString(param++, owner.toString());
                            statement.setString(param++, accessor.toString());
                            param = setChunkPosParams(statement, param, chunk);
                            statement.setString(param++, flagName);
                        }

                        statement.execute();
                        return null;
                    }
                });
    }

    public void revokePermissionFlagsPlayerChunk(
            UUID owner, UUID accessor, ChunkPos chunk, String... flagNames) {
        String clauses =
                Arrays.stream(flagNames)
                        .map(ignored -> "flag_name=?")
                        .collect(Collectors.joining(" OR "));

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    chunkIdQuery(
                                            """
                                            DELETE FROM flags_player_chunk_player_enabled
                                            WHERE player_uuid=?
                                            AND other_player_uuid=?
                                            AND chunk_id=%%SELECT_CHUNK_ID_SQL%%
                                            AND (
                                            """
                                                    + clauses
                                                    + ")"))) {
                        int param = 1;
                        statement.setString(param++, owner.toString());
                        statement.setString(param++, accessor.toString());
                        param = setChunkPosParams(statement, param, chunk);
                        for (String flagName : flagNames) {
                            statement.setString(param++, flagName);
                        }
                        statement.execute();
                        return null;
                    }
                });
    }

    // -- Loading stuff -- //

    public List<FullPlayerData> getAllPlayers() {
        return Q2ObjList.fromClause(SqlDataPlayer.class, null).stream()
                .map(FullPlayerData::new)
                .toList();
    }

    /**
     * @deprecated TODO: Use new method
     */
    @Deprecated
    public static Collection<DataChunk> getAllChunksLegacy() {
        HashMap<ChunkPos, HashMap<UUID, ChunkPlayerPermissions>> permissions = new HashMap<>();
        HashMap<ChunkPos, UUID> owners = new HashMap<>();

        SqlClosure.sqlExecute(
                connection -> {
                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    """
                                    SELECT chunk_world, chunk_x, chunk_z, owner_uuid,
                                        other_player_uuid, permission_bits
                                    FROM chunk_permissions
                                    RIGHT JOIN chunk_data
                                    ON chunk_permissions.chunk_id=chunk_data.chunk_id
                                    """)) {
                        ResultSet resultSet = statement.executeQuery();
                        while (resultSet.next()) {
                            String world = resultSet.getString("chunk_world");
                            int chunk_x = resultSet.getInt("chunk_x");
                            int chunk_z = resultSet.getInt("chunk_z");
                            ChunkPos pos = new ChunkPos(world, chunk_x, chunk_z);

                            String otherUuid = resultSet.getString("other_player_uuid");
                            if (otherUuid != null) {
                                UUID otherPlayer = UUID.fromString(otherUuid);
                                ChunkPlayerPermissions chunkPerms =
                                        new ChunkPlayerPermissions(
                                                resultSet.getInt("permission_bits"));

                                permissions
                                        .computeIfAbsent(pos, ignoredPos -> new HashMap<>())
                                        .put(otherPlayer, chunkPerms);
                            }

                            UUID owner = UUID.fromString(resultSet.getString("owner_uuid"));
                            owners.putIfAbsent(pos, owner);
                        }
                    }

                    return null;
                });

        for (SqlDataChunk chunk : Q2ObjList.fromClause(SqlDataChunk.class, null)) {
            owners.putIfAbsent(
                    new ChunkPos(chunk.world, chunk.x, chunk.z), UUID.fromString(chunk.uuid));
        }

        return owners.entrySet().stream()
                .map(
                        entry ->
                                new DataChunk(
                                        entry.getKey(),
                                        entry.getValue(),
                                        permissions.getOrDefault(entry.getKey(), new HashMap<>()),
                                        false))
                .collect(Collectors.toList());
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

    private String chunkIdQuery(String sql) {
        return SELECT_CHUNK_ID_SQL_PATTERN.matcher(sql).replaceAll(SELECT_CHUNK_ID_SQL);
    }
}
