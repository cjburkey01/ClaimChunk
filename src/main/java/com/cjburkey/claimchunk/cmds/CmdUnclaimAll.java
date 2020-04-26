package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdUnclaimAll implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "unclaimall";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdUnclaimAll;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, true, "unclaim");
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().unclaimNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[]{new Argument("acrossAllWorlds", Argument.TabCompletion.BOOLEAN)};
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        boolean allWorlds = (args.length == 1 && Boolean.parseBoolean(args[0]));
        ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(executor.getUniqueId());
        int unclaimed = 0;
        for (ChunkPos chunk : claimedChunks) {
            if ((allWorlds
                    || executor.getWorld().getName().equals(chunk.getWorld()))
                    && claimChunk.getCommandHandler().mainHandler
                    .unclaimChunk(false, true, executor, chunk.getWorld(), chunk.getX(), chunk.getZ())) {
                unclaimed++;
            }
        }

        Utils.toPlayer(executor, claimChunk.getMessages().unclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        return true;
    }

}
