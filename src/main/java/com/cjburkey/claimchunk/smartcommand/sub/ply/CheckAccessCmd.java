package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.24
 */
public class CheckAccessCmd extends CCSubCommand {

    public CheckAccessCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "access");
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdCheckAccess;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg(claimChunk.getMessages().argPlayer, CCAutoComplete.OFFLINE_PLAYER)
        };
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        if (1 <= args.length) {
            // User is requesting permissions for a particular player
            claimChunk.getMainHandler().checkAccess((Player) executor, args[0]);
        } else {
            // User is requesting all players with permissions on the chunk
            claimChunk.getMainHandler().checkAccess((Player) executor);
        }
        return true;
    }
}
