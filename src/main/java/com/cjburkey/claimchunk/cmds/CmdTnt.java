package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdTnt implements ICommand {

    @Override
    public String getCommand() {
        return "tnt";
    }

    @Override
    public String getDescription() {
        return "Toggle whether or not TNT can explode in the current chunk";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "toggle-tnt");
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[0];
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        if (!Config.getBool("protection", "blockTnt")) {
            Utils.toPlayer(executor, Config.errorColor(), Utils.getMsg("tntAlreadyEnabled"));
            return true;
        }
        if (!Utils.hasPerm(executor, true, "toggle-tnt")) {
            Utils.toPlayer(executor, Config.errorColor(), Utils.getMsg("tntNoPerm"));
            return true;
        }
        MainHandler.toggleTnt(executor);
        return true;
    }

}
