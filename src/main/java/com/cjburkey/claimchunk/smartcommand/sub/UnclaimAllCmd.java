package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnclaimAllCmd extends CCSubCommand {

    public UnclaimAllCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdUnclaimAll;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "unclaim");
    }

    @Override
    public String getPermissionMessage() {
        return claimChunk.getMessages().unclaimNoPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg("acrossAllWorlds", CCAutoComplete.BOOLEAN)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, CommandSender executor, String[] args) {
        Player player = (Player) executor;
        boolean allWorlds = (args.length == 1 && Boolean.parseBoolean(args[0]));
        ChunkHandler chunkHandler = claimChunk.getChunkHandler();

        ChunkPos[] claimedChunks = chunkHandler.getClaimedChunks(player.getUniqueId());
        int unclaimed = 0;
        for (ChunkPos chunk : claimedChunks) {
            if ((allWorlds || player.getWorld().getName().equals(chunk.getWorld()))
                    && claimChunk
                            .getMainHandler()
                            .unclaimChunk(
                                    false,
                                    true,
                                    player,
                                    chunk.getWorld(),
                                    chunk.getX(),
                                    chunk.getZ())) {
                unclaimed++;
            }
        }

        Utils.toPlayer(
                player, claimChunk.getMessages().unclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        return true;
    }
}
