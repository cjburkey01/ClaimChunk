package com.cjburkey.claimchunk.data.conversion;

import com.cjburkey.claimchunk.chunk.DataChunk;
import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;
import com.cjburkey.claimchunk.player.FullPlayerData;
import java.util.Collection;

public interface IDataConverter<From extends IClaimChunkDataHandler, To extends IClaimChunkDataHandler> {

    /**
     * Converts one kind of data handler into the other kind
     *
     * @param oldDataHandler The old data handler
     * @return A new data handler containing the old data handler's data
     * @throws Exception Any error that may occur during any phase of data conversion
     */
    To convert(From oldDataHandler) throws Exception;

    static <A extends IClaimChunkDataHandler, B extends IClaimChunkDataHandler> void copyConvert(A oldDataHandler, B newDataHandler) throws Exception {
        // Initialize the new data handler
        newDataHandler.init();

        // Copy the chunks from the old data handler to the new data handler
        DataChunk[] chunks = oldDataHandler.getClaimedChunks();
        for (DataChunk chunk : chunks) {
            newDataHandler.addClaimedChunk(chunk.chunk, chunk.player);
        }

        // Copy the player data from the old data handler to the new data handler
        Collection<FullPlayerData> players = oldDataHandler.getFullPlayerData();
        for (FullPlayerData player : players) {
            newDataHandler.addPlayer(player);
        }
    }

}
