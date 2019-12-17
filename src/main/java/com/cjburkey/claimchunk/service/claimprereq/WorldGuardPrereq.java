package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.worldguard.WorldGuardHandler;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class WorldGuardPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 100;
    }

    @Override
    public boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location) {
        boolean allowedToClaimWG = WorldGuardHandler.isAllowedClaim(location);
        boolean worldAllowsClaims = !Config.getList("chunks", "disabledWorlds").contains(location.getWorld().getName());
        boolean adminOverride = Config.getBool("worldguard", "allowAdminOverride");
        boolean hasAdmin = Utils.hasAdmin(player);

        //return (worldAllowsClaims || (hasAdmin && adminOverride)) && (allowedToClaimWG || (hasAdmin && adminOverride));
        return (worldAllowsClaims || allowedToClaimWG) && (hasAdmin && adminOverride);
    }

    @Override
    public Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.of(claimChunk.getMessages().claimLocationBlock);
    }

}
