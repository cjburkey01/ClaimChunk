package com.cjburkey.claimchunk.api.layer;

import com.cjburkey.claimchunk.api.IClaimChunkPlugin;

/**
 * ClaimChunk will be separated into different layers responsible for different things, this represents a single one.
 */
public interface IClaimChunkLayer {

    /**
     * Called when ClaimChunk is enabled; this is a good place to register event handlers.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @return Whether this layer was successfully enabled.
     */
    boolean onEnable(IClaimChunkPlugin claimChunk);

    /**
     * Called when ClaimChunk is disabled.
     *
     * @param claimChunk The instance of ClaimChunk.
     */
    void onDisable(IClaimChunkPlugin claimChunk);

    /**
     * Returns an ordering ID that indicates when this layer should execute. Lower values are executed first. Changes to this value will be ignored after insertion.
     *
     * @return The ordering ID to compare to other layers (lower = sooner).
     */
    int getOrderId();

}
