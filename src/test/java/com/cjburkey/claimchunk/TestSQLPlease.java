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
            UUID haterPlayer = UUID.randomUUID();
            wrapper.sql.addPlayer(examplePlayer1);
            wrapper.sql.addPlayer(examplePlayer2);

            DataChunk exampleChunk1 =
                    new DataChunk(new ChunkPos("world", 39, -91), examplePlayer1.player);
            DataChunk exampleChunk2 =
                    new DataChunk(new ChunkPos("world_the_nether", -17, 1), examplePlayer2.player);
            wrapper.sql.addClaimedChunk(exampleChunk1);
            wrapper.sql.addClaimedChunk(exampleChunk2);

            {
                HashMap<String, Boolean> p = new HashMap<>();
                p.put("doThis", true);
                p.put("doThat", false);
                p.put("doThatClear", true);
                wrapper.sql.setPermissionFlags(examplePlayer1.player, null, null, p);
            }
            // Try to clear a flag
            {
                wrapper.sql.clearPermissionFlags(examplePlayer1.player, null, null, "doThatClear");
            }
            {
                HashMap<String, Boolean> p = new HashMap<>();
                p.put("dontDoThat", true);
                wrapper.sql.setPermissionFlags(
                        examplePlayer1.player, null, exampleChunk1.chunk(), p);
            }
            {
                HashMap<String, Boolean> p = new HashMap<>();
                p.put("alpha", false);
                wrapper.sql.setPermissionFlags(
                        examplePlayer1.player, examplePlayer2.player, exampleChunk1.chunk(), p);
            }
            {
                HashMap<String, Boolean> p = new HashMap<>();
                p.put("alAsphalt", false);
                wrapper.sql.setPermissionFlags(
                        examplePlayer1.player, examplePlayer2.player, null, p);
            }
            // Try setting an existing flag assignment
            {
                HashMap<String, Boolean> p = new HashMap<>();
                p.put("alAsphalt", true);
                wrapper.sql.setPermissionFlags(
                        examplePlayer1.player, examplePlayer2.player, null, p);
            }
            // Added flags for nonexistent player shouldn't be loaded
            {
                HashMap<String, Boolean> p = new HashMap<>();
                p.put("iShouldntExist", true);
                wrapper.sql.setPermissionFlags(haterPlayer, examplePlayer2.player, null, p);
                wrapper.sql.setPermissionFlags(haterPlayer, null, null, p);
            }

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
            // Cleared flag
            assertFalse(firstPly.globalFlags.containsKey("doThatClear"));
            assertTrue(firstPly.globalFlags.get("doThis"));
            assertTrue(firstPlysChunk.defaultFlags().get("dontDoThat"));
            assertFalse(firstPlysChunk.specificFlags().get(examplePlayer2.player).get("alpha"));
            // Updated flag
            assertTrue(firstPly.playerFlags.get(examplePlayer2.player).get("alAsphalt"));
        }
    }

    protected static File randomDbFile() {
        File dir = new File("tmp");
        var ignored = dir.mkdirs();
        return new File(dir, UUID.randomUUID() + ".tmp.sqlite3");
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
