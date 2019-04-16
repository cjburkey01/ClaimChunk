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
        return "Toggle access for [player] in your claimed territory or list players that have access to your chunks.";
    }

    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("player", Argument.TabCompletion.OFFLINE_PLAYER)};
    }

    public int getRequiredArguments() {
        return 0;
    }

    public boolean onCall(Player executor, String[] args) {
        if (args.length == 0) MainHandler.listAccessors(executor);
        else MainHandler.accessChunk(executor, args[0].split(","));
        return true;
    }

}
