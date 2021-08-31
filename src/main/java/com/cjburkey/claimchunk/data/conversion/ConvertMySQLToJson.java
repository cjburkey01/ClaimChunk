package com.cjburkey.claimchunk.data.conversion;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.data.newdata.JsonDataHandler;
import com.cjburkey.claimchunk.data.newdata.MySQLDataHandler;

import java.io.File;

@SuppressWarnings("unused")
public class ConvertMySQLToJson implements IDataConverter<MySQLDataHandler<?>, JsonDataHandler> {

    private final ClaimChunk claimChunk;
    private final File claimedChunksFile;
    private final File joinedPlayersFile;

    public ConvertMySQLToJson(
            ClaimChunk claimChunk, File claimedChunksFile, File joinedPlayersFile) {
        this.claimChunk = claimChunk;
        this.claimedChunksFile = claimedChunksFile;
        this.joinedPlayersFile = joinedPlayersFile;
    }

    @Override
    public JsonDataHandler convert(MySQLDataHandler<?> oldDataHandler) throws Exception {
        // Create and a new MySQL data handler
        JsonDataHandler newDataHandler =
                new JsonDataHandler(claimChunk, claimedChunksFile, joinedPlayersFile);

        // Initialize the new data handler
        newDataHandler.init();

        // Convert from the old data handler to the new one
        IDataConverter.copyConvert(oldDataHandler, newDataHandler);

        // Return the new data handler
        return newDataHandler;
    }
}
