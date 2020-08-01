package com.cjburkey.claimchunk.rank;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.config.JsonConfig;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;

public class RankHandler {

    private final JsonConfig<Rank> ranks;
    private final ClaimChunk claimChunk;

    public RankHandler(File file, ClaimChunk claimChunk) {
        ranks = new JsonConfig<>(Rank[].class, file, true);
        this.claimChunk = claimChunk;
    }

    public void readFromDisk() {
        try {
            ranks.reloadData();
        } catch (Exception e) {
            Utils.err("There was an error reading rank data!");
            Utils.err("This means ranks WILL NOT WORK!");
            Utils.err("Error: \"%s\"", e.getMessage());
        }
        for (Rank rank : ranks) {
            if (rank.claims < 1) rank.claims = 1;
            rank.getPerm();
        }
        if (!ranks.file.exists()) {
            // Create the example ranks file
            ranks.addData(new Rank("some_random_example_rank", 100));
            ranks.addData(new Rank("another_random_example_rank", 200));
        }
        try {
            ranks.saveData();
        } catch (Exception e) {
            Utils.err("Failed to save rank data!");
            Utils.err("This means ranks WILL BE DELETED!!!");
            Utils.err("Error:");
            e.printStackTrace();
            Utils.err("Current rank print: \"\"", ranks.toString());
        }
    }

    public int getMaxClaimsForPlayer(@Nullable Player player) {
        int defaultMax = claimChunk.chConfig().getInt("chunks", "maxChunksClaimed");
        if (player == null) {
            return defaultMax;
        }

        int maxClaims = -1;
        boolean hadRank = false;
        for (Rank rank : ranks) {
            if (Utils.hasPerm(player, false, rank.getPerm())) {
                if (rank.claims <= 0) return -1;
                maxClaims = Integer.max(maxClaims, rank.claims);
                hadRank = true;
            }
        }
        return hadRank ? maxClaims : defaultMax;
    }

}
