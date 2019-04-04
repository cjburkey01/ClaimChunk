package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.entity.Player;

public class CmdAuto implements ICommand {

    public String getCommand() {
        return "auto";
    }

    public String getDescription() {
        return "Automatically claim chunks when you enter.";
    }

    public Argument[] getPermittedArguments() {
        return new Argument[] {};
    }

    public int getRequiredArguments() {
        return 0;
    }

    public boolean onCall(Player executor, String[] args) {
        if (!executor.hasPermission("claimchunk.auto")) {
            Utils.toPlayer(executor, Config.getColor("errorColor"), Utils.getMsg("autoNoPerm"));
            return true;
        }
        if (AutoClaimHandler.toggle(executor)) {
            Utils.toPlayer(executor, Config.getColor("successColor"), Utils.getMsg("autoEnabled"));
        } else {
            Utils.toPlayer(executor, Config.getColor("successColor"), Utils.getMsg("autoDisabled"));
        }
        return true;
    }

}
