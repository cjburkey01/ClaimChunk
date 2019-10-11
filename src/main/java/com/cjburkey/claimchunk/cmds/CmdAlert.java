package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
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
        return ClaimChunk.getInstance().getMessages().cmdAlert;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "alert");
    }

    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().alertNoPerm;
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
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().accessNoPerm);
            return true;
        }

        boolean newVal = ClaimChunk.getInstance().getPlayerHandler().toggleAlerts(executor.getUniqueId());
        Utils.toPlayer(executor, (newVal ? ClaimChunk.getInstance().getMessages().enabledAlerts : ClaimChunk.getInstance().getMessages().disabledAlerts));
        return true;
    }

}
