package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.Utils;
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
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public record SqLiteWrapper(File dbFile) implements Closeable {

    private static final String SELECT_CHUNK_ID_SQL =
            """
            (
                SELECT chunk_id
                FROM chunk_data
                WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
            )
            """;

    public SqLiteWrapper(@NotNull File dbFile) {
        this.dbFile = dbFile;

        try {
            if (!dbFile.exists() && dbFile.createNewFile()) {
                Utils.warn("Created empty database file");
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to create new database file even though it didn't exist!", e);
        }

        // Make sure the SQLite driver exists and get it in the classpath
        // for the DriverManager to search.
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbFile);
        //q2o.initializeTxSimple(dataSource);
        q2o.initializeTxNone(dataSource);

        // Initialize the tables and perform any changes to them
        SqLiteTableMigrationManager.go();

        // TODO: WHY DOESN'T PARAMETER SUBSTITUTION WORK?!?!?!
        //       HELP ME PLEASE
        Q2Sql.executeUpdate(
                """
                INSERT INTO player_data (
                    player_uuid,
                    last_ign,
                    last_online_time,
                    alerts_enabled,
                    extra_max_claims
                ) VALUES (
                    ?,
                    "CJBurkey",
                    72468,
                    TRUE,
                    0
                )
                """,
                "8da30070-a1df-4e47-a913-f12424aabf6a");
    }

    @Override
    public void close() {
        q2o.deinitialize();
    }

    // -- DATABASE INTEGRATIONS! -- //

    public void addClaimedChunk(DataChunk chunk) {
        Q2Obj.insert(new SqlDataChunk(chunk));
    }

    public void removeClaimedChunk(ChunkPos chunk) {
        int chunkId =
                SqlClosure.sqlExecute(
                        connection -> {
                            // Get chunk ID
                            ResultSet resultSet =
                                    Q2Sql.executeQuery(
                                            connection,
                                            """
                                            SELECT chunk_id FROM chunk_data
                                            WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
                                            """,
                                            chunk.world(),
                                            chunk.x(),
                                            chunk.z());
                            return resultSet.next() ? resultSet.getInt(1) : -1;
                        });
        if (chunkId < 0) return;

        // Remove permissions
        Q2Sql.executeUpdate(
                """
                DELETE FROM chunk_permissions
                WHERE chunk_id=?
                """,
                chunkId);

        // Remove chunks
        Q2Sql.executeUpdate(
                """
                DELETE FROM chunk_data
                WHERE chunk_id=?
                """,
                chunkId);
    }

    public void addPlayer(FullPlayerData playerData) {
        Q2Obj.insert(new SqlDataPlayer(playerData));
    }

    public void setPlayerLastOnline(UUID player, long time) {
        Q2Sql.executeUpdate(
                """
                UPDATE player_data
                SET last_online_time=?
                WHERE player_uuid=?
                """,
                time,
                player.toString());
    }

    public void setPlayerChunkName(UUID player, String chunkName) {
        Q2Sql.executeUpdate(
                """
                UPDATE player_data
                SET chunk_name=?
                WHERE player_uuid=?
                """,
                chunkName,
                player.toString());
    }

    public void setPlayerReceiveAlerts(UUID player, boolean receiveAlerts) {
        Q2Sql.executeUpdate(
                """
                UPDATE player_data
                SET receiveAlerts=?
                WHERE player_uuid=?
                """,
                receiveAlerts,
                player.toString());
    }

    public void setPlayerExtraMaxClaims(UUID player, int extraMaxClaims) {
        Q2Sql.executeUpdate(
                """
                UPDATE player_data
                SET extra_max_claims=?
                WHERE player_uuid=?
                """,
                extraMaxClaims,
                player.toString());
    }

    public void updateOrInsertPlayerAccess(ChunkPos chunk, UUID accessor, int permissionFlags) {
        // Check if the access already exists
        // If so, update the permission bits
        if (Q2Obj.countFromClause(
                        SqlDataChunkPermission.class,
                        "other_player_uuid=? AND chunk_id=" + SELECT_CHUNK_ID_SQL,
                        accessor.toString(),
                        chunk.world(),
                        chunk.x(),
                        chunk.z())
                > 0) {
            Q2Sql.executeUpdate(
                    String.format(
                            """
                            UPDATE chunk_permissions
                            SET permission_bits=?
                            WHERE chunk_id=%s
                            AND other_player_uuid=?
                            """,
                            SELECT_CHUNK_ID_SQL),
                    permissionFlags,
                    chunk.world(),
                    chunk.x(),
                    chunk.z(),
                    accessor.toString());
        } else {
            Q2Sql.executeUpdate(
                    String.format(
                            """
                            INSERT INTO chunk_permissions (
                                chunk_id,
                                other_player_uuid,
                                permission_bits
                            ) VALUES (
                                %s, ?, ?
                            )
                            """,
                            SELECT_CHUNK_ID_SQL),
                    chunk.world(),
                    chunk.x(),
                    chunk.z(),
                    accessor.toString(),
                    permissionFlags);
        }
    }

    public void removePlayerAccess(ChunkPos chunk, UUID accessor) {
        Q2Sql.executeUpdate(
                String.format(
                        """
                        DELETE FROM chunk_permissions
                        WHERE chunk_id=%s,
                        AND other_player_uuid=?
                        """,
                        SELECT_CHUNK_ID_SQL),
                chunk.world(),
                chunk.x(),
                chunk.z(),
                accessor.toString());
    }

    // -- Loading stuff -- //

    public List<FullPlayerData> getAllPlayers() {
        return Q2ObjList.fromClause(SqlDataPlayer.class, null).stream()
                .map(FullPlayerData::new)
                .toList();
    }

    public Collection<DataChunk> getAllChunks() {
        HashMap<ChunkPos, HashMap<UUID, ChunkPlayerPermissions>> permissions = new HashMap<>();
        HashMap<ChunkPos, UUID> owners = new HashMap<>();

        try (ResultSet resultSet =
                SqlClosure.sqlExecute(
                        connection ->
                                Q2Sql.executeQuery(
                                        connection,
                                        """
                                        SELECT chunk_world, chunk_x, chunk_z, owner_uuid,
                                            other_player_uuid, permission_bits
                                        FROM chunk_permissions
                                        RIGHT JOIN chunk_data
                                        ON chunk_permissions.chunk_id=chunk_data.chunk_id
                                        """))) {
            while (resultSet.next()) {
                String world = resultSet.getString("chunk_world");
                int chunk_x = resultSet.getInt("chunk_x");
                int chunk_z = resultSet.getInt("chunk_z");
                ChunkPos pos = new ChunkPos(world, chunk_x, chunk_z);
                UUID owner = UUID.fromString(resultSet.getString("owner_uuid"));
                UUID otherPlayer = UUID.fromString(resultSet.getString("other_player_uuid"));
                ChunkPlayerPermissions chunkPerms =
                        new ChunkPlayerPermissions(resultSet.getInt("permission_bits"));

                permissions
                        .computeIfAbsent(pos, ignoredPos -> new HashMap<>())
                        .put(otherPlayer, chunkPerms);

                owners.putIfAbsent(pos, owner);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load chunk data!", e);
        }

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
}
