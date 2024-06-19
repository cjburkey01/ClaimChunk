package com.cjburkey.claimchunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.sqlite.SqLiteTableMigrationManager;
import com.cjburkey.claimchunk.data.sqlite.SqLiteWrapper;
import com.cjburkey.claimchunk.player.FullPlayerData;
import com.zaxxer.q2o.Q2Sql;
import com.zaxxer.q2o.q2o;

import org.junit.jupiter.api.Test;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class TestSQLPlease {

    @Test
    void ensureColumnExistsMethodWorks() {
        // Must create the wrapper to initialize (and deinitialize) connection
        try (TestQlWrap ignoredWrapper = new TestQlWrap()) {
            assert SqLiteTableMigrationManager.tableExists("player_data");

            // Make sure that instantiating SqLiteWrapper created the tables
            assert SqLiteTableMigrationManager.columnExists("player_data", "player_uuid");
            assert !SqLiteTableMigrationManager.columnExists("chunk_hell", "permission_bits");
            assert !SqLiteTableMigrationManager.columnExists("player_data", "fake_col");
        }
    }

    @Test
    void ensureSchemaVersionIsValid() {
        try (TestQlWrap ignoredWrapper = new TestQlWrap()) {
            // -1 value represents an error, the initial version of the schema starts at 1 (I decided), so 0 is invalid and we shouldn't get it.
            assert SqLiteTableMigrationManager.getSchemaVersion() > 0;
        }
    }

    @Test
    void ensureMigrationWorks() throws IOException {
        File dbFile = randomDbFile();

        {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            boolean ignored = dbFile.createNewFile();
            dataSource.setUrl("jdbc:sqlite:" + dbFile);
            q2o.initializeTxNone(dataSource);
            dbFile.deleteOnExit();
            // Old table format
            Q2Sql.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS player_data (
                        player_uuid TEXT PRIMARY KEY NOT NULL,
                        last_ign TEXT NOT NULL,
                        chunk_name TEXT,
                        last_online_time INTEGER NOT NULL,
                        alerts_enabled INTEGER NOT NULL,
                        extra_max_claims INTEGER NOT NULL
                    ) STRICT
                    """);
            q2o.deinitialize();
        }

        try (TestQlWrap ignoredWrapper = new TestQlWrap()) {
            // Make sure the migration code added the column to the existing table
            assert SqLiteTableMigrationManager.columnExists(
                    "player_data", "default_chunk_permissions");
        }
    }

    @Test
    void ensureNoDataLoss() {
        try (TestQlWrap wrapper = new TestQlWrap()) {
            // Add a random player
            UUID ply1Uuid = UUID.randomUUID();
            UUID ply2Uuid = UUID.randomUUID();
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            ply1Uuid,
                            "SomeGuysName",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            ply2Uuid,
                            "OtherPersonsName",
                            "queenshit",
                            System.currentTimeMillis(),
                            false,
                            0,
                            new ChunkPlayerPermissions(0)));

            // Make fake accessors and permissions
            UUID accessorUuid1 = UUID.randomUUID();
            UUID accessorUuid2 = UUID.randomUUID();
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessorUuid1,
                            "blajsd",
                            "g4g4",
                            System.currentTimeMillis(),
                            false,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessorUuid2,
                            "nlfjkdsf",
                            "4vsrg",
                            System.currentTimeMillis(),
                            false,
                            0,
                            new ChunkPlayerPermissions(0)));
            ChunkPlayerPermissions permissions1 = new ChunkPlayerPermissions(0b11111111);
            ChunkPlayerPermissions permissions2 = new ChunkPlayerPermissions(0b10101101);

            // Add a chunk to the player and give the permissions to the other players
            ChunkPos chunkPos = new ChunkPos("world", 10, -3);
            DataChunk chunkData = new DataChunk(chunkPos, ply1Uuid, new HashMap<>(), null);
            chunkData.playerPermissions.put(accessorUuid1, permissions1);
            chunkData.playerPermissions.put(accessorUuid2, permissions2);
            wrapper.sql.addClaimedChunk(chunkData);

            // Make sure all four players get loaded
            Collection<FullPlayerData> players = wrapper.sql.getAllPlayers();
            assertEquals(4, players.size());
            assert players.stream()
                    .allMatch(ply -> ply.player.equals(ply1Uuid) || ply.player.equals(ply2Uuid) || ply.player.equals(accessorUuid1) || ply.player.equals(accessorUuid2));
            assert players.stream().anyMatch(ply -> "queenshit".equals(ply.chunkName));

            // Load the chunk after adding it
            Collection<DataChunk> loadedChunks = wrapper.sql.getAllChunks();
            DataChunk loadedChunk = loadedChunks.iterator().next();
            assertNotNull(loadedChunk);

            // Make sure the chunk exists when we load from the database
            assert loadedChunk.player.equals(ply1Uuid) && loadedChunk.chunk.equals(chunkPos);
            // Make sure the chunk permission got loaded correctly
            assertEquals(permissions1, loadedChunk.playerPermissions.get(accessorUuid1));
            assertEquals(permissions2, loadedChunk.playerPermissions.get(accessorUuid2));
        }
    }

    @Test
    void multiplePermissions() {
        try (TestQlWrap wrapper = new TestQlWrap()) {
            UUID owner = UUID.randomUUID();
            UUID accessor1 = UUID.randomUUID();
            UUID accessor2 = UUID.randomUUID();
            ChunkPos chunk = new ChunkPos("world", 824, -29);
            DataChunk chunkData = new DataChunk(chunk, owner, new HashMap<>(), null);
            chunkData.playerPermissions.put(accessor1, new ChunkPlayerPermissions(0b01));
            chunkData.playerPermissions.put(accessor2, new ChunkPlayerPermissions(0b10));

            // Add the players
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            owner,
                            "PersonHere",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessor1,
                            "PersonThere",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessor2,
                            "AnotherOne",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));

            // Add the chunk
            wrapper.sql.addClaimedChunk(chunkData);

            // Load the chunk and make sure it contains both accessors
            Map<UUID, ChunkPlayerPermissions> loadedPerms =
                    wrapper.sql.getAllChunks().iterator().next().playerPermissions;
            assert loadedPerms.containsKey(accessor1);
            assert loadedPerms.containsKey(accessor2);
        }
    }

    @Test
    void insertOrUpdatePermission() {
        try (TestQlWrap wrapper = new TestQlWrap()) {
            UUID owner = UUID.randomUUID();
            UUID accessor = UUID.randomUUID();
            ChunkPos chunk = new ChunkPos("world", 824, -29);
            int flags1 = 0b10101001;
            int flags2 = 0b01010100;

            // Add the players and the chunk
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            owner,
                            "PersonHere",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessor,
                            "PersonThere",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addClaimedChunk(new DataChunk(chunk, owner, new HashMap<>(), null));

            // Insert the permission and check it
            wrapper.sql.setPlayerAccess(chunk, accessor, flags1);
            assertEquals(
                    flags1,
                    wrapper.sql
                            .getAllChunks()
                            .iterator()
                            .next()
                            .playerPermissions
                            .get(accessor)
                            .permissionFlags);

            // Update the permission and check it
            wrapper.sql.setPlayerAccess(chunk, accessor, flags2);
            assertEquals(
                    flags2,
                    wrapper.sql
                            .getAllChunks()
                            .iterator()
                            .next()
                            .playerPermissions
                            .get(accessor)
                            .permissionFlags);

            // Remove the permission and make sure there aren't any permissions now
            wrapper.sql.removePlayerAccess(chunk, accessor);
            assert wrapper.sql.getAllChunks().iterator().next().playerPermissions.isEmpty();
        }
    }

    @Test
    void removeClaims() {
        try (TestQlWrap wrapper = new TestQlWrap()) {
            // Add random players
            UUID ply1Uuid = UUID.randomUUID();
            UUID ply2Uuid = UUID.randomUUID();
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            ply1Uuid,
                            "SomeGuysName",
                            null,
                            System.currentTimeMillis(),
                            true,
                            0,
                            new ChunkPlayerPermissions(0)));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            ply2Uuid,
                            "OtherPersonsName",
                            "queenshit",
                            System.currentTimeMillis(),
                            false,
                            0,
                            new ChunkPlayerPermissions(0)));

            // Add chunks
            ChunkPos chunk1 = new ChunkPos("world", 824, -29);
            ChunkPos chunk2 = new ChunkPos("world_nether", -4, 29);
            wrapper.sql.addClaimedChunk(new DataChunk(chunk1, ply1Uuid, new HashMap<>(), null));
            wrapper.sql.addClaimedChunk(new DataChunk(chunk2, ply2Uuid, new HashMap<>(), null));

            // Delete one chunk
            wrapper.sql.removeClaimedChunk(chunk2);

            // Make sure chunk2 still exists but chunk1 doesn't.
            Collection<DataChunk> chunks = wrapper.sql.getAllChunks();
            assert chunks.stream().noneMatch(chunk -> chunk.chunk.equals(chunk2));

            // Delete the other chunk
            wrapper.sql.removeClaimedChunk(chunk1);

            // Make sure no chunks are left.
            assert wrapper.sql.getAllChunks().isEmpty();
        }
    }

    protected static File randomDbFile() {
        return new File(UUID.randomUUID() + ".tmp.sqlite3");
    }

    static class TestQlWrap implements AutoCloseable {
        SqLiteWrapper sql;
        File dbFile;

        TestQlWrap() {
            dbFile = randomDbFile();
            sql = new SqLiteWrapper(dbFile, false);
            dbFile.deleteOnExit();
        }

        @Override
        public void close() {
            sql.close();
        }
    }
}
