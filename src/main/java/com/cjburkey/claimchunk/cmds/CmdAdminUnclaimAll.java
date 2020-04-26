package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdAdminUnclaimAll implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "adminunclaimall";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdAdminUnclaimAll;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().unclaimNoPermAdmin;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[]{
                new Argument("player", Argument.TabCompletion.OFFLINE_PLAYER),
                new Argument("acrossAllWorlds", Argument.TabCompletion.NONE),
        };
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 1;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        boolean allWorlds = (args.length == 2 && Boolean.parseBoolean(args[1]));
        ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        UUID ply = claimChunk.getPlayerHandler().getUUID(args[0]);
        if (ply != null) {
            ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(ply);
            int unclaimed = 0;
            for (ChunkPos chunk : claimedChunks) {
                if (allWorlds || executor.getWorld().getName().equals(chunk.getWorld())) {
                    chunkHandler.unclaimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
                    unclaimed++;
                }
            }
            Utils.toPlayer(executor, claimChunk.getMessages().adminUnclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        } else {
            Utils.toPlayer(executor, claimChunk.getMessages().noPlayer);
        }

        return true;
    }

}
