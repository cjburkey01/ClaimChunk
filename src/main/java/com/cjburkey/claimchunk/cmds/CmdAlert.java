package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdAlert implements ICommand {

    @Override
    public String getCommand() {
        return "alert";
    }

    @Override
    public String getDescription() {
        return "Toggle whether or not you will receive alerts when someone enters your chunks";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "alert");
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
        if (!Utils.hasPerm(executor, true, "alert")) {
            Utils.toPlayer(executor, Config.errorColor(), Utils.getMsg("accessNoPerm"));
            return true;
        }

        boolean newVal = ClaimChunk.getInstance().getPlayerHandler().toggleAlerts(executor.getUniqueId());
        Utils.toPlayer(executor, Config.infoColor(), Utils.getMsg(newVal ? "enabledAlerts" : "disabledAlerts"));
        return true;
    }

}
