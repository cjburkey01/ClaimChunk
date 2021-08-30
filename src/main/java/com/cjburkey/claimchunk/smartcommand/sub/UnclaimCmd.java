package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnclaimCmd extends CCSubCommand {

    public UnclaimCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdUnclaim;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "unclaim");
    }

    @Override
    public String getPermissionMessage() {
        return claimChunk.getMessages().unclaimNoPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[0];
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, CommandSender executor, String[] args) {
        Player player = (Player) executor;
        claimChunk.getMainHandler().unclaimChunk(false, false, player);
        return true;
    }
}
