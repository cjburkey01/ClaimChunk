package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class PermissionPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return -100;
    }

    @Override
    public boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location) {
        return Utils.hasPerm(player, true, "claim");
    }

    @Override
    public Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.of(ClaimChunk.getInstance().getMessages().claimNoPerm);
    }

}
