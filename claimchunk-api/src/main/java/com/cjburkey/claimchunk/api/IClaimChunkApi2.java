package com.cjburkey.claimchunk.api;

/**
 * Modernized API for ClaimChunk.
 *
 * <p>This is the "second" API, but the first that is separate and more nicely organized.
 *
 * @since 1.0.0
 */
public interface IClaimChunkApi2 {

    /**
     * @return An instance of a class that allows access to claimed chunk information.
     */
    IChunkApi getChunkApi();

    /**
     * @return An instance of a class that allows access to chunk owner information.
     */
    IOwnerApi getOwnerApi();

    /**
     * @return An instance of a class that allows access to the flag permission system information.
     */
    IFlagApi getFlagApi();

    /**
     * @return A reference to a class that allows access to the API responsible for messaging
     *     players via the chat and titles.
     */
    IMessageApi getMessageApi();

    /**
     * @return A reference to a class that gives access to general utilities like logging and such.
     */
    ICCUtils utils();
}
