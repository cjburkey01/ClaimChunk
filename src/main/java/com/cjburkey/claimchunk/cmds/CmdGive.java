package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdGive implements ICommand {

    @Override
    public String getCommand() {
        return "give";
    }

    @Override
    public String getDescription() {
        return ClaimChunk.getInstance().getMessages().cmdGive;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Config.getBool("chunks", "allowChunkGive") && Utils.hasPerm(sender, true, "claim");
    }

    @Override
    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().claimNoPerm;
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {
                new Argument("player", Argument.TabCompletion.OFFLINE_PLAYER),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        MainHandler.giveChunk(executor, executor.getLocation().getChunk(), args[0]);
        return true;
    }

}
