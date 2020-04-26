package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdAuto implements ICommand {

    @Override
    public String getCommand(ClaimChunk claimChunk) {
        return "auto";
    }

    @Override
    public String getDescription(ClaimChunk claimChunk) {
        return claimChunk.getMessages().cmdAuto;
    }

    @Override
    public boolean hasPermission(ClaimChunk claimChunk, CommandSender sender) {
        return Utils.hasPerm(sender, false, "auto");
    }

    @Override
    public String getPermissionMessage(ClaimChunk claimChunk) {
        return claimChunk.getMessages().autoNoPerm;
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
    public boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args) {
        if (AutoClaimHandler.toggle(executor)) {
            Utils.toPlayer(executor, claimChunk.getMessages().autoEnabled);
        } else {
            Utils.toPlayer(executor, claimChunk.getMessages().autoDisabled);
        }
        return true;
    }

}
