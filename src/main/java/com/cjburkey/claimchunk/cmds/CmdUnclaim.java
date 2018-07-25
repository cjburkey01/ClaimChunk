package com.cjburkey.claimchunk.cmds;

import java.io.IOException;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;

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
        try {
            MainHandler.unclaimChunk(executor);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.msg(executor, Config.getColor("errorColor") + "An error occurred while unclaiming that chunk.");
        }
        return true;
    }

}