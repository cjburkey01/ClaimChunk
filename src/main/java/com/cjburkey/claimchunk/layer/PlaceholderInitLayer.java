package com.cjburkey.claimchunk.layer;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.api.IClaimChunkPlugin;
import com.cjburkey.claimchunk.api.layer.IClaimChunkLayer;
import com.cjburkey.claimchunk.placeholder.ClaimChunkPlaceholders;

import lombok.Getter;

@SuppressWarnings("LombokGetterMayBeUsed")
public class PlaceholderInitLayer implements IClaimChunkLayer {

    @Getter private ClaimChunkPlaceholders placeholders;

    @Override
    public boolean onEnable(IClaimChunkPlugin claimChunk) {
        try {
            // Check if PlaceholderAPI is present
            if (claimChunk.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                // Try to initialize
                placeholders = new ClaimChunkPlaceholders(claimChunk);
                // Finally, register them
                if (placeholders.register()) {
                    Utils.log("Successfully enabled the ClaimChunk PlaceholderAPI expansion!");
                    return true;
                } else {
                    Utils.err("PlaceholderAPI is present but setting up the API failed!");
                }
            } else {
                Utils.log("PlaceholderAPI not found, not loading API.");
            }
        } catch (Exception e) {
            Utils.err(
                    "An error occurred while trying to enable the PlaceholderAPI expansion for"
                            + " claimchunk placeholders!");
            Utils.err("Here is the error for reference:");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onDisable(IClaimChunkPlugin claimChunk) {
        placeholders = null;
    }

    @Override
    public int getOrderId() {
        return 600;
    }
}
