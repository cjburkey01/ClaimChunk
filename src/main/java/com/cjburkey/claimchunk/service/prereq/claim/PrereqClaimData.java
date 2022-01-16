package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.ClaimChunk;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PrereqClaimData {

    public final ClaimChunk claimChunk;
    public final Chunk chunk;
    public final UUID playerId;
    public final Player player;
    // Automatically loaded
    public final int claimedBefore;
    public final int maxClaimed;
    public final int freeClaims;

    public PrereqClaimData(
            @NotNull ClaimChunk claimChunk,
            @NotNull Chunk chunk,
            @NotNull UUID playerId,
            @Nullable Player player) {
        this.claimChunk = claimChunk;
        this.chunk = chunk;
        this.playerId = playerId;
        this.player = player;

        this.claimedBefore = claimChunk.getChunkHandler().getClaimed(playerId);
        this.maxClaimed = claimChunk.getRankHandler().getMaxClaimsForPlayer(player);
        this.freeClaims = claimChunk.getConfigHandler().getFirstFreeChunks();
    }
}
