package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NearChunkPrereq implements IClaimPrereq{
    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        boolean nearClaimed = false;

        for(int x1 = -1; x1 < 2; x1++) {
            for(int z1 = -1; z1 < 2; z1++) {
                if(nearClaimed) break;

                Chunk chunk = data.chunk.getWorld().getChunkAt(x1 + data.chunk.getX(), z1 + data.chunk.getZ());
                System.out.println(data.claimChunk.getChunkHandler().isOwner(chunk, data.player));

                if(data.claimChunk.getChunkHandler().isOwner(chunk, data.player)) continue;
                nearClaimed = data.claimChunk.getChunkHandler().isClaimed(data.chunk.getWorld().getChunkAt(x1 + data.chunk.getX(), z1 + data.chunk.getZ()));
            }
        }

        return !nearClaimed;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of("Too close to a chunk");
    }
}
