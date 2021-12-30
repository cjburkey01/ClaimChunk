package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** @since 0.0.23 */
public class AccessCmd extends CCSubCommand {

    public AccessCmd(ClaimChunk claimChunk) {
        // TODO: CREATE `/chunk admin access <PLY>` to allow listing from
        //       console as well
        super(claimChunk, Executor.PLAYER, "access", true);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdAccess);
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {new CCArg("player", CCAutoComplete.OFFLINE_PLAYER)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        if (args.length == 0) {
            claimChunk.getMainHandler().listAccessors(player);
        } else {
            claimChunk.getMainHandler().accessChunk(player, args[0].split(","));
        }
        return true;
    }
}
