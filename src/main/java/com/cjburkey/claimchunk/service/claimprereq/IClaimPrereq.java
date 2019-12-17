package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.Comparator;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public interface IClaimPrereq {

    int getWeight();

    boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location);

    default Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.empty();
    }

    default Optional<String> getSuccessMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        return Optional.empty();
    }

    default void onClaimSuccess(ClaimChunk claimChunk, Player player, Chunk location) {
    }

    final class ClaimPrereqComparator implements Comparator<IClaimPrereq> {

        public int compare(IClaimPrereq o1, IClaimPrereq o2) {
            return Integer.compare(o1.getWeight(), o2.getWeight());
        }

    }

}
