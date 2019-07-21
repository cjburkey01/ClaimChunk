package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdAuto implements ICommand {

    @Override
    public String getCommand() {
        return "auto";
    }

    @Override
    public String getDescription() {
        return "Automatically claim chunks when you enter.";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, false, "auto");
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
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        if (!Utils.hasPerm(executor, false, "auto")) {
            Utils.toPlayer(executor, Config.errorColor(), Utils.getMsg("autoNoPerm"));
            return true;
        }
        if (AutoClaimHandler.toggle(executor)) {
            Utils.toPlayer(executor, Config.successColor(), Utils.getMsg("autoEnabled"));
        } else {
            Utils.toPlayer(executor, Config.successColor(), Utils.getMsg("autoDisabled"));
        }
        return true;
    }

}
