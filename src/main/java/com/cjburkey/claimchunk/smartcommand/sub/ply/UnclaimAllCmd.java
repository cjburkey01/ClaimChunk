package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UnclaimAllCmd extends CCSubCommand {

    public UnclaimAllCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdUnclaimAll);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "unclaim");
    }

    @Override
    public @NotNull String getPermissionMessage() {
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
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var allWorlds = (args.length == 1 && Boolean.parseBoolean(args[0]));
        var chunkHandler = claimChunk.getChunkHandler();

        var claimedChunks = chunkHandler.getClaimedChunks(player.getUniqueId());
        int unclaimed = 0;
        for (var chunk : claimedChunks) {
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

        messagePly(
                player, claimChunk.getMessages().unclaimAll.replace("%%CHUNKS%%", unclaimed + ""));
        return true;
    }
}
