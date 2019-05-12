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
        if (!ranks.file.exists()) {
            // Create the example ranks file
            ranks.addData(new Rank("someRandomExampleRank", 100));
            ranks.addData(new Rank("anotherRandomExampleRank", 200));
            ranks.saveData();
            Utils.debug("Created example ranks file");
        }
        for (Rank rank : ranks) {
            if (rank.claims < 1) rank.claims = 1;
        }
        Utils.debug("Ranks: %s", ranks.getData().toString());
    }

    public int getMaxClaimsForPlayer(Player player) {
        int maxClaims = -1;
        for (Rank rank : ranks) {
            if (Utils.hasPerm(player, false, rank.permName)) maxClaims = Integer.max(maxClaims, rank.claims);
        }
        int claims = ((maxClaims <= 0) ? Config.getInt("chunks", "maxChunksClaimed") : maxClaims);
        Utils.debug("User %s can claim %s chunks", player.getDisplayName(), claims);
        return claims;
    }

}
