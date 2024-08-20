package com.cjburkey.claimchunk.smartcommand.sub.ply;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.23
 */
public class AutoCmd extends CCSubCommand {

    public AutoCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "auto");
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdAuto;
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
        if (AutoClaimHandler.toggle(player)) {
            messagePly(player, claimChunk.getMessages().autoEnabled);
        } else {
            messagePly(player, claimChunk.getMessages().autoDisabled);
        }
        return true;
    }
}
