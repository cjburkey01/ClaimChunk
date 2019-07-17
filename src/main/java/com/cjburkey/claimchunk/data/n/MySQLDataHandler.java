package com.cjburkey.claimchunk.data.n;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

// TODO: TEST ALL THESE METHODS
public class MySQLDataHandler implements IClaimChunkDataHandler {

    private static final String CLAIMED_CHUNKS_TABLE_NAME = "claimed_chunks";
    private static final String CLAIMED_CHUNKS_WORLD = "world_name";
    private static final String CLAIMED_CHUNKS_X = "x_pos";
    private static final String CLAIMED_CHUNKS_Z = "z_pos";
    private static final String CLAIMED_CHUNKS_OWNER = "owner_uuid";

    private static final String PLAYERS_TABLE_NAME = "joined_players";
    private static final String PLAYERS_UUID = "uuid";
    private static final String PLAYERS_IGN = "last_in_game_name";
    private static final String PLAYERS_NAME = "chunk_name";
    private static final String PLAYERS_LAST_JOIN = "last_join_time_ms";
    private static final String PLAYERS_ALERT = "receive_alerts";

    private static final String ACCESS_TABLE_NAME = "access_granted";

    private Connection connection;

    @Override
    public void init() throws SQLException, ClassNotFoundException {
        // Initialize a connection to the specified MySQL database
        // (This call automatically pulls the values from the config)
        connection = SqlBacking.connect();
        if (connection == null) throw new IllegalStateException("Failed to initialize MySQL connection");

        // Initialize the tables if they don't yet exist
        String dbName = Config.getString("database", "database");
        if (SqlBacking.tableDoesntExist(connection, dbName, CLAIMED_CHUNKS_TABLE_NAME)) {
            createJoinedPlayersTable();
        }
        if (SqlBacking.tableDoesntExist(connection, dbName, PLAYERS_TABLE_NAME)) {
            createClaimedChunksTable();
        }
        if (SqlBacking.tableDoesntExist(connection, dbName, ACCESS_TABLE_NAME)) {
            createAccessTable();
        }
    }

    @Override
    public void exit() throws SQLException {
        connection.close();
    }

    @Override
    public void save() {
        // No saving necessary
    }

    @Override
    public void load() {
        // No loading necessary
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)",
                CLAIMED_CHUNKS_TABLE_NAME, CLAIMED_CHUNKS_WORLD, CLAIMED_CHUNKS_X, CLAIMED_CHUNKS_Z, CLAIMED_CHUNKS_OWNER);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            statement.setString(4, player.toString());
            statement.executeQuery().close();
        } catch (Exception e) {
            Utils.err("Failed to claim chunk");
            e.printStackTrace();
        }
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        String sql = String.format("DELETE FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                CLAIMED_CHUNKS_TABLE_NAME, CLAIMED_CHUNKS_WORLD, CLAIMED_CHUNKS_X, CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            statement.executeQuery().close();
        } catch (Exception e) {
            Utils.err("Failed to unclaim chunk");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        String sql = String.format("SELECT count(*) FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                CLAIMED_CHUNKS_TABLE_NAME, CLAIMED_CHUNKS_WORLD, CLAIMED_CHUNKS_X, CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            Utils.err("Failed to determine if chunk was claimed");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public UUID getChunkOwner(ChunkPos pos) {
        String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                CLAIMED_CHUNKS_OWNER, CLAIMED_CHUNKS_TABLE_NAME, CLAIMED_CHUNKS_WORLD, CLAIMED_CHUNKS_X, CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return UUID.fromString(result.getString(1));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve chunk owner");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        String sql = String.format("SELECT (`%s`, `%s`, `%s`, `%s`) FROM `%s`",
                CLAIMED_CHUNKS_WORLD, CLAIMED_CHUNKS_X, CLAIMED_CHUNKS_Z, CLAIMED_CHUNKS_OWNER, CLAIMED_CHUNKS_TABLE_NAME);
        List<DataChunk> chunks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                chunks.add(new DataChunk(
                        new ChunkPos(result.getString(1), result.getInt(2), result.getInt(3)),
                        UUID.fromString(result.getString(4))
                ));
            }
        } catch (Exception e) {
            Utils.err("Failed to get all claimed chunks");
            e.printStackTrace();
        }
        return chunks.toArray(new DataChunk[0]);
    }

    @Override
    public void addPlayer(UUID player,
                          String lastIgn,
                          Set<UUID> _permitted,
                          @Nullable String chunkName,
                          long lastOnlineTime,
                          boolean alerts) {
        String sql = String.format("INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?)",
                PLAYERS_TABLE_NAME, PLAYERS_UUID, PLAYERS_IGN, PLAYERS_NAME, PLAYERS_LAST_JOIN, PLAYERS_ALERT);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.toString());
            statement.setString(2, lastIgn);
            statement.setString(3, chunkName);
            statement.setLong(4, lastOnlineTime);
            statement.setBoolean(5, alerts);
            statement.executeQuery().close();
        } catch (Exception e) {
            Utils.err("Failed to add player");
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public String getPlayerUsername(UUID player) {
        String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`=?",
                PLAYERS_IGN, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player username");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Nullable
    public UUID getPlayerUUID(String username) {
        String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`=?",
                PLAYERS_UUID, PLAYERS_TABLE_NAME, PLAYERS_IGN);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return UUID.fromString(result.getString(1));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player username UUID");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {
        String sql = String.format("UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                PLAYERS_TABLE_NAME, PLAYERS_LAST_JOIN, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, time);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed update player last online time");
            e.printStackTrace();
        }
    }

    @Override
    public void setPlayerChunkName(UUID player, @Nullable String name) {
        String sql = String.format("UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                PLAYERS_TABLE_NAME, PLAYERS_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed update player chunk name");
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayerChunkName(UUID player) {
        String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`=?",
                PLAYERS_NAME, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player chunk name");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPlayerAccess(UUID owner, UUID accessor, boolean access) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID[] getPlayersWithAccess(UUID owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean playerHasAccess(UUID owner, UUID accessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean alerts) {
        String sql = String.format("UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                PLAYERS_TABLE_NAME, PLAYERS_ALERT, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, alerts);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed update player alert preference");
            e.printStackTrace();
        }
    }

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`=?",
                PLAYERS_ALERT, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getBoolean(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player alert preference");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        String sql = String.format("SELECT count(*) FROM `%s` WHERE `%s`=?",
                PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player alert preference");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        String sql = String.format("SELECT (`%s`, `%s`, `%s`) FROM `%s`",
                PLAYERS_UUID, PLAYERS_IGN, PLAYERS_LAST_JOIN, PLAYERS_TABLE_NAME);
        ArrayList<SimplePlayerData> players = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    players.add(new SimplePlayerData(UUID.fromString(result.getString(1)),
                            result.getString(2),
                            result.getLong(3)));
                }
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve all players");
            e.printStackTrace();
        }
        return players;
    }

    private void createClaimedChunksTable() {
        throw new UnsupportedOperationException();
    }

    private void createJoinedPlayersTable() {
        throw new UnsupportedOperationException();
    }

    private void createAccessTable() {
        throw new UnsupportedOperationException();
    }

}
