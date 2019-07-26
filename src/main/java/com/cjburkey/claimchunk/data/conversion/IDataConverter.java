package com.cjburkey.claimchunk.data.conversion;

import com.cjburkey.claimchunk.data.newdata.IClaimChunkDataHandler;

public interface IDataConverter<From extends IClaimChunkDataHandler, To extends IClaimChunkDataHandler> {

    /**
     * Converts one kind of data handler into the other kind.
     *
     * @param oldDataHandler The old data handler
     * @return A new data handler containing the old data handler's data
     * @throws Exception Any error that may occur during any phase of data conversion
     */
    To convert(From oldDataHandler) throws Exception;

    /**
     * Copies the data from the provided old data handler into the provided new data handler.
     * This does not update the old data handler.
     *
     * @param oldDataHandler The old handler
     * @param newDataHandler The new handler
     * @param <A>            The type of the old data handler
     * @param <B>            The type of the new data handler
     */
    static <A extends IClaimChunkDataHandler, B extends IClaimChunkDataHandler> void copyConvert(A oldDataHandler, B newDataHandler) throws Exception {
        if (!oldDataHandler.getHasInit()) oldDataHandler.init();
        if (!newDataHandler.getHasInit()) newDataHandler.init();

        // Load the old data
        oldDataHandler.load();

        // Copy the chunks from the old data handler to the new data handler
        newDataHandler.addClaimedChunks(oldDataHandler.getClaimedChunks());

        // Copy the player data from the old data handler to the new data handler
        newDataHandler.addPlayers(oldDataHandler.getFullPlayerData());

        // Save the new data
        newDataHandler.save();
    }

}
