package com.cjburkey.claimchunk.service.prereq.claim;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NearChunkPrereq implements IClaimPrereq {
    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        boolean nearClaimed = false;

        int near = data.claimChunk.chConfig().getNearChunkSearch();

        if (near < 1) return true;

        int min = (near - 1) / 2;
        int max = (near - 1) / 2 + 1;

        for (int x1 = -min; x1 < max; x1++) {
            for (int z1 = -min; z1 < max; z1++) {
                if (nearClaimed) break;

                Chunk chunk =
                        data.chunk
                                .getWorld()
                                .getChunkAt(x1 + data.chunk.getX(), z1 + data.chunk.getZ());

                if (data.claimChunk.getChunkHandler().isOwner(chunk, data.player)) continue;
                nearClaimed = data.claimChunk.getChunkHandler().isClaimed(chunk);
            }
        }

        return !nearClaimed;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().nearChunkSearch);
    }
}
