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
    public String getCommand() {
        return "adminunclaimall";
    }

    @Override
    public String getDescription() {
        return ClaimChunk.getInstance().getMessages().cmdAdminUnclaimAll;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, false, "admin");
    }

    @Override
    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().unclaimNoPermAdmin;
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {
                new Argument("player", Argument.TabCompletion.OFFLINE_PLAYER),
                new Argument("acrossAllWorlds", Argument.TabCompletion.NONE),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        boolean allWorlds = (args.length == 2 && Boolean.parseBoolean(args[1]));
        ChunkHandler chunkHandler = ClaimChunk.getInstance().getChunkHandler();

        UUID ply = ClaimChunk.getInstance().getPlayerHandler().getUUID(args[0]);
        if (ply != null) {
            ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(ply);
            int unclaimed = 0;
            for (ChunkPos chunk : claimedChunks) {
                if (allWorlds || executor.getWorld().getName().equals(chunk.getWorld())) {
                    chunkHandler.unclaimChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
                    unclaimed++;
                }
            }
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().adminUnclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        } else {
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().noPlayer);
        }

        return true;
    }

}
