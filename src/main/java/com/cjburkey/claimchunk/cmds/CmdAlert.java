package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdAlert implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "alert";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdAlert;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, true, "alert");
    }

    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().alertNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[0];
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        boolean newVal = claimChunk.getPlayerHandler().toggleAlerts(executor.getUniqueId());
        Utils.toPlayer(executor, (newVal ?
                claimChunk.getMessages().enabledAlerts
                : claimChunk.getMessages().disabledAlerts));
        return true;
    }

}
