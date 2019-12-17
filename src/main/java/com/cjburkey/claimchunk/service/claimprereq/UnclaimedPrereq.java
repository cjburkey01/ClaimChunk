package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class UnclaimedPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location) {
        return !claimChunk.getChunkHandler().isClaimed(location);
    }

    @Override
    public Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.of(claimChunk.getMessages().claimAlreadyOwned);
    }

}
