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
        ranks = new JsonDataStorage<>(Rank[].class, file);
    }

    public void readFromDisk() throws IOException {
        ranks.reloadData();
        if (!ranks.file.exists()) ranks.saveData();     // Create the empty JSON ranks file
    }

    public int getMaxClaimsForPlayer(Player player) {
        for (Rank rank : ranks) {
            if (Utils.hasPerm(player, false, rank.permName)) return rank.claims;
        }
        return Config.getInt("chunks", "maxChunksClaimed");
    }

}
