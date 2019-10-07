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
    public String getCommand() {
        return "auto";
    }

    @Override
    public String getDescription() {
        return ClaimChunk.getInstance().getMessages().cmdAuto;
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
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().autoNoPerm);
            return true;
        }
        if (AutoClaimHandler.toggle(executor)) {
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().autoEnabled);
        } else {
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().autoDisabled);
        }
        return true;
    }

}
