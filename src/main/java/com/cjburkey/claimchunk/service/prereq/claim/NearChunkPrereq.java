package com.cjburkey.claimchunk.service.prereq.claim;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class NearChunkPrereq implements IClaimPrereq {
    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getPassed(@NotNull PrereqClaimData data) {
        // Get the diameter around the player to check
        int near = data.claimChunk.chConfig().getNearChunkSearch();
        if (near < 1) return true;

        // Get starting and ending bounds
        int min = (near - 1) / 2;
        int max = (near - 1) / 2 + 1;

        // Check through chunks within the given area
        for (int x1 = -min; x1 < max; x1++) {
            for (int z1 = -min; z1 < max; z1++) {
                if (nearClaimed || data.player.hasPermission("claimchunk.bypassnearbychunk")) break;

                Chunk chunk =
                        data.chunk
                                .getWorld()
                                .getChunkAt(x1 + data.chunk.getX(), z1 + data.chunk.getZ());

                if (data.claimChunk.getChunkHandler().isOwner(chunk, data.player)) continue;
                nearClaimed = data.claimChunk.getChunkHandler().isClaimed(chunk);
                UUID owner = data.claimChunk.getChunkHandler().getOwner(data.chunk);
                // If the given chunk is owned but not by this player, fail this prereq
                if (owner != null && owner.equals(data.playerId)) return false;
            }
        }

        // Otherwise, pass
        return true;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk.getMessages().nearChunkSearch);
    }
}
