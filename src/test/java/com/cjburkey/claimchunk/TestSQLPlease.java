package com.cjburkey.claimchunk;

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
import java.util.Objects;
import java.util.UUID;

class TestSQLPlease {

    @Test
    void ensureNoDataLoss() {
        File dbFile = randomDbFile();
        dbFile.deleteOnExit();

        try (SqLiteWrapper ignoredWrapper = new SqLiteWrapper(dbFile, false)) {
            // Add a random player
            UUID plyUuid = UUID.randomUUID();
            ignoredWrapper.addPlayer(
                    new FullPlayerData(
                            plyUuid, "SomeGuysName", null, System.currentTimeMillis(), true, 0));

            // Make fake accessors and permissions
            UUID accessorUuid1 = UUID.randomUUID();
            UUID accessorUuid2 = UUID.randomUUID();
            ChunkPlayerPermissions permissions1 = new ChunkPlayerPermissions(0b11111111);
            ChunkPlayerPermissions permissions2 = new ChunkPlayerPermissions(0b10101101);

            // Add a chunk to the player and give the permissions to the other players
            ChunkPos chunkPos = new ChunkPos("world", 10, -3);
            DataChunk chunkData = new DataChunk(chunkPos, plyUuid, new HashMap<>(), false);
            chunkData.playerPermissions.put(accessorUuid1, permissions1);
            chunkData.playerPermissions.put(accessorUuid2, permissions2);
            ignoredWrapper.addClaimedChunk(chunkData);

            // Load the chunk after adding it
            Collection<DataChunk> loadedChunks = ignoredWrapper.getAllChunks();
            DataChunk loadedChunk = loadedChunks.iterator().next();
            Objects.requireNonNull(loadedChunk);

            // Make sure the chunk exists when we load from the database
            assert loadedChunk.player.equals(plyUuid) && loadedChunk.chunk.equals(chunkPos);
            // Make sure the chunk permission got loaded correctly
            assert Objects.equals(permissions1, loadedChunk.playerPermissions.get(accessorUuid1));
            assert Objects.equals(permissions2, loadedChunk.playerPermissions.get(accessorUuid2));
        }
    }

    @Test
    void ensureSomeColumnsExistsAfterInitializing() {
        File dbFile = randomDbFile();
        dbFile.deleteOnExit();

        try (SqLiteWrapper ignoredWrapper = new SqLiteWrapper(dbFile, false)) {
            // Make sure that instantiating SqLiteWrapper created the tables
            assert SqLiteTableMigrationManager.columnExists("player_data", "player_uuid");
            assert SqLiteTableMigrationManager.columnExists("chunk_data", "owner_uuid");
            assert SqLiteTableMigrationManager.columnExists("chunk_permissions", "permission_bits");
            assert !SqLiteTableMigrationManager.columnExists("chunk_hell", "permission_bits");
            assert !SqLiteTableMigrationManager.columnExists("player_data", "fake_col");
        }
    }

    protected static File randomDbFile() {
        return new File(UUID.randomUUID() + ".tmp.sqlite3");
    }
}
