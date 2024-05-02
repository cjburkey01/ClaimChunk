package com.cjburkey.claimchunk.data.newdata;

import static com.cjburkey.claimchunk.data.newdata.SqlBacking.*;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.conversion.IDataConverter;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.cjburkey.claimchunk.player.SimplePlayerData;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Uses per-request database access system to save/load data so many requests are made when they are
 * needed. This is only a good system if connecting to the database is very fast. Otherwise, the
 * bulk system is much much faster and won't result in constant server lag.
 *
 * @param <T> The type of the backup data system.
 * @since 0.0.13
 */
public class MySQLDataHandler<T extends IClaimChunkDataHandler> implements IClaimChunkDataHandler {

    static final String CLAIMED_CHUNKS_TABLE_NAME = "claimed_chunks";
    static final String PLAYERS_TABLE_NAME = "joined_players";
    static final String ACCESS_TABLE_NAME = "access_granted";
    private static final String CLAIMED_CHUNKS_ID = "id";
    private static final String CLAIMED_CHUNKS_WORLD = "world_name";
    private static final String CLAIMED_CHUNKS_X = "chunk_x_pos";
    private static final String CLAIMED_CHUNKS_Z = "chunk_z_pos";
    private static final String CLAIMED_CHUNKS_TNT = "tnt_enabled";
    private static final String CLAIMED_CHUNKS_OWNER = "owner_uuid";
    private static final String PLAYERS_UUID = "uuid";
    private static final String PLAYERS_IGN = "last_in_game_name";
    private static final String PLAYERS_NAME = "chunk_name";
    private static final String PLAYERS_LAST_JOIN = "last_join_time_ms";
    private static final String PLAYERS_ALERT = "receive_alerts";
    private static final String PLAYERS_MAX_CLAIM = "max_claims";

    private static final String ACCESS_ACCESS_ID = "access_id";
    private static final String ACCESS_CHUNK_ID = "chunk_id";
    private static final String ACCESS_OWNER = "owner_uuid";
    private static final String ACCESS_OTHER = "other_uuid";
    private static final String ACCESS_PERMISSIONS_FLAGS = "permissions_flags";

    private final ClaimChunk claimChunk;
    Supplier<Connection> connection;
    private String dbName;
    private T oldDataHandler;
    private Consumer<T> onCleanOld;
    private boolean init;

    public MySQLDataHandler(
            ClaimChunk claimChunk, Supplier<T> oldDataHandler, Consumer<T> onCleanOld) {
        this.claimChunk = claimChunk;
        if (oldDataHandler != null) {
            this.oldDataHandler = oldDataHandler.get();
            this.onCleanOld = onCleanOld;
        }
    }

    @Override
    public void init() throws Exception {
        init = true;

        Utils.warn("MySQL support is going to be removed from ClaimChunk!");
        Utils.warn("Your data will automatically convert when this happens, but be aware.");
        Utils.warn(
                "There will be a message notifying you when the change occurs, no changes will be"
                        + " made to the database.");

        // Initialize a connection to the specified MySQL database
        dbName = claimChunk.getConfigHandler().getDatabaseName();
        connection =
                connect(
                        claimChunk.getConfigHandler().getDatabaseHostname(),
                        claimChunk.getConfigHandler().getDatabasePort(),
                        dbName,
                        claimChunk.getConfigHandler().getDatabaseUsername(),
                        claimChunk.getConfigHandler().getDatabasePassword(),
                        claimChunk.getConfigHandler().getUseSsl(),
                        claimChunk.getConfigHandler().getAllowPublicKeyRetrieval());

        // Initialize the tables if they don't yet exist
        if (getTableDoesntExist(claimChunk, connection, dbName, CLAIMED_CHUNKS_TABLE_NAME)) {
            Utils.debug("Creating claimed chunks table");
            createClaimedChunksTable();
        } else {
            migrateClaimedChunksTable0015_0016();
            Utils.debug("Found claimed chunks table");
        }
        if (getTableDoesntExist(claimChunk, connection, dbName, PLAYERS_TABLE_NAME)) {
            Utils.debug("Creating joined players table");
            createJoinedPlayersTable();
        } else {
            migratePlayerTableMaxClaim0023_0024();
            Utils.debug("Found joined players table");
        }
        if (getTableDoesntExist(claimChunk, connection, dbName, ACCESS_TABLE_NAME)) {
            Utils.debug("Creating access table");
            createAccessTable();
        } else {
            migrateAccessTable0015_0016();
            migrateAccessTable0023_0024();
            Utils.debug("Found access table");
        }

        if (oldDataHandler != null && claimChunk.getConfigHandler().getConvertOldData()) {
            IDataConverter.copyConvert(oldDataHandler, this);
            oldDataHandler.exit();
            if (onCleanOld != null) {
                onCleanOld.accept(oldDataHandler);
            }
        }
    }

