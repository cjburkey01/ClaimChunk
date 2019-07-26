package com.cjburkey.claimchunk.data.conversion;

import com.cjburkey.claimchunk.data.newdata.JsonDataHandler;
import com.cjburkey.claimchunk.data.newdata.MySQLDataHandler;
import java.io.File;

@SuppressWarnings("unused")
public class ConvertMySQLToJson implements IDataConverter<MySQLDataHandler<?>, JsonDataHandler> {

    private final File claimedChunksFile;
    private final File joinedPlayersFile;

    public ConvertMySQLToJson(File claimedChunksFile, File joinedPlayersFile) {
        this.claimedChunksFile = claimedChunksFile;
        this.joinedPlayersFile = joinedPlayersFile;
    }

    @Override
    public JsonDataHandler convert(MySQLDataHandler<?> oldDataHandler) throws Exception {
        // Create and a new MySQL data handler
        JsonDataHandler newDataHandler = new JsonDataHandler(claimedChunksFile, joinedPlayersFile);

        // Initialize the new data handler
        newDataHandler.init();

        // Convert from the old data handler to the new one
        IDataConverter.copyConvert(oldDataHandler, newDataHandler);

        // Return the new data handler
        return newDataHandler;
    }

}
