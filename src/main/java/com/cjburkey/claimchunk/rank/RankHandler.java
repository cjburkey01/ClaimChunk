package com.cjburkey.claimchunk.rank;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.data.JsonDataStorage;
import java.io.File;
import java.io.IOException;
import org.bukkit.entity.Player;

public class RankHandler {

    private final JsonDataStorage<Rank> ranks;

    public RankHandler(File file) {
        ranks = new JsonDataStorage<>(Rank[].class, file, true);
    }

    public void readFromDisk() throws IOException {
        ranks.reloadData();
        for (Rank rank : ranks) {
            if (rank.claims < 1) rank.claims = 1;
            rank.getPerm();
        }
        if (!ranks.file.exists()) {
            // Create the example ranks file
            ranks.addData(new Rank("some_random_example_rank", 100));
            ranks.addData(new Rank("another_random_example_rank", 200));
        }
        Utils.debug("Loaded ranks: %s", ranks.toString());
        ranks.saveData();
    }

    public int getMaxClaimsForPlayer(Player player) {
        int maxClaims = -1;
        boolean hadRank = false;
        for (Rank rank : ranks) {
            if (Utils.hasPerm(player, false, rank.getPerm())) {
                if (rank.claims <= 0) return -1;
                maxClaims = Integer.max(maxClaims, rank.claims);
                hadRank = true;
            }
        }
        return hadRank ? maxClaims : Config.getInt("chunks", "maxChunksClaimed");
    }

}
