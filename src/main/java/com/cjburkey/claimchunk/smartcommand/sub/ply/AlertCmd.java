package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** @since 0.0.23 */
public class AlertCmd extends CCSubCommand {

    public AlertCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER);
    }

    @Override
    public @NotNull Optional<String> getDescription() {
        return Optional.ofNullable(claimChunk.getMessages().cmdAlert);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "alert");
    }

    public @NotNull String getPermissionMessage() {
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
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        var newVal = claimChunk.getPlayerHandler().toggleAlerts(player.getUniqueId());
        var msg =
                (newVal
                        ? claimChunk.getMessages().enabledAlerts
                        : claimChunk.getMessages().disabledAlerts);
        messagePly(player, msg);
        return true;
    }
}
