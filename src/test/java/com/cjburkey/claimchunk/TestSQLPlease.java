package com.cjburkey.claimchunk;

import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.sqlite.SqLiteTableMigrationManager;
import com.cjburkey.claimchunk.data.sqlite.SqLiteWrapper;
import com.cjburkey.claimchunk.player.FullPlayerData;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

class TestSQLPlease {

    @Test
    void ensureSomeColumnsExistsAfterInitializing() {
        File dbFile = randomDbFile();

        try (SqLiteWrapper ignoredWrapper = new SqLiteWrapper(dbFile, false)) {
            // Make sure that instantiating SqLiteWrapper created the tables
            assert SqLiteTableMigrationManager.columnExists("player_data", "player_uuid");
            assert SqLiteTableMigrationManager.columnExists("chunk_data", "owner_uuid");
            assert SqLiteTableMigrationManager.columnExists("chunk_permissions", "permission_bits");
            assert !SqLiteTableMigrationManager.columnExists("chunk_hell", "permission_bits");
            assert !SqLiteTableMigrationManager.columnExists("player_data", "fake_col");

            // Add a random player
            UUID plyUuid = UUID.randomUUID();
            ignoredWrapper.addPlayer(
                    new FullPlayerData(
                            plyUuid, "SomeGuysName", null, System.currentTimeMillis(), true, 0));

            // Add a chunk to the player
            ChunkPos chunkPos = new ChunkPos("world", 10, -3);
            ignoredWrapper.addClaimedChunk(
                    new DataChunk(chunkPos, plyUuid, new HashMap<>(), false));

            // Make sure the chunk exists when we load from the database
            assert ignoredWrapper.getAllChunks().stream()
                    .anyMatch(
                            chunk -> chunk.player.equals(plyUuid) && chunk.chunk.equals(chunkPos));
        }

        //noinspection ResultOfMethodCallIgnored
        dbFile.delete();
    }

    protected static File randomDbFile() {
        return new File(UUID.randomUUID() + ".tmp.sqlite3");
    }
}
