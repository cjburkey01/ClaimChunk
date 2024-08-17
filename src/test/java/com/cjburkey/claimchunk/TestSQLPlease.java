package com.cjburkey.claimchunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.cjburkey.claimchunk.chunk.ChunkPlayerPermissions;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.sqlite.SqLiteTableMigrationManager;
import com.cjburkey.claimchunk.data.sqlite.SqLiteWrapper;
import com.cjburkey.claimchunk.player.FullPlayerData;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class TestSQLPlease {

    @Test
    void ensureColumnExistsMethodWorks() {
        // Must create the wrapper to initialize (and deinitialize) connection
        try (TestQlWrap ignoredWrapper = new TestQlWrap()) {
            // Make sure that instantiating SqLiteWrapper created the tables
            assert SqLiteTableMigrationManager.columnExists("player_data", "player_uuid");
            assert SqLiteTableMigrationManager.columnExists("chunk_data", "owner_uuid");
            assert SqLiteTableMigrationManager.tableExists("flags_player_chunk_player_enabled");
            assert !SqLiteTableMigrationManager.tableExists("bob_the_builder_no_we_cant");
            assert !SqLiteTableMigrationManager.columnExists("chunk_hell", "permission_bits");
            assert !SqLiteTableMigrationManager.columnExists("player_data", "fake_col");
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
                            ply1Uuid, "SomeGuysName", null, System.currentTimeMillis(), true, 0));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            ply2Uuid,
                            "OtherPersonsName",
                            "queenshit",
                            System.currentTimeMillis(),
                            false,
                            0));

            // Make fake accessors and permissions
            UUID accessorUuid1 = UUID.randomUUID();
            UUID accessorUuid2 = UUID.randomUUID();
            ChunkPlayerPermissions permissions1 = new ChunkPlayerPermissions(0b11111111);
            ChunkPlayerPermissions permissions2 = new ChunkPlayerPermissions(0b10101101);

            // Add a chunk to the player and give the permissions to the other players
            ChunkPos chunkPos = new ChunkPos("world", 10, -3);
            DataChunk chunkData = new DataChunk(chunkPos, ply1Uuid, new HashMap<>(), false);
            chunkData.playerPermissions.put(accessorUuid1, permissions1);
            chunkData.playerPermissions.put(accessorUuid2, permissions2);
            wrapper.sql.addClaimedChunk(chunkData);

            // Make sure both players get loaded
            Collection<FullPlayerData> players = wrapper.sql.getAllPlayers();
            assertEquals(2, players.size());
            assert players.stream()
                    .allMatch(ply -> ply.player.equals(ply1Uuid) || ply.player.equals(ply2Uuid));
            assert players.stream().anyMatch(ply -> "queenshit".equals(ply.chunkName));

            // Load the chunk after adding it
            //noinspection deprecation
            Collection<DataChunk> loadedChunks = SqLiteWrapper.getAllChunksLegacy();
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
            DataChunk chunkData = new DataChunk(chunk, owner, new HashMap<>(), false);
            chunkData.playerPermissions.put(accessor1, new ChunkPlayerPermissions(0b01));
            chunkData.playerPermissions.put(accessor2, new ChunkPlayerPermissions(0b10));

            // Add the players
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            owner, "PersonHere", null, System.currentTimeMillis(), true, 0));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessor1, "PersonThere", null, System.currentTimeMillis(), true, 0));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessor2, "AnotherOne", null, System.currentTimeMillis(), true, 0));

            // Add the chunk
            wrapper.sql.addClaimedChunk(chunkData);

            // Load the chunk and make sure it contains both accessors
            //noinspection deprecation
            Map<UUID, ChunkPlayerPermissions> loadedPerms =
                    SqLiteWrapper.getAllChunksLegacy().iterator().next().playerPermissions;
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
                            owner, "PersonHere", null, System.currentTimeMillis(), true, 0));
            wrapper.sql.addPlayer(
                    new FullPlayerData(
                            accessor, "PersonThere", null, System.currentTimeMillis(), true, 0));
            wrapper.sql.addClaimedChunk(new DataChunk(chunk, owner, new HashMap<>(), false));

            // Insert the permission and check it
            wrapper.sql.setPlayerAccess(chunk, accessor, flags1);
            //noinspection deprecation
            assertEquals(
                    flags1,
                    SqLiteWrapper.getAllChunksLegacy()
                            .iterator()
                            .next()
                            .playerPermissions
                            .get(accessor)
                            .permissionFlags);

            // Update the permission and check it
            wrapper.sql.setPlayerAccess(chunk, accessor, flags2);
            //noinspection deprecation
            assertEquals(
                    flags2,
                    SqLiteWrapper.getAllChunksLegacy()
                            .iterator()
                            .next()
                            .playerPermissions
                            .get(accessor)
                            .permissionFlags);

            // Remove the permission and make sure there aren't any permissions now
            wrapper.sql.removePlayerAccess(chunk, accessor);
            //noinspection deprecation
            assert SqLiteWrapper.getAllChunksLegacy().iterator().next().playerPermissions.isEmpty();
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
