package com.cjburkey.claimchunk.smartcommand.sub.admin;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminUnclaimCmd extends CCSubCommand {

    public AdminUnclaimCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, false, "admin");
    }

    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdAdminUnclaim;
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
        claimChunk.getMainHandler().unclaimChunk(true, false, player);
        return true;
    }
}
