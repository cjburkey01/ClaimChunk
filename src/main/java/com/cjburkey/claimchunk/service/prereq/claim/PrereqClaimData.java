package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.service.prereq.IPrereqData;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class PrereqClaimData implements IPrereqData {

    public final ClaimChunk claimChunk;
    public final Chunk chunk;
    public final UUID playerId;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Player> player;

    // Automatically loaded
    public final int claimedBefore;
    public final int maxClaimed;
    public final int freeClaims;

    public PrereqClaimData(ClaimChunk claimChunk, Chunk chunk, UUID playerId, Player player) {
        this.claimChunk = claimChunk;
        this.chunk = chunk;
        this.playerId = playerId;
        this.player = Optional.ofNullable(player);

        this.claimedBefore = claimChunk.getChunkHandler()
                                       .getClaimed(playerId);
        this.maxClaimed = claimChunk.getRankHandler()
                                    .getMaxClaimsForPlayer(player);
        this.freeClaims = Config.getInt("economy", "firstFreeChunks");
    }

}
