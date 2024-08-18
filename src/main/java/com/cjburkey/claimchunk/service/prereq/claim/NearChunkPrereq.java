package com.cjburkey.claimchunk.service.prereq.claim;

import org.bukkit.Chunk;
import org.bukkit.World;
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
        boolean nearClaimed = false;

        // Get the diameter around the player to check
        int near = data.claimChunk().getConfigHandler().getNearChunkSearch();
        if (near < 1) return true;

        // Get starting and ending bounds
        int min = (near - 1) / 2;
        int max = (near - 1) / 2 + 1;

        World world = data.claimChunk().getServer().getWorld(data.chunk().world());
        if (world == null) {
            return false;
        }

        // Check through chunks within the given area
        for (int x1 = -min; x1 < max; x1++) {
            for (int z1 = -min; z1 < max; z1++) {
                if (nearClaimed || data.player().hasPermission("claimchunk.bypassnearbychunk"))
                    break;

                Chunk chunk = world.getChunkAt(x1 + data.chunk().x(), z1 + data.chunk().z());

                if (data.claimChunk().getChunkHandler().isOwner(chunk, data.player())) continue;
                nearClaimed = data.claimChunk().getChunkHandler().isClaimed(chunk);
                UUID owner = data.claimChunk().getChunkHandler().getOwner(data.chunk());
                // If the given chunk is owned but not by this player, fail this prereq
                if (owner != null && owner.equals(data.playerId())) return false;
            }
        }

        // Otherwise, pass
        return true;
    }

    @Override
    public Optional<String> getErrorMessage(@NotNull PrereqClaimData data) {
        return Optional.of(data.claimChunk().getMessages().nearChunkSearch);
    }
}
