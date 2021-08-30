package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class CmdAdminOverride implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "adminoverride";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdAdminOverride;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().adminOverrideNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments(ClaimChunk claimChunk) {
        return new Argument[] {};
    }

    @Override
    public int getRequiredArguments(ClaimChunk claimChunk) {
        return 0;
    }

    @Override
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player p, String[] args) {
        if (claimChunk.getAdminOverride().toggle(p.getUniqueId())) {
            Utils.toPlayer(p, claimChunk.getMessages().adminOverrideEnable);
        } else {
            Utils.toPlayer(p, claimChunk.getMessages().adminOverrideDisable);
        }
        return true;
    }
}
