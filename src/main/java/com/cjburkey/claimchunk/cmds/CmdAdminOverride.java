package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdAdminOverride implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "adminoverride";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdTeam;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().teamNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[]{};
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player p, String[] args) {
        if(claimChunk.getAdminOverride().hasOverride(p.getUniqueId())) {
            Utils.toPlayer(p, claimChunk.getMessages().teamEnable);
        }else {
            Utils.toPlayer(p, claimChunk.getMessages().teamDisable);
        }
        return true;
    }

}
