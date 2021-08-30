package com.cjburkey.claimchunk.smartcommand.sub;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** @since 0.0.23 */
public class AlertCmd extends CCSubCommand {

    public AlertCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdAlert;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "alert");
    }

    public String getPermissionMessage() {
        return claimChunk.getMessages().alertNoPerm;
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
        boolean newVal = claimChunk.getPlayerHandler().toggleAlerts(player.getUniqueId());
        Utils.toPlayer(
                player,
                (newVal
                        ? claimChunk.getMessages().enabledAlerts
                        : claimChunk.getMessages().disabledAlerts));
        return true;
    }
}
