package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.AutoClaimHandler;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** @since 0.0.23 */
public class AutoCmd extends CCSubCommand {

    public AutoCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdAuto;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, false, "auto");
    }

    @Override
    public String getPermissionMessage() {
        return claimChunk.getMessages().autoNoPerm;
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
        if (AutoClaimHandler.toggle(player)) {
            Utils.toPlayer(player, claimChunk.getMessages().autoEnabled);
        } else {
            Utils.toPlayer(player, claimChunk.getMessages().autoDisabled);
        }
        return true;
    }
}
