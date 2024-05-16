package com.cjburkey.claimchunk.data.sqlite;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class SqLiteWrapper {

    public final File dbFile;
    private Connection liveConnection;

    public SqLiteWrapper(@NotNull File dbFile) throws RuntimeException {
        this.dbFile = dbFile;

        try {
            // Make sure the SQLite driver exists and get it in the classpath
            // for the DriverManager to search.
            Class.forName("org.sqlite.JDBC");

            // Initialize the tables and perform any changes to them
            SqLiteTableMigrationManager.go(this::connectionOrException);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Cannot find SQLite JDBC class? Not sure how this can happen. Please submit an"
                            + " issue on GitHub",
                    e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize tables! This is fatal!", e);
        }
    }

    // -- DATABASE INTEGRATIONS! -- //

    public void addClaimedChunk(DataChunk chunk) {
        try (Connection connection = connectionOrException()) {
            // Use the nested select query to get the user's row ID as the
            // owner's id
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            INSERT OR IGNORE INTO chunk_data (
                                chunk_world,
                                chunk_x,
                                chunk_z,
                                owner_uuid
                            ) VALUES (?, ?, ?, ?)
                            """)) {
                statement.setString(1, chunk.chunk.world());
                statement.setInt(2, chunk.chunk.x());
                statement.setInt(3, chunk.chunk.z());
                statement.setString(4, chunk.player.toString());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add claimed chunk!", e);
        }
    }

    public void removeClaimedChunk(ChunkPos chunk) {
        try (Connection connection = connectionOrException()) {
            // Get chunk ID
            final int chunkId;
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            SELECT chunk_id FROM chunk_data
                            WHERE chunk_world=? AND chunk_x=? AND chunk_y=?
                            """)) {
                statement.setString(1, chunk.world());
                statement.setInt(2, chunk.x());
                statement.setInt(3, chunk.z());
                ResultSet results = statement.executeQuery();
                if (!results.next()) return;
                chunkId = results.getInt(1);
            }

            // Remove granted permissions
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            DELETE FROM chunk_permissions
                            WHERE chunk_id=?
                            """)) {
                statement.setInt(1, chunkId);
                statement.execute();
            }

            // Remove the chunk
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            DELETE FROM chunk_data
                            WHERE chunk_id=?
                            """)) {
                statement.setInt(1, chunkId);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove claimed chunk!", e);
        }
    }

    // TODO: TEST
    public void addPlayer(FullPlayerData playerData) {
        try (Connection connection = connectionOrException()) {
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
                            ) VALUES (?, ?, ?, ?, ?, ?)
                            """)) {
                statement.setString(1, playerData.player.toString());
                statement.setString(2, playerData.lastIgn);
                statement.setString(3, playerData.chunkName);
                statement.setLong(4, playerData.lastOnlineTime);
                statement.setBoolean(5, playerData.alert);
                statement.setInt(6, playerData.extraMaxClaims);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add player to data handler!", e);
        }
    }

    public void setPlayerLastOnline(UUID player, long time) {
        try (Connection connection = connectionOrException()) {
            // Use the nested select query to get the user's row ID as the
            // owner's id
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set player last online time!", e);
        }
    }

    public void setPlayerChunkName(UUID player, String chunkName) {
        try (Connection connection = connectionOrException()) {
            // Use the nested select query to get the user's row ID as the
            // owner's id
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set player chunk name!", e);
        }
    }

    public void setPlayerReceiveAlerts(UUID player, boolean receiveAlerts) {
        try (Connection connection = connectionOrException()) {
            // Use the nested select query to get the user's row ID as the
            // owner's id
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to enable/disable player alerts!", e);
        }
    }

    public void setPlayerExtraMaxClaims(UUID player, int extraMaxClaims) {
        try (Connection connection = connectionOrException()) {
            // Use the nested select query to get the user's row ID as the
            // owner's id
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set player extra max claims!", e);
        }
    }

    public void addPlayerAccess(ChunkPos chunk, UUID accessor, int permissionFlags) {
        try (Connection connection = connectionOrException()) {
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            INSERT OR IGNORE INTO chunk_permissions (
                                chunk_id,
                                other_player_uuid,
                                permission_bits
                            ) VALUES (
                                (
                                    SELECT chunk_id
                                    FROM chunk_data
                                    WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
                                ),
                                ?, ?
                            )
                            """)) {
                statement.setString(1, chunk.world());
                statement.setInt(2, chunk.x());
                statement.setInt(3, chunk.z());
                statement.setString(4, accessor.toString());
                statement.setInt(5, permissionFlags);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add player access!", e);
        }
    }

    public void updatePlayerAccess(ChunkPos chunk, UUID accessor, int permissionFlags) {
        try (Connection connection = connectionOrException()) {
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            UPDATE chunk_permissions
                            SET permission_bits=?
                            WHERE
                                chunk_id=(
                                    SELECT chunk_id
                                    FROM chunk_data
                                    WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
                                )
                            AND other_player_uuid=?
                            """)) {
                statement.setInt(1, permissionFlags);
                statement.setString(2, chunk.world());
                statement.setInt(3, chunk.x());
                statement.setInt(4, chunk.z());
                statement.setString(5, accessor.toString());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update player access!", e);
        }
    }

    public void removePlayerAccess(ChunkPos chunk, UUID accessor) {
        try (Connection connection = connectionOrException()) {
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            DELETE FROM chunk_permissions
                            WHERE
                                chunk_id=(
                                    SELECT chunk_id
                                    FROM chunk_data
                                    WHERE chunk_world=? AND chunk_x=? AND chunk_z=?
                                )
                            AND other_player_uuid=?
                            """)) {
                statement.setString(1, chunk.world());
                statement.setInt(2, chunk.x());
                statement.setInt(3, chunk.z());
                statement.setString(4, accessor.toString());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove player access!", e);
        }
    }

    // -- Loading stuff -- //

    public Collection<FullPlayerData> getAllPlayers() {
        ArrayList<FullPlayerData> players = new ArrayList<>();

        try (Connection connection = connectionOrException()) {
            try (PreparedStatement statement =
                    connection.prepareStatement("SELECT * FROM player_data")) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    UUID player = UUID.fromString(resultSet.getString("player_uuid"));
                    String lastIgn = resultSet.getString("last_ign");
                    String chunkName = resultSet.getString("chunk_name");
                    long lastOnlineTime = resultSet.getLong("last_online_time");
                    boolean alert = resultSet.getBoolean("alerts_enabled");
                    int extraMaxClaims = resultSet.getInt("extra_max_claims");

                    players.add(
                            new FullPlayerData(
                                    player,
                                    lastIgn,
                                    chunkName,
                                    lastOnlineTime,
                                    alert,
                                    extraMaxClaims));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all players from SQLite database!", e);
        }

        return players;
    }

    public Collection<DataChunk> getAllChunks() {
        HashMap<ChunkPos, HashMap<UUID, ChunkPlayerPermissions>> permissions = new HashMap<>();
        HashMap<ChunkPos, UUID> owners = new HashMap<>();

        try (Connection connection = connectionOrException()) {
            // Get the permissions first
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
                    UUID owner = UUID.fromString(resultSet.getString("owner_uuid"));
                    UUID otherPlayer = UUID.fromString(resultSet.getString("other_player_uuid"));
                    ChunkPlayerPermissions chunkPerms =
                            new ChunkPlayerPermissions(resultSet.getInt("permission_bits"));

                    permissions
                            .computeIfAbsent(pos, ignoredPos -> new HashMap<>())
                            .put(otherPlayer, chunkPerms);

                    owners.putIfAbsent(pos, owner);
                }
            }

            // Then the chunks, for chunks with no permissions granted
            try (PreparedStatement statement =
                    connection.prepareStatement(
                            """
                            SELECT chunk_world, chunk_x, chunk_z, owner_uuid
                            FROM chunk_data
                            """)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String world = resultSet.getString("chunk_world");
                    int chunk_x = resultSet.getInt("chunk_x");
                    int chunk_z = resultSet.getInt("chunk_z");
                    ChunkPos pos = new ChunkPos(world, chunk_x, chunk_z);
                    UUID owner = UUID.fromString(resultSet.getString("owner_uuid"));

                    owners.putIfAbsent(pos, owner);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all players from SQLite database!", e);
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

    // -- Connection stuff -- //

    public @NotNull Connection connection() throws SQLException, IOException {
        if (liveConnection == null || liveConnection.isClosed()) {
            if (!dbFile.exists() && dbFile.createNewFile()) {
                Utils.warn("Created empty database file");
            }
            liveConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        }
        return liveConnection;
    }

    public @NotNull Connection connectionOrException() throws RuntimeException {
        try {
            return connection();
        } catch (SQLException e) {
            throw new RuntimeException("SQLException on connection creation", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new file " + dbFile, e);
        }
    }
}
