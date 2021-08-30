package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class CmdTnt implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "tnt";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdTnt;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, true, "toggle-tnt");
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().tntNoPerm;
    }

    @Override
    public boolean getShouldDisplayInHelp(ClaimChunk claimChunk, CommandSender sender) {
        return false;
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
        Utils.msg(executor, "&cThis command has been disabled!");
        Utils.msg(
                executor,
                "&cClaimChunk 0.1.0 will give players the ability to edit their own chunk's"
                    + " permissions, but it is not implemented in this version yet.");

        return true;
    }
}
