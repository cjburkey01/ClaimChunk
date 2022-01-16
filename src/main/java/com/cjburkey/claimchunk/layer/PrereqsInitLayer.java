package com.cjburkey.claimchunk.layer;

import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.api.IClaimChunkPlugin;
import com.cjburkey.claimchunk.api.layer.IClaimChunkLayer;
import com.cjburkey.claimchunk.service.prereq.PrereqChecker;
import com.cjburkey.claimchunk.service.prereq.claim.*;

import lombok.Getter;

public class PrereqsInitLayer implements IClaimChunkLayer {

    // The pre-req checker responsible for chunk claiming.
    // We reuse this instance (clearing and re-adding on disable and re-enable, respectively).
    @Getter
    private final PrereqChecker<IClaimPrereq, PrereqClaimData> claimPrereqChecker =
            new PrereqChecker<>();

    // Initialize each of the prereq checkers (based on config values when necessary)
    @Override
    public boolean onEnable(IClaimChunkPlugin claimChunk) {
        // Add default chunk claiming prerequisites

        // Check permissions
        claimPrereqChecker.prereqs.add(new PermissionPrereq());
        // Check that the world is enabled
        claimPrereqChecker.prereqs.add(new WorldPrereq());
        // Check if the chunk is already claimed
        claimPrereqChecker.prereqs.add(new UnclaimedPrereq());
        // Check if players can claim chunks here/in this world
        claimPrereqChecker.prereqs.add(new WorldGuardPrereq());
        // Check if the player has room for more chunk claims
        claimPrereqChecker.prereqs.add(new MaxChunksPrereq());
        // Check if the player is near someone else's claim
        claimPrereqChecker.prereqs.add(new NearChunkPrereq());

        // Enable this layer
        return true;
    }

    // Clear the prereqs when the plugin is disabled
    @Override
    public void onDisable(IClaimChunkPlugin claimChunk) {
        Utils.debug("Clearing prerequisites");
        claimPrereqChecker.prereqs.clear();
    }

    @Override
    public int getOrderId() {
        return 500;
    }
}
