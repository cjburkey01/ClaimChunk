package com.cjburkey.claimchunk.service.prereq.claim;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NearChunkPrereq implements IClaimPrereq{
    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        System.out.println("Original" + data.chunk.getX());
        System.out.println("Original" + data.chunk.getZ());

        boolean safe = true;

        for(int x1 = -1; x1 < 1; x1++)
        {
            for(int z1 = -1; z1 < 1; z1++)
            {
                safe = !data.claimChunk.getChunkHandler().isClaimed(data.chunk.getWorld().getChunkAt(x1, z1));
            }
        }
        return safe;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of("Too close to a chunk");
    }
}