    @Override
    public boolean getHasInit() {
        return init;
    }

    @Override
    public void exit() throws SQLException {
        // No closing necessary
    }

    @Override
    public void save() throws Exception {
        // No saving necessary
    }

    @Override
    public void load() {
        // No loading necessary
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        String sql =
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_OWNER);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            statement.setString(4, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to claim chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        if (chunks.length == 0) return;

        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES",
                                CLAIMED_CHUNKS_TABLE_NAME,
                                CLAIMED_CHUNKS_WORLD,
                                CLAIMED_CHUNKS_X,
                                CLAIMED_CHUNKS_Z,
                                CLAIMED_CHUNKS_OWNER));
        for (int i = 0; i < chunks.length; i++) {
            sql.append(" (?, ?, ?, ?)");
            if (i != chunks.length - 1) sql.append(',');
        }
        try (PreparedStatement statement = prep(claimChunk, connection, sql.toString())) {
            int i = 0;
            for (DataChunk chunk : chunks) {
                statement.setString(4 * i + 1, chunk.chunk.getWorld());
                statement.setInt(4 * i + 2, chunk.chunk.getX());
                statement.setInt(4 * i + 3, chunk.chunk.getZ());
                statement.setString(4 * i + 4, chunk.player.toString());
                i++;
            }
            statement.execute();
            writeAccessAssociationsBulk(chunks);
        } catch (Exception e) {
            Utils.err("Failed add claimed chunks: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        String sql =
                String.format(
                        "DELETE FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to unclaim chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        String sql =
                String.format(
                        "SELECT count(*) FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            Utils.err("Failed to determine if chunk was claimed: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    @Nullable
    public UUID getChunkOwner(ChunkPos pos) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=? LIMIT 1",
                        CLAIMED_CHUNKS_OWNER,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return UUID.fromString(result.getString(1));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve chunk owner: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s`",
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_OWNER,
                        CLAIMED_CHUNKS_TABLE_NAME);
        List<DataChunk> chunks = new ArrayList<>();
        Map<Integer, Map<UUID, ChunkPlayerPermissions>> allChunkPermissions =
                getPlayerPermissionsForAllChunks();

        try (PreparedStatement statement = prep(claimChunk, connection, sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                chunks.add(
                        new DataChunk(
                                new ChunkPos(
                                        result.getString(2), result.getInt(3), result.getInt(4)),
                                UUID.fromString(result.getString(6)),
                                allChunkPermissions.getOrDefault(result.getInt(1), new HashMap<>()),
                                result.getBoolean(5)));
            }
        } catch (Exception e) {
            Utils.err("Failed to get all claimed chunks: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return chunks.toArray(new DataChunk[0]);
    }

    @Override
    public boolean toggleTnt(ChunkPos pos) {
        boolean current = isTntEnabled(pos);
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE (`%s`=?) AND (`%s`=?) AND (`%s`=?)",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setBoolean(1, !current);
            statement.setString(2, pos.getWorld());
            statement.setInt(3, pos.getX());
            statement.setInt(4, pos.getZ());
            statement.execute();
            return !current;
        } catch (Exception e) {
            Utils.err("Failed to update tnt enabled in chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return current;
    }

    @Override
    public boolean isTntEnabled(ChunkPos pos) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE (`%s`=?) AND (`%s`=?) AND (`%s`=?)",
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, pos.getWorld());
            statement.setInt(2, pos.getX());
            statement.setInt(3, pos.getZ());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getBoolean(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve tnt enabled in chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void addPlayer(
            UUID player,
            String lastIgn,
            @Nullable String chunkName,
            long lastOnlineTime,
            boolean alerts,
            int maxClaims) {
        String sql =
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?,"
                            + " ?)",
                        PLAYERS_TABLE_NAME,
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN,
                        PLAYERS_ALERT,
                        PLAYERS_MAX_CLAIM);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            statement.setString(2, lastIgn);
            statement.setString(3, chunkName);
            statement.setLong(4, lastOnlineTime);
            statement.setBoolean(5, alerts);
            statement.setInt(6, maxClaims);
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to add player: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        if (players.length == 0) return;

        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`, `%s`) VALUES",
                                PLAYERS_TABLE_NAME,
                                PLAYERS_UUID,
                                PLAYERS_IGN,
                                PLAYERS_NAME,
                                PLAYERS_LAST_JOIN,
                                PLAYERS_ALERT,
                                PLAYERS_MAX_CLAIM));
        for (int i = 0; i < players.length; i++) {
            sql.append(" (?, ?, ?, ?, ?, ?)");
            if (i != players.length - 1) sql.append(',');
        }
        try (PreparedStatement statement = prep(claimChunk, connection, sql.toString())) {
            int i = 0;
            for (FullPlayerData player : players) {
                // OFFSET BY ONE!
                statement.setString(6 * i + 1, player.player.toString());
                statement.setString(6 * i + 2, player.lastIgn);
                statement.setString(6 * i + 3, player.chunkName);
                statement.setLong(6 * i + 4, player.lastOnlineTime);
                statement.setBoolean(6 * i + 5, player.alert);
                statement.setBoolean(6 * i + 6, player.alert);
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to add joined players: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public String getPlayerUsername(UUID player) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_IGN, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player username: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Nullable
    public UUID getPlayerUUID(String username) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_UUID, PLAYERS_TABLE_NAME, PLAYERS_IGN);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return UUID.fromString(result.getString(1));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player username UUID: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_LAST_JOIN, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setLong(1, time);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed update player last online time: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void setPlayerChunkName(UUID player, @Nullable String name) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, name);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed update player chunk name: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public String getPlayerChunkName(UUID player) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_NAME, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player chunk name: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean alerts) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_ALERT, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setBoolean(1, alerts);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to update player alert preference: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_ALERT, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getBoolean(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player alert preference: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setPlayerExtraMaxClaims(UUID player, int extraMaxClaims) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_MAX_CLAIM, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setInt(1, extraMaxClaims);
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to update player max claims: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void addPlayerExtraMaxClaims(UUID player, int numToAdd) {
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=`%2$s`+? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_MAX_CLAIM, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setInt(1, Math.abs(numToAdd));
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to update player max claims: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void takePlayerExtraMaxClaims(UUID player, int numToTake) {
        // Ugly but idk how to do this in sql :(
        int finalNumToTake = Math.max(getPlayerExtraMaxClaims(player), numToTake);
        String sql =
                String.format(
                        "UPDATE `%s` SET `%s`=`%2$s`-? WHERE `%s`=?",
                        PLAYERS_TABLE_NAME, PLAYERS_MAX_CLAIM, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setInt(1, Math.abs(finalNumToTake));
            statement.setString(2, player.toString());
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to update player max claims: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public int getPlayerExtraMaxClaims(UUID player) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        PLAYERS_MAX_CLAIM, PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1);
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player max claims: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return -6969;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        String sql =
                String.format(
                        "SELECT count(*) FROM `%s` WHERE `%s`=?", PLAYERS_TABLE_NAME, PLAYERS_UUID);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve player alert preference: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s` FROM `%s` LIMIT 1",
                        PLAYERS_UUID, PLAYERS_IGN, PLAYERS_LAST_JOIN, PLAYERS_TABLE_NAME);
        ArrayList<SimplePlayerData> players = new ArrayList<>();
        try (PreparedStatement statement = prep(claimChunk, connection, sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                players.add(
                        new SimplePlayerData(
                                UUID.fromString(result.getString(1)),
                                result.getString(2),
                                result.getLong(3)));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve all players: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` LIMIT 1",
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN,
                        PLAYERS_ALERT,
                        PLAYERS_TABLE_NAME,
                        PLAYERS_MAX_CLAIM);
        ArrayList<FullPlayerData> players = new ArrayList<>();
        try (PreparedStatement statement = prep(claimChunk, connection, sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                players.add(
                        new FullPlayerData(
                                uuid,
                                result.getString(2),
                                result.getString(3),
                                result.getLong(4),
                                result.getBoolean(5),
                                result.getInt(6)));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve all players data: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return players.toArray(new FullPlayerData[0]);
    }

    @Override
    public void givePlayerAccess(
            ChunkPos chunk, UUID accessor, ChunkPlayerPermissions permissions) {
        // Get chunk Id
        String getChunkIdSql =
                String.format(
                        "SELECT `%s`, `%s` FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_OWNER,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        try (PreparedStatement chunkIdStatement = prep(claimChunk, connection, getChunkIdSql)) {
            chunkIdStatement.setString(1, chunk.getWorld());
            chunkIdStatement.setInt(2, chunk.getX());
            chunkIdStatement.setInt(3, chunk.getZ());

            try (ResultSet result = chunkIdStatement.executeQuery()) {
                if (result.next()) {
                    int chunkId = result.getInt(1);
                    String chunkOwner = result.getString(2);

                    // Check if accessor already has access
                    String checkExistingAccessSql =
                            String.format(
                                    "SELECT `%s` FROM `%s` WHERE `%s`=? AND `%s`=?",
                                    ACCESS_ACCESS_ID,
                                    ACCESS_TABLE_NAME,
                                    ACCESS_CHUNK_ID,
                                    ACCESS_OTHER);
                    try (PreparedStatement checkExistingAccess =
                            prep(claimChunk, connection, checkExistingAccessSql)) {
                        checkExistingAccess.setInt(1, chunkId);
                        checkExistingAccess.setString(2, accessor.toString());
                        try (ResultSet existingAccessResult = checkExistingAccess.executeQuery()) {
                            if (existingAccessResult.next()) {
                                // There are already permissions for the given player on the given
                                // chunk, so just update them
                                String updateStatementSql =
                                        String.format(
                                                "UPDATE `%s` SET `%s`=? WHERE `%s`=?",
                                                ACCESS_TABLE_NAME,
                                                ACCESS_PERMISSIONS_FLAGS,
                                                ACCESS_ACCESS_ID);
                                try (PreparedStatement updateStatement =
                                        prep(claimChunk, connection, updateStatementSql)) {
                                    updateStatement.setInt(1, permissions.getPermissionFlags());
                                    updateStatement.setInt(2, result.getInt(1));

                                    updateStatement.execute();
                                }
                            } else {
                                // There are no existing permissions for the player on the given
                                // chunk, so insert a new row
                                String insertStatementSql =
                                        String.format(
                                                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES"
                                                        + " (?, ?, ?, ?)",
                                                ACCESS_TABLE_NAME,
                                                ACCESS_CHUNK_ID,
                                                ACCESS_OWNER,
                                                ACCESS_OTHER,
                                                ACCESS_PERMISSIONS_FLAGS);
                                try (PreparedStatement insertStatement =
                                        prep(claimChunk, connection, insertStatementSql)) {
                                    insertStatement.setInt(1, chunkId);
                                    insertStatement.setString(2, chunkOwner);
                                    insertStatement.setString(3, accessor.toString());
                                    insertStatement.setInt(4, permissions.getPermissionFlags());

                                    insertStatement.execute();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.err("Failed to give player access to chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void writeAccessAssociationsBulk(DataChunk[] chunks) {
        // Write many rows to Access table in one statement.  Requires the table to be empty
        // beforehand (as it does not check to see if the accesses already exist before inserting
        // them)
        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES",
                                ACCESS_TABLE_NAME,
                                ACCESS_CHUNK_ID,
                                ACCESS_OWNER,
                                ACCESS_OTHER,
                                ACCESS_PERMISSIONS_FLAGS));
        String accessValuesString =
                String.format(
                        " ((SELECT `%s` FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?), ?, ?, ?),",
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);
        boolean chunksHavePermissions = false;
        for (DataChunk chunk : chunks) {
            for (int i = 0; i < chunk.playerPermissions.size(); i++) {
                sql.append(accessValuesString);
                chunksHavePermissions = true;
            }
        }

        if (!chunksHavePermissions) {
            // No permissions have been set for any chunks, so nothing to write
            return;
        }
        sql.deleteCharAt(sql.length() - 1);

        try (PreparedStatement statement = prep(claimChunk, connection, sql.toString())) {
            int i = 0;
            for (DataChunk c : chunks) {
                for (Map.Entry<UUID, ChunkPlayerPermissions> entry :
                        c.playerPermissions.entrySet()) {
                    statement.setString(6 * i + 1, c.chunk.getWorld());
                    statement.setInt(6 * i + 2, c.chunk.getX());
                    statement.setInt(6 * i + 3, c.chunk.getZ());
                    statement.setString(6 * i + 4, c.player.toString());
                    statement.setString(6 * i + 5, entry.getKey().toString());
                    statement.setInt(6 * i + 6, entry.getValue().getPermissionFlags());
                    statement.setInt(6 * i + 6, entry.getValue().getPermissionFlags());
                    i++;
                }
            }

            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to add chunk accesses: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public void takePlayerAccess(ChunkPos chunk, UUID accessor) {
        // Get chunk ID
        String getChunkIdSql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=? AND `%s`=? AND `%s`=?",
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);

        try (PreparedStatement chunkIdStatement = prep(claimChunk, connection, getChunkIdSql)) {
            chunkIdStatement.setString(1, chunk.getWorld());
            chunkIdStatement.setInt(2, chunk.getX());
            chunkIdStatement.setInt(3, chunk.getZ());

            try (ResultSet chunkIdResult = chunkIdStatement.executeQuery()) {
                if (chunkIdResult.next()) {
                    int chunkId = chunkIdResult.getInt(1);

                    // Delete access for the chunk for the given player
                    String deleteSql =
                            String.format(
                                    "DELETE FROM `%s` WHERE `%s`=? AND `%s`=?",
                                    ACCESS_TABLE_NAME, ACCESS_CHUNK_ID, ACCESS_OTHER);
                    try (PreparedStatement deleteStatement =
                            prep(claimChunk, connection, deleteSql)) {
                        deleteStatement.setInt(1, chunkId);
                        deleteStatement.setString(2, accessor.toString());

                        deleteStatement.execute();
                    }
                }
            }
        } catch (Exception e) {
            Utils.err("Failed to take player's access to chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    @Override
    public Map<UUID, ChunkPlayerPermissions> getPlayersWithAccess(ChunkPos chunk) {
        String getPlayerPermsSql =
                String.format(
                        "SELECT `%1$s`.`%2$s`, `%1$s`.`%3$s` FROM `%1$s` "
                                + "INNER JOIN `%4$s` ON `%1$s`.`%5$s`=`%4$s`.`%6$s` "
                                + "WHERE `%4$s`.`%7$s`=? AND `%4$s`.`%8$s`=? AND `%4$s`.`%9$s`=?",
                        ACCESS_TABLE_NAME,
                        ACCESS_OTHER,
                        ACCESS_PERMISSIONS_FLAGS,
                        CLAIMED_CHUNKS_TABLE_NAME,
                        ACCESS_CHUNK_ID,
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z);

        try (PreparedStatement statement = prep(claimChunk, connection, getPlayerPermsSql)) {
            statement.setString(1, chunk.getWorld());
            statement.setInt(2, chunk.getX());
            statement.setInt(3, chunk.getZ());

            try (ResultSet result = statement.executeQuery()) {
                Map<UUID, ChunkPlayerPermissions> playerPermissions = new HashMap<>();
                while (result.next()) {
                    playerPermissions.put(
                            UUID.fromString(result.getString(1)),
                            new ChunkPlayerPermissions(result.getInt(2)));
                }
                return playerPermissions;
            }
        } catch (Exception e) {
            Utils.err("Failed to get player permissions for chunk: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        return null;
    }

    private Map<Integer, Map<UUID, ChunkPlayerPermissions>> getPlayerPermissionsForAllChunks() {
        // Get permissions for all claimed chunks
        Map<Integer, Map<UUID, ChunkPlayerPermissions>> allChunkPerms = new HashMap<>();

        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s` FROM `%s`",
                        ACCESS_CHUNK_ID, ACCESS_OTHER, ACCESS_PERMISSIONS_FLAGS, ACCESS_TABLE_NAME);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Integer chunkId = result.getInt(1);
                    UUID playerWithPerms = UUID.fromString(result.getString(2));
                    ChunkPlayerPermissions perms = new ChunkPlayerPermissions(result.getInt(3));
                    if (allChunkPerms.containsKey(chunkId)) {
                        allChunkPerms.get(chunkId).put(playerWithPerms, perms);
                    } else {
                        Map<UUID, ChunkPlayerPermissions> newPermsMap = new HashMap<>();
                        newPermsMap.put(playerWithPerms, perms);
                        allChunkPerms.put(chunkId, newPermsMap);
                    }
                }
            }
        } catch (Exception e) {
            Utils.err("Failed to get all chunk permissions");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        return allChunkPerms;
    }

    private void createClaimedChunksTable() throws Exception {
        String sql =
                String.format(
                        "CREATE TABLE `%s` ("
                                + "`%s` INT NOT NULL AUTO_INCREMENT," // ID (for per-chunk access)
                                + "`%s` VARCHAR(64) NOT NULL," // World
                                + "`%s` INT NOT NULL," // X
                                + "`%s` INT NOT NULL," // Z
                                + "`%s` BOOL NOT NULL DEFAULT 0," // TNT
                                + "`%s` VARCHAR(36) NOT NULL," // Owner (UUIDs are always 36 chars)
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        CLAIMED_CHUNKS_TABLE_NAME,
                        CLAIMED_CHUNKS_ID,
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_OWNER);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            Utils.err("Failed to create claimed chunks table: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            throw e;
        }
    }

    private void createJoinedPlayersTable() throws Exception {
        String sql =
                String.format(
                        "CREATE TABLE `%s` ("
                                + "`%s` VARCHAR(36) NOT NULL," // UUID
                                + "`%s` VARCHAR(64) NOT NULL," // In-game name
                                + "`%s` VARCHAR(64) NULL DEFAULT NULL," // Chunk display name
                                + "`%s` BIGINT NOT NULL," // Last join time in ms
                                + "`%s` BOOL NOT NULL," // Enable alerts
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        PLAYERS_TABLE_NAME,
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN,
                        PLAYERS_ALERT);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            Utils.err("Failed to create claimed chunks table: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            throw e;
        }
    }

    private void createAccessTable() throws Exception {
        String sql =
                String.format(
                        "CREATE TABLE `%s` ("
                                + "`%s` INT NOT NULL AUTO_INCREMENT," // Access ID (for primary key)
                                + "`%s` INT NULL DEFAULT NULL," // Chunk ID (for per-chunk access)
                                + "`%s` VARCHAR(36) NOT NULL," // Granter
                                + "`%s` VARCHAR(36) NOT NULL," // Granted
                                + "`%s` INT NOT NULL," // Permission flags
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        ACCESS_TABLE_NAME,
                        ACCESS_ACCESS_ID,
                        ACCESS_CHUNK_ID,
                        ACCESS_OWNER,
                        ACCESS_OTHER,
                        ACCESS_PERMISSIONS_FLAGS);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            Utils.err("Failed to create access table: %s", e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Updates 0.0.15 access tables to 0.0.16. Fixes chunk id not being nullable.
     *
     * @since 0.0.16
     */
    private void migrateAccessTable0015_0016() {
        try {
            if (!getColumnIsNullable(claimChunk, connection, ACCESS_TABLE_NAME, ACCESS_CHUNK_ID)) {
                Utils.debug("Migrating access table from 0.0.15 to 0.0.16+");

                // Allow null and make it the default
                String sql =
                        String.format(
                                "ALTER TABLE `%s` MODIFY `%s` INT NULL DEFAULT NULL",
                                ACCESS_TABLE_NAME, ACCESS_CHUNK_ID);
                try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
                    statement.executeUpdate();
                    Utils.debug("Successfully migrated access table from 0.0.15 to 0.0.16+");
                } catch (Exception e) {
                    Utils.err("Failed to migrate access table: %s", e.getMessage());
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SQLException e) {
            Utils.err(
                    "Failed to determine if access table needs updated from 0.0.15 to 0.0.16+: %s",
                    e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Updates 0.0.23 access tables to 0.0.24. Allows granular permissions
     *
     * @since 0.0.24
     */
    private void migrateAccessTable0023_0024() {
        try {
            if (!getColumnExists(
                    claimChunk, connection, dbName, ACCESS_TABLE_NAME, ACCESS_PERMISSIONS_FLAGS)) {
                Utils.debug("Migrating access table from 0.0.23 to 0.0.24+");

                // Alter table to add new permissions column
                String alterTableSql =
                        String.format(
                                "ALTER TABLE `%s` ADD `%s` INT DEFAULT ?",
                                ACCESS_TABLE_NAME, ACCESS_PERMISSIONS_FLAGS);
                try (PreparedStatement alterTableStatement =
                        prep(claimChunk, connection, alterTableSql)) {
                    alterTableStatement.setInt(1, 0);
                    alterTableStatement.execute();

                    // Add association record for each player with access to a specific chunk
                    DataChunk[] claimedChunks = getClaimedChunks();
                    // Sort chunks by owner
                    Map<UUID, List<DataChunk>> chunksByOwner = new HashMap<>();
                    for (DataChunk chunk : claimedChunks) {
                        if (!chunksByOwner.containsKey(chunk.player)) {
                            chunksByOwner.put(chunk.player, new ArrayList<>());
                        }
                        chunksByOwner.get(chunk.player).add(chunk);
                    }

                    String getAccessesSql =
                            String.format(
                                    "SELECT `%s`, `%s` FROM `%s`",
                                    ACCESS_OWNER, ACCESS_OTHER, ACCESS_TABLE_NAME);
                    try (PreparedStatement getAccessesStatement =
                            prep(claimChunk, connection, getAccessesSql)) {
                        try (ResultSet result = getAccessesStatement.executeQuery()) {

                            while (result.next()) {
                                UUID chunkOwner = UUID.fromString(result.getString(1));
                                UUID accessor = UUID.fromString(result.getString(2));

                                // Grant default permissions to this accessor for all chunks
                                // belonging to this chunkOwner
                                for (DataChunk chunk :
                                        chunksByOwner.getOrDefault(chunkOwner, new ArrayList<>())) {
                                    chunk.playerPermissions.put(
                                            accessor,
                                            ChunkPlayerPermissions.fromPermissionsMap(
                                                    Utils.getDefaultPermissionsMap()));
                                }
                            }

                            writeAccessAssociationsBulk(claimedChunks);

                            // Delete existing access records
                            String deleteExistingSql =
                                    String.format(
                                            "DELETE FROM `%s` WHERE `%s` IS NULL",
                                            ACCESS_TABLE_NAME, ACCESS_CHUNK_ID);
                            try (PreparedStatement deleteExistingStatement =
                                    prep(claimChunk, connection, deleteExistingSql)) {
                                deleteExistingStatement.execute();
                            }
                        }
                    }
                } catch (Exception e) {
                    Utils.err("Failed to migrate access table: %s", e.getMessage());
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (SQLException e) {
            Utils.err(
                    "Failed to determine if access table needs updating from 0.0.23 to 0.0.24+: %s",
                    e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Updates 0.0.15 claimed chunks tables to 0.0.16. Allows per-chunk TNT.
     *
     * @since 0.0.16
     */
    private void migrateClaimedChunksTable0015_0016() {
        try {
            if (!getColumnExists(
                    claimChunk,
                    connection,
                    dbName,
                    CLAIMED_CHUNKS_TABLE_NAME,
                    CLAIMED_CHUNKS_TNT)) {
                Utils.debug("Migrating claimed chunks table from 0.0.15 to 0.0.16+");

                // Allow null and make it the default
                String sql =
                        String.format(
                                "ALTER TABLE `%s` ADD `%s` BOOL NOT NULL DEFAULT 0 AFTER `%s`",
                                CLAIMED_CHUNKS_TABLE_NAME, CLAIMED_CHUNKS_TNT, CLAIMED_CHUNKS_Z);
                try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
                    statement.executeUpdate();
                    Utils.debug(
                            "Successfully migrated claimed chunks table from 0.0.15 to 0.0.16+");
                } catch (Exception e) {
                    Utils.err("Failed to migrate claimed chunks table: %s", e.getMessage());
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SQLException e) {
            Utils.err(
                    "Failed to determine if claimed chunks table needs updated from 0.0.15 to"
                            + " 0.0.16+: %s",
                    e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Updates 0.0.23 players to 0.0.24 after max claim update.
     *
     * @since 0.0.24
     */
    private void migratePlayerTableMaxClaim0023_0024() {
        try {
            if (!getColumnExists(
                    claimChunk, connection, dbName, PLAYERS_TABLE_NAME, PLAYERS_MAX_CLAIM)) {
                Utils.debug("Migrating players table from 0.0.23 to 0.0.24+");

                // Allow null and make it the default
                String sql =
                        String.format(
                                "ALTER TABLE `%s` ADD `%s` INT NOT NULL DEFAULT 0 AFTER `%s`",
                                PLAYERS_TABLE_NAME, PLAYERS_MAX_CLAIM, PLAYERS_ALERT);
                try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
                    statement.executeUpdate();
                    Utils.debug("Successfully migrated players table from 0.0.23 to 0.0.24+");
                } catch (Exception e) {
                    Utils.err("Failed to migrate players table: %s", e.getMessage());
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SQLException e) {
            Utils.err(
                    "Failed to determine if claimed chunks table needs updated from 0.0.15 to"
                            + " 0.0.16+: %s",
                    e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
