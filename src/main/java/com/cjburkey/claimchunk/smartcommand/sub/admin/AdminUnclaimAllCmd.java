package com.cjburkey.claimchunk.smartcommand.sub.admin;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;
import de.goldmensch.commanddispatcher.Executor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AdminUnclaimAllCmd extends CCSubCommand {

    public AdminUnclaimAllCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdAdminUnclaimAll);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public @NotNull String getPermissionMessage() {
        return claimChunk.getMessages().unclaimNoPermAdmin;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg("player", CCAutoComplete.OFFLINE_PLAYER),
            new CCArg("acrossAllWorlds", CCAutoComplete.NONE),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var allWorlds = (args.length == 2 && Boolean.parseBoolean(args[1]));
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
