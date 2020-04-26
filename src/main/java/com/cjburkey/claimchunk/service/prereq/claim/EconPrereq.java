package com.cjburkey.claimchunk.service.prereq.claim;

import com.cjburkey.claimchunk.Utils;
import java.util.Optional;
import javax.annotation.Nonnull;

public class EconPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getPassed(@Nonnull PrereqClaimData data) {
        if (data.claimChunk.useEconomy() && data.claimChunk.getChunkHandler().getHasAllFreeChunks(data.playerId)) {
            double cost = data.claimChunk.chConfig().getDouble("economy", "claimPrice");

            // Check if the chunk is free or the player has enough money
            return cost <= 0 || data.claimChunk.getEconomy().getMoney(data.playerId) >= cost;
        }
        return true;
    }

    @Override
    public Optional<String> getErrorMessage(@Nonnull PrereqClaimData data) {
        // `getCanClaim` will only ever fail if the economy is enabled, the
        // player has used all of their free chunks, the cost of claiming a
        // chunk is larger than $0.00, and the player cannot afford the cost of
        // the chunk claiming. Therefore, this is the only error that is 
        // possible
        return Optional.of(data.claimChunk.getMessages().claimNotEnoughMoney);
    }

    @Override
    public Optional<String> getSuccessMessage(@Nonnull PrereqClaimData data) {
        // Check if the economy isn't being used, in which case it's not the
        // responsbility of this prereq to set the success message
        if (!data.claimChunk.useEconomy()) {
            return Optional.empty();
        }

        // Check if the player should get this chunk for free because they
        // haven't claimed all of their free chunks yet
        if (!data.claimChunk.getChunkHandler().getHasAllFreeChunks(data.playerId)) {
            // If the chunk is free, determine the message to display based on how many chunks are free
            int freeCount = data.claimChunk.chConfig().getInt("economy", "firstFreeChunks");
            if (freeCount <= 1) {
                // Only one free chunk (or error?)
                // We shouldn't get this far if players can't claim free chunks
                return Optional.of(data.claimChunk.getMessages().claimFree1);
            }

            // Multiple free chunks
            return Optional.of(data.claimChunk.getMessages().claimFrees.replace("%%COUNT%%", freeCount + ""));
        } else {
            double cost = data.claimChunk.chConfig().getDouble("economy", "claimPrice");

            // The success message includes the price
            // If the price is less than or 0 (free), then it should display
            // that it's free
            return Optional.of(data.claimChunk.getMessages().claimSuccess
                    .replace("%%PRICE%%",
                            (cost <= 0.0d)
                                    ? data.claimChunk.getMessages().claimNoCost
                                    : data.claimChunk.getEconomy().format(cost)
                    ));
        }
    }

    // Take money from the player after we're sure that the chunk claiming was
    // successful. We don't want to take money if the claiming fails
    // (obviously)
    @Override
    public void onSuccess(@Nonnull PrereqClaimData data) {
        if (data.claimChunk.useEconomy()) {
            double cost = data.claimChunk.chConfig().getDouble("economy", "claimPrice");

            if (!data.claimChunk.getEconomy().buy(data.playerId, cost)) {
                // Error check
                Utils.err("Failed to buy chunk (%s, %s) in world %s for player %s",
                        data.chunk.getX(),
                        data.chunk.getZ(),
                        data.chunk.getWorld().getName(),
                        data.player != null ? data.player.getName() : data.playerId);
            }
        }
    }

}
