package com.cjburkey.claimchunk.data.newdata;

import static com.cjburkey.claimchunk.data.newdata.SqlBacking.*;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.cjburkey.claimchunk.player.SimplePlayerData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Some servers keep MySQL on a separate host from the server. In this case, making a MySQL request
 * is (relatively) EXTREMELY slow. This data handler only makes requests when it initializes, saves
 * data, and loads data. This handler uses a JSON handler so backups are possible.
 *
 * @param <T> The type of the backup data system.
 * @since 0.0.16
 */
public class BulkMySQLDataHandler<T extends IClaimChunkDataHandler> extends MySQLDataHandler<T>
        implements IClaimChunkDataHandler {

    private final ClaimChunk claimChunk;
    private final boolean doBackups;
    private final JsonDataHandler dataHandler;

    public BulkMySQLDataHandler(
            @NotNull ClaimChunk claimChunk,
            @Nullable File claimedChunksFile,
            @Nullable File joinedPlayersFile,
            Supplier<T> oldDataHandler,
            Consumer<T> onCleanOld) {
        super(claimChunk, oldDataHandler, onCleanOld);

        this.claimChunk = claimChunk;

        doBackups = claimedChunksFile != null && joinedPlayersFile != null;
        dataHandler = new JsonDataHandler(claimChunk, claimedChunksFile, joinedPlayersFile);
    }

    public BulkMySQLDataHandler(
            ClaimChunk claimChunk, Supplier<T> oldDataHandler, Consumer<T> onCleanOld) {
        this(claimChunk, null, null, oldDataHandler, onCleanOld);
    }

    @Override
    public void init() throws Exception {
        dataHandler.init();

        super.init();
    }

    @Override
    public boolean getHasInit() {
        return super.getHasInit();
    }

    @Override
    public void exit() throws SQLException {
        dataHandler.exit();

        super.exit();
    }

    @Override
    public void save() throws Exception {
        // Clear the chunks table and accesses table
        {
            try (PreparedStatement statement =
                    prep(
                            claimChunk,
                            super.connection,
                            String.format("DELETE FROM `%s`", CLAIMED_CHUNKS_TABLE_NAME))) {
                statement.execute();
                try (PreparedStatement clearAccessesStatement =
                        prep(
                                claimChunk,
                                super.connection,
                                String.format("DELETE FROM `%s`", ACCESS_TABLE_NAME))) {
                    clearAccessesStatement.execute();
                }
            } catch (Exception e) {
                Utils.err("Failed to clear chunks table and accesses table");
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        // Add the current chunks to the chunks table
        super.addClaimedChunks(dataHandler.getClaimedChunks());

        // Clear the players table
        {
            try (PreparedStatement statement =
                    prep(
                            claimChunk,
                            super.connection,
                            String.format("DELETE FROM `%s`", PLAYERS_TABLE_NAME))) {
                statement.execute();
            } catch (Exception e) {
                Utils.err("Failed to clear players table");
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
        // Add the current players to the players table
        super.addPlayers(dataHandler.getFullPlayerData());

        // Perform JSON backups if necessary
        if (doBackups) dataHandler.save();
    }

    @Override
    public void load() {
        dataHandler.clearData();

        dataHandler.addClaimedChunks(super.getClaimedChunks());
        dataHandler.addPlayers(super.getFullPlayerData());
    }

    @Override
    public void addClaimedChunk(ChunkPos pos, UUID player) {
        dataHandler.addClaimedChunk(pos, player);
    }

    @Override
    public void addClaimedChunks(DataChunk[] chunks) {
        dataHandler.addClaimedChunks(chunks);
    }

    @Override
    public void removeClaimedChunk(ChunkPos pos) {
        dataHandler.removeClaimedChunk(pos);
        super.removeClaimedChunk(pos);
    }

    @Override
    public boolean isChunkClaimed(ChunkPos pos) {
        return dataHandler.isChunkClaimed(pos);
    }

    @Override
    @Nullable
    public UUID getChunkOwner(ChunkPos pos) {
        return dataHandler.getChunkOwner(pos);
    }

    @Override
    public DataChunk[] getClaimedChunks() {
        return dataHandler.getClaimedChunks();
    }

    @Override
    public void addPlayer(
            UUID player,
            String lastIgn,
            String chunkName,
            long lastOnlineTime,
            boolean alerts,
            int maxClaims) {
        dataHandler.addPlayer(player, lastIgn, chunkName, lastOnlineTime, alerts, maxClaims);
    }

    @Override
    public void addPlayers(FullPlayerData[] players) {
        dataHandler.addPlayers(players);
    }

    @Override
    @Nullable
    public String getPlayerUsername(UUID player) {
        return dataHandler.getPlayerUsername(player);
    }

    @Override
    @Nullable
    public UUID getPlayerUUID(String username) {
        return dataHandler.getPlayerUUID(username);
    }

    @Override
    public void setPlayerLastOnline(UUID player, long time) {
        dataHandler.setPlayerLastOnline(player, time);
    }

    @Override
    public void setPlayerChunkName(UUID player, String name) {
        dataHandler.setPlayerChunkName(player, name);
    }

    @Override
    @Nullable
    public String getPlayerChunkName(UUID player) {
        return dataHandler.getPlayerChunkName(player);
    }

    @Override
    public void givePlayerAccess(
            ChunkPos chunk, UUID accessor, ChunkPlayerPermissions permissions) {
        dataHandler.givePlayerAccess(chunk, accessor, permissions);
    }

    @Override
    public void takePlayerAccess(ChunkPos chunk, UUID accessor) {
        dataHandler.takePlayerAccess(chunk, accessor);
        super.takePlayerAccess(chunk, accessor);
    }

    @Override
    public Map<UUID, ChunkPlayerPermissions> getPlayersWithAccess(ChunkPos chunk) {
        return dataHandler.getPlayersWithAccess(chunk);
    }

    @Override
    public void setPlayerReceiveAlerts(UUID player, boolean alert) {
        dataHandler.setPlayerReceiveAlerts(player, alert);
    }

    @Override
    public boolean getPlayerReceiveAlerts(UUID player) {
        return dataHandler.getPlayerReceiveAlerts(player);
    }

    @Override
    public void setPlayerExtraMaxClaims(UUID player, int extraMaxClaims) {
        dataHandler.setPlayerExtraMaxClaims(player, extraMaxClaims);
    }

    @Override
    public void addPlayerExtraMaxClaims(UUID player, int numToAdd) {
        // abs is performed in json data handler
        dataHandler.addPlayerExtraMaxClaims(player, numToAdd);
    }

    @Override
    public void takePlayerExtraMaxClaims(UUID player, int numToTake) {
        dataHandler.takePlayerExtraMaxClaims(player, numToTake);
    }

    @Override
    public int getPlayerExtraMaxClaims(UUID player) {
        return dataHandler.getPlayerExtraMaxClaims(player);
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return dataHandler.hasPlayer(player);
    }

    @Override
    public Collection<SimplePlayerData> getPlayers() {
        return dataHandler.getPlayers();
    }

    @Override
    public FullPlayerData[] getFullPlayerData() {
        return dataHandler.getFullPlayerData();
    }
}
