package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
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
        return ClaimChunk.getInstance().getMessages().cmdTnt;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "toggle-tnt");
    }

    @Override
    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().tntNoPerm;
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
            Utils.toPlayer(executor, ClaimChunk.getInstance().getMessages().tntAlreadyEnabled);
            return true;
        }
        MainHandler.toggleTnt(executor);
        return true;
    }

}
