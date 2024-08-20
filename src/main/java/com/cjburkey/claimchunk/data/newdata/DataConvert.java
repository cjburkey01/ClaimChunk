package com.cjburkey.claimchunk.data.newdata;

/**
 * Represents a class that may act as a converter between two different data systems.
 *
 * @since 0.0.13
 */
public class DataConvert {

    private DataConvert() {}

    /**
     * Copies the data from the provided old data handler into the provided new data handler. This
     * does not update the old data handler.
     *
     * @param oldDataHandler The old handler, may or may not be initialized
     * @param newDataHandler The new handler, may or may not be initialized
     * @since 0.0.13
     */
    public static void copyConvert(
            IClaimChunkDataHandler oldDataHandler, IClaimChunkDataHandler newDataHandler)
            throws Exception {
        // Initialize the old data handler if it hasn't been initialized yet
        if (!oldDataHandler.getHasInit()) oldDataHandler.init();

        // Load the old data
        oldDataHandler.load();

        // Initialize the new data handler if it hasn't been initialized yet
        if (!newDataHandler.getHasInit()) newDataHandler.init();

        // Copy the player data from the old data handler to the new data handler.
        // Make sure we do this before players! The SQLite data handler will make dummy players if
        // there aren't proper players in the player data table already.
        newDataHandler.addPlayers(oldDataHandler.getFullPlayerData());

        // Copy the chunks from the old data handler to the new data handler
        newDataHandler.addClaimedChunks(oldDataHandler.getClaimedChunks());
    }
}
