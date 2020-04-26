package com.cjburkey.claimchunk.rank;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.data.JsonDataStorage;
import java.io.File;
import javax.annotation.Nullable;
import org.bukkit.entity.Player;

public class RankHandler {

    private final JsonDataStorage<Rank> ranks;

    public RankHandler(File file) {
        ranks = new JsonDataStorage<>(Rank[].class, file, true);
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
        int defaultMax = Config.getInt("chunks", "maxChunksClaimed");
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
