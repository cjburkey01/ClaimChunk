package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;
import org.bukkit.entity.Player;

public class CmdAccess implements ICommand {

    public String getCommand() {
        return "access";
    }

    public String getDescription() {
        return "Toggle access for <player> in your claimed territory.";
    }

    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("player", Argument.TabCompletion.OFFLINE_PLAYER)};
    }

    public int getRequiredArguments() {
        return 1;
    }

    public boolean onCall(Player executor, String[] args) {
        MainHandler.accessChunk(executor, args);
        return true;
    }

}
