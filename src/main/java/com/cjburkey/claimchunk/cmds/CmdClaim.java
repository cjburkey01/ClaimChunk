package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;
import org.bukkit.entity.Player;

public class CmdClaim implements ICommand {

    @Override
    public String getCommand() {
        return "claim";
    }

    @Override
    public String getDescription() {
        return "Claim the chunk you're standing in.";
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(Player executor, String[] args) {
        MainHandler.claimChunk(executor, executor.getLocation().getChunk());
        return true;
    }

}
