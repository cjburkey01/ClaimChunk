package com.cjburkey.claimchunk.service.claimprereq;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import java.util.Optional;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class EconPrereq implements IClaimPrereq {

    @Override
    public int getWeight() {
        return 300;
    }

    @Override
    public boolean getCanClaim(ClaimChunk claimChunk, Player player, Chunk location) {
        if (claimChunk.useEconomy() && claimChunk.getChunkHandler().getHasAllFreeChunks(player.getUniqueId())) {
            double cost = Config.getDouble("economy", "claimPrice");

            // Check if the chunk is free or the player has enough money
            return cost <= 0 || claimChunk.getEconomy().getMoney(player.getUniqueId()) >= cost;
        }
        return true;
    }

    @Override
    public Optional<String> getErrorMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        // `getCanClaim` will only ever fail if the economy is enabled, the
        // player has used all of their free chunks, the cost of claiming a
        // chunk is larger than $0.00, and the player cannot afford the cost of
        // the chunk claiming. Therefore, this is the only error that is 
        // possible
        return Optional.of(claimChunk.getMessages().claimNotEnoughMoney);
    }

    @Override
    public Optional<String> getSuccessMessage(ClaimChunk claimChunk, Player player, Chunk location) {
        // Check if the economy isn't being used, in which case it's not the
        // responsbility of this prereq to set the success message
        if (!claimChunk.useEconomy()) {
            return Optional.empty();
        }

        // Check if the player should get this chunk for free because they
        // haven't claimed all of their free chunks yet
        if (!claimChunk.getChunkHandler().getHasAllFreeChunks(player.getUniqueId())) {
            // If the chunk is free, determine the message to display based on how many chunks are free
            int freeCount = Config.getInt("economy", "firstFreeChunks");
            if (freeCount <= 1) {
                // Only one free chunk (or error?)
                // We shouldn't get this far if players can't claim free chunks
                return Optional.of(claimChunk.getMessages().claimFree1);
            }

            // Multiple free chunks
            return Optional.of(claimChunk.getMessages().claimFrees.replace("%%COUNT%%", freeCount + ""));
        } else {
            double cost = Config.getDouble("economy", "claimPrice");

            // The success message includes the price
            // If the price is less than or 0 (free), then it should display
            // that it's free
            return Optional.of(claimChunk.getMessages().claimSuccess
                    .replace("%%PRICE%%",
                            (cost <= 0.0d)
                                    ? claimChunk.getMessages().claimNoCost
                                    : claimChunk.getEconomy().format(cost)
                    ));
        }
    }

    // Take money from the player after we're sure that the chunk claiming was
    // successful. We don't want to take money if the claiming fails
    // (obviously)
    @Override
    public void onClaimSuccess(ClaimChunk claimChunk, Player player, Chunk location) {
        if (claimChunk.useEconomy()) {
            double cost = Config.getDouble("economy", "claimPrice");

            if (!claimChunk.getEconomy().buy(player.getUniqueId(), cost)) {
                // Error check
                Utils.err("Failed to buy chunk (%s, %s) in world %s for player %s",
                        location.getX(),
                        location.getZ(),
                        location.getWorld().getName(),
                        player.getName());
            }
        }
    }

}
