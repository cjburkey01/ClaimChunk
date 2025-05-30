package com.cjburkey.claimchunk.smartcommand.sub.ply;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.23
 */
public class GiveCmd extends CCSubCommand {

    public GiveCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "give");
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdGive;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg(claimChunk.getMessages().argPlayer, CCAutoComplete.OFFLINE_PLAYER),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        claimChunk.getMainHandler().giveChunk(player, player.getLocation().getChunk(), args[0]);
        return true;
    }
}
