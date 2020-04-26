package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public final class PrereqClaimData {

    public final ClaimChunk claimChunk;
    public final Chunk chunk;
    public final UUID playerId;
    public final Player player;

    public PrereqClaimData(@Nonnull ClaimChunk claimChunk,
                           @Nonnull Chunk chunk,
                           @Nonnull UUID playerId,
                           @Nullable Player player) {
        this.claimChunk = claimChunk;
        this.chunk = chunk;
        this.playerId = playerId;
        this.player = player;
    }

}
