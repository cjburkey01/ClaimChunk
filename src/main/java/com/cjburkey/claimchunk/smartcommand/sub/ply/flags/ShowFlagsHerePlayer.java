package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ShowFlagsHerePlayer extends CCSubCommand {

    public ShowFlagsHerePlayer(@NotNull ClaimChunk claimChunk, @NotNull Executor executor) {
        super(claimChunk, executor, true, "player", "access");
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
    public boolean onCall(
            @NotNull String cmdUsed, @NotNull CommandSender executor, @NotNull String[] args) {
        Set<String> allFlags = claimChunk.getPermFlags().getAllFlags();

        return false;
    }
}
