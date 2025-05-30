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
     * @return A reference to the class that allows access to claimed chunk information.
     */
    IChunkApi getChunkApi();

    /**
     * @return A reference to the class that allows access to chunk owner information.
     */
    IOwnerApi getOwnerApi();

    /**
     * @return A reference to the class that allows access to the flag permission system
     *     information.
     */
    IFlagApi getFlagApi();
}
