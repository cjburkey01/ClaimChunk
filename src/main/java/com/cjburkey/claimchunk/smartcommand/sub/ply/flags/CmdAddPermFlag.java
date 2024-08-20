package com.cjburkey.claimchunk.smartcommand.sub.ply.flags;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.jetbrains.annotations.NotNull;

public abstract class CmdAddPermFlag extends CCSubCommand {

    public CmdAddPermFlag(@NotNull ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, true, "player", "access");
    }

    // TODO: MAKE GENERAL FORM AND 4 IMPLEMENTORS

}
