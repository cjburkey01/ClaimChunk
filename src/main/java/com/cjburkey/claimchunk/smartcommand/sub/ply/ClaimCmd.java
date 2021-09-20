package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** @since 0.0.23 */
public class ClaimCmd extends CCSubCommand {

    public ClaimCmd(ClaimChunk claimChunk) {
        // TODO: ADD `/chunk admin claim <PLY>` to allow claiming a chunk for
        //       a player.
        //       ADD `/chunk admin claim <PLY> <X> <Y>` to allow claiming the
        //       chunk containing the world coordinates X and Y for the given
        //       player.
        super(claimChunk, Executor.PLAYER, "claim");
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdClaim);
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[0];
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        claimChunk.getMainHandler().claimChunk(player, player.getLocation().getChunk());
        return true;
    }
}
