package com.cjburkey.claimchunk;

import static org.junit.jupiter.api.Assertions.*;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.sqlite.SqLiteTableMigrationManager;
import com.cjburkey.claimchunk.data.sqlite.SqLiteWrapper;
import com.cjburkey.claimchunk.player.FullPlayerData;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class TestSQLPlease {

    @Test
    void ensureColumnExistsMethodWorks() {
        // Must create the wrapper to initialize (and deinitialize) connection
        try (TestQlWrap ignoredWrapper = new TestQlWrap()) {
            assertTrue(SqLiteTableMigrationManager.columnExists("player_data", "player_uuid"));
            assertTrue(SqLiteTableMigrationManager.columnExists("chunk_data", "owner_uuid"));
            assertTrue(SqLiteTableMigrationManager.tableExists("permission_flags"));
            assertFalse(SqLiteTableMigrationManager.tableExists("bob_the_builder_no_we_cant"));
            assertFalse(SqLiteTableMigrationManager.columnExists("chunk_hell", "permission_bits"));
            assertFalse(SqLiteTableMigrationManager.columnExists("player_data", "fake_col"));
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void test0026BasicsWork() {
        try (TestQlWrap wrapper = new TestQlWrap()) {
            assertTrue(SqLiteTableMigrationManager.tableExists("player_data"));
            assertTrue(SqLiteTableMigrationManager.tableExists("chunk_data"));
            assertTrue(SqLiteTableMigrationManager.tableExists("permission_flags"));

            FullPlayerData examplePlayer1 =
                    new FullPlayerData(
                            UUID.randomUUID(), "BobMarley", "Chris Farley", 924789, true, 0);
            FullPlayerData examplePlayer2 =
                    new FullPlayerData(
                            UUID.randomUUID(), "HisBrother", "Tommy Boy", 63425, false, 3);
            wrapper.sql.addPlayer(examplePlayer1);
            wrapper.sql.addPlayer(examplePlayer2);

            DataChunk exampleChunk1 =
                    new DataChunk(new ChunkPos("world", 39, -91), examplePlayer1.player);
            DataChunk exampleChunk2 =
                    new DataChunk(new ChunkPos("world_the_nether", -17, 1), examplePlayer2.player);
            wrapper.sql.addClaimedChunk(exampleChunk1);
            wrapper.sql.addClaimedChunk(exampleChunk2);

            HashMap<String, Boolean> perms1 = new HashMap<>();
            perms1.put("doThis", true);
            perms1.put("doThat", false);
            wrapper.sql.setPermissionFlags(examplePlayer1.player, null, null, perms1);

            HashMap<String, Boolean> perms2 = new HashMap<>();
            perms2.put("dontDoThis", true);
            perms2.put("dontDoThat", false);
            perms2.put("dontAtAll", false);
            wrapper.sql.setPermissionFlags(
                    examplePlayer1.player, null, exampleChunk2.chunk(), perms2);

            HashMap<String, Boolean> perms3 = new HashMap<>();
            perms3.put("alpha", true);
            wrapper.sql.setPermissionFlags(
                    examplePlayer1.player, examplePlayer2.player, exampleChunk2.chunk(), perms3);

            HashMap<String, Boolean> perms4 = new HashMap<>();
            perms4.put("alAsphalt", false);
            wrapper.sql.setPermissionFlags(
                    examplePlayer1.player, examplePlayer2.player, null, perms4);

            List<FullPlayerData> loadedPlayers = wrapper.sql.getAllPlayers();
            Collection<DataChunk> loadedChunks = wrapper.sql.getAllChunks();

            assertEquals(2, loadedPlayers.size());
            assertEquals(2, loadedChunks.size());

            FullPlayerData firstPly =
                    loadedPlayers.stream()
                            .filter(s -> s.player.equals(examplePlayer1.player))
                            .findFirst()
                            .get();
            DataChunk firstPlysChunk =
                    loadedChunks.stream()
                            .filter(s -> s.player().equals(examplePlayer1.player))
                            .findFirst()
                            .get();

            assertEquals(2, firstPly.globalFlags.size());
            assertEquals(false, firstPly.globalFlags.get("doThat"));
            assertEquals(true, firstPly.globalFlags.get("doThis"));
            assertEquals(false, firstPlysChunk.defaultFlags().get("dontDoThat"));
            assertEquals(
                    true, firstPlysChunk.specificFlags().get(examplePlayer2.player).get("alpha"));
            assertEquals(false, firstPly.playerFlags.get(examplePlayer2.player).get("alAsphalt"));
        }
    }

    // TODO:
    /*@Test
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
            DataChunk chunkData = new DataChunk(chunkPos, ply1Uuid);
            chunkData.playerPermissions().put(accessorUuid1, permissions1);
            chunkData.playerPermissions().put(accessorUuid2, permissions2);
            wrapper.sql.addClaimedChunk(chunkData);

            // Make sure both players get loaded
            Collection<FullPlayerData> players = wrapper.sql.getAllPlayers();
            assertEquals(2, players.size());
            assert players.stream()
                    .allMatch(ply -> ply.player.equals(ply1Uuid) || ply.player.equals(ply2Uuid));
            assert players.stream().anyMatch(ply -> "queenshit".equals(ply.chunkName));

            // Load the chunk after adding it
            //noinspection deprecation
            Collection<DataChunk> loadedChunks = SqLiteWrapper.getAllChunks();
            DataChunk loadedChunk = loadedChunks.iterator().next();
            assertNotNull(loadedChunk);

            // Make sure the chunk exists when we load from the database
            assert loadedChunk.player().equals(ply1Uuid) && loadedChunk.chunk().equals(chunkPos);
            // Make sure the chunk permission got loaded correctly
            assertEquals(permissions1, loadedChunk.playerPermissions().get(accessorUuid1));
            assertEquals(permissions2, loadedChunk.playerPermissions().get(accessorUuid2));
        }
    }

    @Test
    void multiplePermissions() {
        try (TestQlWrap wrapper = new TestQlWrap()) {
            UUID owner = UUID.randomUUID();
            UUID accessor1 = UUID.randomUUID();
            UUID accessor2 = UUID.randomUUID();
            ChunkPos chunk = new ChunkPos("world", 824, -29);
            DataChunk chunkData = new DataChunk(chunk, owner);
            chunkData.playerPermissions().put(accessor1, new ChunkPlayerPermissions(0b01));
            chunkData.playerPermissions().put(accessor2, new ChunkPlayerPermissions(0b10));

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
                    SqLiteWrapper.getAllChunks().iterator().next().playerPermissions();
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
            wrapper.sql.addClaimedChunk(new DataChunk(chunk, owner));

            // Insert the permission and check it
            wrapper.sql.setPlayerAccess(chunk, accessor, flags1);
            //noinspection deprecation
            assertEquals(
                    flags1,
                    SqLiteWrapper.getAllChunks()
                            .iterator()
                            .next()
                            .playerPermissions()
                            .get(accessor)
                            .permissionFlags);

            // Update the permission and check it
            wrapper.sql.setPlayerAccess(chunk, accessor, flags2);
            //noinspection deprecation
            assertEquals(
                    flags2,
                    SqLiteWrapper.getAllChunks()
                            .iterator()
                            .next()
                            .playerPermissions()
                            .get(accessor)
                            .permissionFlags);

            // Remove the permission and make sure there aren't any permissions now
            wrapper.sql.removePlayerAccess(chunk, accessor);
            //noinspection deprecation
            assert SqLiteWrapper.getAllChunks().iterator().next().playerPermissions().isEmpty();
        }
    }*/

    protected static File randomDbFile() {
        return new File(UUID.randomUUID() + ".tmp.sqlite3");
    }

    static class TestQlWrap implements AutoCloseable {
        SqLiteWrapper sql;
        File dbFile;

        TestQlWrap() {
            dbFile = randomDbFile();
            sql = new SqLiteWrapper(dbFile, false);
        }

        @Override
        public void close() {
            sql.close();
        }
    }
}
