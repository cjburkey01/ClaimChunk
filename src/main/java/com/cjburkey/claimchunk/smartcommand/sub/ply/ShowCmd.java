package com.cjburkey.claimchunk.smartcommand.sub.ply;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.ChunkOutlineHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.23
 */
public class ShowCmd extends CCSubCommand {

    public int maxSeconds = 60;

    public ShowCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "show");
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdShow;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg(claimChunk.getMessages().argSeconds, CCAutoComplete.NONE)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var chunkPos = new ChunkPos(player.getLocation().getChunk());
        var showForSeconds = 5;

        // Optional argument to show for a given time (up to the max defined)
        if (args.length == 1) {
            try {
                showForSeconds = Integer.min(Integer.parseInt(args[0]), maxSeconds);
            } catch (Exception e) {
                return false;
            }
        }

        // Use the new particle system!
        claimChunk
                .getChunkOutlineHandler()
                .showChunkFor(
                        chunkPos,
                        player,
                        showForSeconds,
                        ChunkOutlineHandler.OutlineSides.makeAll(true));
        return true;
    }
}
