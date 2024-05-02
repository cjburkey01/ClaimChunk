package com.cjburkey.claimchunk.rank;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.data.JsonConfig;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// TODO: REPLACE WITH PER-PLAYER AMOUNT OF CHUNKS!
//       IT'S JUST BETTER ALL AROUND!
public class RankHandler {

    // Info!
    private static final String RANK_FILE_HELP =
            """
            This file lists all of the ranks for claimchunk.
            For more information, see this wiki page:
            https://github.com/cjburkey01/ClaimChunk/wiki/Ranks-System""";

    private final JsonConfig<Rank> ranks;
    private final ClaimChunk claimChunk;

    public RankHandler(File file, File oldLocation, ClaimChunk claimChunk) {
        ranks = new JsonConfig<>(Rank[].class, file, true);
        this.claimChunk = claimChunk;

        // Migration check
        if (!file.exists() && oldLocation.exists()) {
            try {
                // Copy the old file to the new file location because it needs
                // to be migrated from pre-0.0.23 to 0.0.23 (the version I'm
                // writing right this second! wow!)
                Files.copy(
                        oldLocation.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Utils.err(
                        "Failed to migrate pre-0.0.23 \"ranks.json\" file at \"%s\" to \"%s\"",
                        oldLocation.getAbsolutePath(), file.getAbsolutePath());
                Utils.err("Complete stacktrace:");
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
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
            // Save with the info header comment
            ranks.saveData(RANK_FILE_HELP);
        } catch (Exception e) {
            Utils.err("Failed to save rank data!");
            Utils.err("This means ranks WILL BE DELETED!!!");
            Utils.err("Error:");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Utils.err("Current rank print: \"\"", ranks.toString());
        }
    }

    public int getMaxClaimsForPlayer(@Nullable Player player) {
        int defaultMax = claimChunk.getConfigHandler().getDefaultMaxChunksClaimed();
        if (defaultMax <= 0) {
            defaultMax = Integer.MAX_VALUE;
        }
        if (player == null) {
            return defaultMax;
        }

        int maxClaims = -1;
        boolean hadRank = false;
        for (Rank rank : ranks) {
            // Don't use Utils.hasPerm because it returns true for admins!
            if (player.hasPermission(rank.getPerm())) {
                if (rank.claims <= 0) return -1;
                maxClaims = Integer.max(maxClaims, rank.claims);
                hadRank = true;
            }
        }

        // All that work, and this is the load-bearing line :)
        int preExtra = hadRank ? maxClaims : defaultMax;
        return preExtra + claimChunk.getPlayerHandler().getMaxClaims(player.getUniqueId());
    }
}
