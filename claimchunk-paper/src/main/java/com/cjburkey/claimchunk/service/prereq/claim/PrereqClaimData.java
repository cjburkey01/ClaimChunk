package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkPos;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PrereqClaimData(
        ClaimChunk claimChunk,
        ChunkPos chunk,
        UUID playerId,
        Player player,
        int claimedBefore,
        int maxClaimed,
        int freeClaims) {

    public PrereqClaimData(
            @NotNull ClaimChunk claimChunk,
            @NotNull ChunkPos chunk,
            @NotNull UUID playerId,
            @Nullable Player player) {
        this(
                claimChunk,
                chunk,
                playerId,
                player,
                claimChunk.getChunkHandler().getClaimed(playerId),
                claimChunk.getRankHandler().getMaxClaimsForPlayer(player),
                claimChunk.getConfigHandler().getFirstFreeChunks());
    }
}
