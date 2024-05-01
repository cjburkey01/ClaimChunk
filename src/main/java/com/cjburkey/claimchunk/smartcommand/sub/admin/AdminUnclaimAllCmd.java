package com.cjburkey.claimchunk.smartcommand.sub.admin;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminUnclaimAllCmd extends CCSubCommand {

    public AdminUnclaimAllCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, false, "admin");
    }

    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdAdminUnclaimAll;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg(claimChunk.getMessages().argPlayer, CCAutoComplete.OFFLINE_PLAYER),
            new CCArg(claimChunk.getMessages().argAcrossAllWorlds, CCAutoComplete.NONE),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var allWorlds = false;
        if (args.length == 2) {
            var allWorldsOpt = claimChunk.getMessages().parseBool(args[1]);
            // Failed to parse the provided boolean value
            if (allWorldsOpt.isEmpty()) {
                return false;
            }
            allWorlds = allWorldsOpt.get();
        }
        var chunkHandler = claimChunk.getChunkHandler();

        var ply = claimChunk.getPlayerHandler().getUUID(args[0]);
        if (ply != null) {
            var claimedChunks = chunkHandler.getClaimedChunks(ply);
            int unclaimed = 0;
            for (var chunk : claimedChunks) {
                if (allWorlds || player.getWorld().getName().equals(chunk.getWorld())) {
                    chunkHandler.unclaimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
                    unclaimed++;
                }
            }
            messagePly(
                    player,
                    claimChunk.getMessages().adminUnclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        } else {
            messagePly(player, claimChunk.getMessages().noPlayer);
        }

        return true;
    }
}
