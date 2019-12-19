package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.service.prereq.IPrereqData;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public final class PrereqClaimData implements IPrereqData {

    public final ClaimChunk claimChunk;
    public final Chunk chunk;
    public final UUID playerId;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Player> player;

    public PrereqClaimData(ClaimChunk claimChunk,
                           Chunk chunk,
                           UUID playerId,
                           Player player) {
        this.claimChunk = claimChunk;
        this.chunk = chunk;
        this.playerId = playerId;
        this.player = Optional.ofNullable(player);
    }

}
