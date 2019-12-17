package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class MaxChunksPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 200;
    }

    @Override
    public boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location) {
        int max = claimChunk.getRankHandler().getMaxClaimsForPlayer(player);
        return !(max > 0 && claimChunk.getChunkHandler().getClaimed(player.getUniqueId()) >= max);
    }

    @Override
    public Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.of(claimChunk.getMessages().claimTooMany);
    }

}
