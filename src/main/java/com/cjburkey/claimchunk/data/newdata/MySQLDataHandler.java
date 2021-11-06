package com.cjburkey.claimchunk.data.newdata;

import static com.cjburkey.claimchunk.data.newdata.SqlBacking.*;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    private static final String ACCESS_TABLE_NAME = "access_granted";
    private static final String ACCESS_ACCESS_ID = "access_id";
    private static final String ACCESS_CHUNK_ID = "chunk_id";
    private static final String ACCESS_OWNER = "owner_uuid";
    private static final String ACCESS_OTHER = "other_uuid";

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
        dbName = claimChunk.chConfig().getDatabaseName();
        connection =
                connect(
                        claimChunk.chConfig().getDatabaseHostname(),
                        claimChunk.chConfig().getDatabasePort(),
                        dbName,
                        claimChunk.chConfig().getDatabaseUsername(),
                        claimChunk.chConfig().getDatabasePassword(),
                        claimChunk.chConfig().getUseSsl(),
                        claimChunk.chConfig().getAllowPublicKeyRetrieval());

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
            Utils.debug("Found joined players table");
        }
        if (getTableDoesntExist(claimChunk, connection, dbName, ACCESS_TABLE_NAME)) {
            Utils.debug("Creating access table");
            createAccessTable();
        } else {
            migrateAccessTable0015_0016();
            Utils.debug("Found access table");
        }

        if (oldDataHandler != null && claimChunk.chConfig().getConvertOldData()) {
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
        } catch (Exception e) {
            Utils.err("Failed add claimed chunks: %s", e.getMessage());
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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s`",
                        CLAIMED_CHUNKS_WORLD,
                        CLAIMED_CHUNKS_X,
                        CLAIMED_CHUNKS_Z,
                        CLAIMED_CHUNKS_TNT,
                        CLAIMED_CHUNKS_OWNER,
                        CLAIMED_CHUNKS_TABLE_NAME);
        List<DataChunk> chunks = new ArrayList<>();
        try (PreparedStatement statement = prep(claimChunk, connection, sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                chunks.add(
                        new DataChunk(
                                new ChunkPos(
                                        result.getString(1), result.getInt(2), result.getInt(3)),
                                UUID.fromString(result.getString(5)),
                                result.getBoolean(4)));
            }
        } catch (Exception e) {
            Utils.err("Failed to get all claimed chunks: %s", e.getMessage());
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
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void addPlayer(
            UUID player,
            String lastIgn,
            Set<UUID> permitted,
            @Nullable String chunkName,
            long lastOnlineTime,
            boolean alerts) {
        String sql =
                String.format(
                        "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?, ?)",
                        PLAYERS_TABLE_NAME,
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN,
                        PLAYERS_ALERT);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, player.toString());
            statement.setString(2, lastIgn);
            statement.setString(3, chunkName);
            statement.setLong(4, lastOnlineTime);
            statement.setBoolean(5, alerts);
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to add player: %s", e.getMessage());
            e.printStackTrace();
        }

        // Create the access associations separately
        givePlayersAccess(player, permitted.toArray(new UUID[0]));
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        if (players.length == 0) return;

        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`, `%s`) VALUES",
                                PLAYERS_TABLE_NAME,
                                PLAYERS_UUID,
                                PLAYERS_IGN,
                                PLAYERS_NAME,
                                PLAYERS_LAST_JOIN,
                                PLAYERS_ALERT));
        for (int i = 0; i < players.length; i++) {
            givePlayersAccess(players[i].player, players[i].permitted.toArray(new UUID[0]));
            sql.append(" (?, ?, ?, ?, ?)");
            if (i != players.length - 1) sql.append(',');
        }
        try (PreparedStatement statement = prep(claimChunk, connection, sql.toString())) {
            int i = 0;
            for (FullPlayerData player : players) {
                statement.setString(5 * i + 1, player.player.toString());
                statement.setString(5 * i + 2, player.lastIgn);
                statement.setString(5 * i + 3, player.chunkName);
                statement.setLong(5 * i + 4, player.lastOnlineTime);
                statement.setBoolean(5 * i + 5, player.alert);
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed to add joined players: %s", e.getMessage());
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
            e.printStackTrace();
        }
        return false;
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
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        String sql =
                String.format(
                        "SELECT `%s`, `%s`, `%s`, `%s`, `%s` FROM `%s` LIMIT 1",
                        PLAYERS_UUID,
                        PLAYERS_IGN,
                        PLAYERS_NAME,
                        PLAYERS_LAST_JOIN,
                        PLAYERS_ALERT,
                        PLAYERS_TABLE_NAME);
        ArrayList<FullPlayerData> players = new ArrayList<>();
        try (PreparedStatement statement = prep(claimChunk, connection, sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString(1));
                players.add(
                        new FullPlayerData(
                                uuid,
                                result.getString(2),
                                new HashSet<>(Arrays.asList(getPlayersWithAccess(uuid))),
                                result.getString(3),
                                result.getLong(4),
                                result.getBoolean(5)));
            }
        } catch (Exception e) {
            Utils.err("Failed to retrieve all players data: %s", e.getMessage());
            e.printStackTrace();
        }
        return players.toArray(new FullPlayerData[0]);
    }

    @Override
    public void setPlayerAccess(UUID owner, UUID accessor, boolean access) {
        if (access == playerHasAccess(owner, accessor)) return;
        if (access) {
            String sql =
                    String.format(
                            "INSERT INTO `%s` (`%s`, `%s`) VALUES (?, ?)",
                            ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER);
            try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, accessor.toString());
                statement.execute();
            } catch (Exception e) {
                Utils.err("Failed give player chunk access: %s", e.getMessage());
                e.printStackTrace();
            }
        } else {
            String sql =
                    String.format(
                            "DELETE FROM `%s` WHERE `%s`=? AND `%s`=?",
                            ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER);
            try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
                statement.setString(1, owner.toString());
                statement.setString(2, accessor.toString());
                statement.execute();
            } catch (Exception e) {
                Utils.err("Failed to remove player chunk access: %s", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void givePlayersAccess(UUID owner, UUID[] accessors) {
        if (accessors.length == 0) return;

        // Determine which of the provided accessors actually need to be GIVEN access
        HashSet<UUID> withAccess = new HashSet<>(Arrays.asList(getPlayersWithAccess(owner)));
        HashSet<UUID> needAccess = new HashSet<>();
        for (UUID accessor : accessors) {
            if (!withAccess.contains(accessor)) needAccess.add(accessor);
        }

        // Use a single query to add all the access associations
        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "INSERT INTO `%s` (`%s`, `%s`) VALUES",
                                ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER));
        for (int i = 0; i < needAccess.size(); i++) {
            sql.append(" (?, ?)");
            if (i != needAccess.size() - 1) sql.append(',');
        }
        try (PreparedStatement statement = prep(claimChunk, connection, sql.toString())) {
            int i = 0;
            for (UUID accessor : needAccess) {
                statement.setString(2 * i + 1, owner.toString());
                statement.setString(2 * i + 2, accessor.toString());
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed give players chunk access: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void takePlayersAccess(UUID owner, UUID[] accessors) {
        if (accessors.length == 0) return;

        // Use a single query to remove all the access associations
        StringBuilder sql =
                new StringBuilder(
                        String.format(
                                "DELETE FROM `%s` WHERE (`%s`, `%s`) IN (",
                                ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER));
        for (int i = 0; i < accessors.length; i++) {
            sql.append("(?, ?)");
            if (i < accessors.length - 1) sql.append(", ");
        }
        sql.append(')');
        try (PreparedStatement statement = prep(claimChunk, connection, sql.toString())) {
            int i = 0;
            for (UUID accessor : accessors) {
                statement.setString(2 * i + 1, owner.toString());
                statement.setString(2 * i + 2, accessor.toString());
                i++;
            }
            statement.execute();
        } catch (Exception e) {
            Utils.err("Failed revoke players chunk access: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean playerHasAccess(UUID owner, UUID accessor) {
        String sql =
                String.format(
                        "SELECT count(*) FROM `%s` WHERE `%s`=? AND `%s`=?",
                        ACCESS_TABLE_NAME, ACCESS_OWNER, ACCESS_OTHER);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, owner.toString());
            statement.setString(2, accessor.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt(1) > 0;
            }
        } catch (Exception e) {
            Utils.err("Failed to check player access: %s", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public UUID[] getPlayersWithAccess(UUID owner) {
        String sql =
                String.format(
                        "SELECT `%s` FROM `%s` WHERE `%s`=?",
                        ACCESS_OTHER, ACCESS_TABLE_NAME, ACCESS_OWNER);
        List<UUID> accessors = new ArrayList<>();
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, owner.toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    accessors.add(UUID.fromString(result.getString(1)));
                }
            }
        } catch (Exception e) {
            Utils.err("Failed to get all claimed chunks: %s", e.getMessage());
            e.printStackTrace();
        }
        return accessors.toArray(new UUID[0]);
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
                                + "PRIMARY KEY (`%2$s`)"
                                + ") ENGINE = InnoDB",
                        ACCESS_TABLE_NAME,
                        ACCESS_ACCESS_ID,
                        ACCESS_CHUNK_ID,
                        ACCESS_OWNER,
                        ACCESS_OTHER);
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            Utils.err("Failed to create access table: %s", e.getMessage());
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
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SQLException e) {
            Utils.err(
                    "Failed to determine if access table needs updated from 0.0.15 to 0.0.16+: %s",
                    e.getMessage());
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
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SQLException e) {
            Utils.err(
                    "Failed to determine if claimed chunks table needs updated from 0.0.15 to"
                            + " 0.0.16+: %s",
                    e.getMessage());
            e.printStackTrace();
        }
    }
}
