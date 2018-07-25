package com.cjburkey.claimchunk.cmds;

import java.io.IOException;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;

public class CmdAccess implements ICommand {

    public String getCommand() {
        return "access";
    }

    public String getDescription() {
        return "Toggle access for <player> in your claimed territory.";
    }

    public Argument[] getPermittedArguments() {
        return new Argument[] { new Argument("player", Argument.TabCompletion.OFFLINE_PLAYER) };
    }

    public int getRequiredArguments() {
        return 1;
    }

    public boolean onCall(Player executor, String[] args) {
        try {
            MainHandler.accessChunk(executor, args);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.msg(executor, Config.getColor("errorColor") + "There was an error while claiming that chunk.");
        }
        return true;
    }

}