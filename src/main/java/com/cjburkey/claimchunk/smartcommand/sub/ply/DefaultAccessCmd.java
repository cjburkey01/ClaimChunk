package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.i18n.V2JsonMessages;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.26
 */
public class DefaultAccessCmd extends CCSubCommand {

    public final boolean isSingleChunk;

    public DefaultAccessCmd(ClaimChunk claimChunk, boolean isSingleChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "access");

        this.isSingleChunk = isSingleChunk;
    }

    @Override
    public @Nullable String getDescription() {
        V2JsonMessages messages = claimChunk.getMessages();
        return isSingleChunk ? messages.cmdDefaultAccessHere : messages.cmdDefaultAccess;
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
        // TODO: THIS
        return true;
    }
}
