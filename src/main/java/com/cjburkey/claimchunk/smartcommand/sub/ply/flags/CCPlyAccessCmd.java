package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.jetbrains.annotations.NotNull;

public abstract class CCPlyAccessCmd extends CCSubCommand {

    public final boolean isForPly;
    public final boolean isForChunk;

    public CCPlyAccessCmd(@NotNull ClaimChunk claimChunk, boolean isForPly, boolean isForChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "access");
        this.isForPly = isForPly;
        this.isForChunk = isForChunk;
    }

    @Override
    public abstract @NotNull String getDescription();

    @Override
    public CCArg[] getPermittedArguments() {
        if (isForPly) {
            return new CCArg[] {new CCArg("otherPlayer", CCAutoComplete.OFFLINE_PLAYER)};
        }
        return new CCArg[0];
    }

    @Override
    public int getRequiredArguments() {
        return isForPly ? 1 : 0;
    }
}
