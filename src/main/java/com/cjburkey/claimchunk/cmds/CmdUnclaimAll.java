package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdUnclaimAll implements ICommand {

    @Override
    public String getCommand() {
        return "unclaimall";
    }

    @Override
    public String getDescription() {
        return "Unclaim all the chunks you own.";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "unclaim");
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("acrossAllWorlds", Argument.TabCompletion.BOOLEAN)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        boolean allWorlds = (args.length == 1 && Boolean.parseBoolean(args[0]));
        ChunkHandler chunkHandler = ClaimChunk.getInstance().getChunkHandler();

        ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(executor.getUniqueId());
        int unclaimed = 0;
        for (ChunkPos chunk : claimedChunks) {
            if ((allWorlds || executor.getWorld().getName().equals(chunk.getWorld()))
                    && MainHandler.unclaimChunk(false, true, executor, chunk.getWorld(), chunk.getX(), chunk.getZ())) {
                unclaimed++;
            }
        }

        Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().unclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        return true;
    }

}
