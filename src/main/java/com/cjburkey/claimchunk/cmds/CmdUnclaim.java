package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;
import org.bukkit.entity.Player;

public class CmdUnclaim implements ICommand {

    public String getCommand() {
        return "unclaim";
    }

    public String getDescription() {
        return "Unclaim the chunk you're standing in.";
    }

    public Argument[] getPermittedArguments() {
        return new Argument[] {};
    }

    public int getRequiredArguments() {
        return 0;
    }

    public boolean onCall(Player executor, String[] args) {
        MainHandler.unclaimChunk(executor);
        return true;
    }

}
