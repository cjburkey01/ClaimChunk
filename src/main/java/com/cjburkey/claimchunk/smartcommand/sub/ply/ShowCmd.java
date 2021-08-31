package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkOutlineHandler;
import com.cjburkey.claimchunk.chunk.ChunkPos;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** @since 0.0.23 */
public class ShowCmd extends CCSubCommand {

    public int maxSeconds = 60;

    public ShowCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdShow;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    @Override
    public @NotNull String getPermissionMessage() {
        return claimChunk.getMessages().noPluginPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg("seconds", CCAutoComplete.NONE)};
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
