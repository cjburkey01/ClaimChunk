package com.cjburkey.claimchunk.smartcommand.sub.admin;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** @since 0.0.23 */
public class AdminOverrideCmd extends CCSubCommand {

    public AdminOverrideCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdAdminOverride;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasAdmin(sender);
    }

    @Override
    public @NotNull String getPermissionMessage() {
        return claimChunk.getMessages().adminOverrideNoPerm;
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
        if (claimChunk.getAdminOverride().toggle(player.getUniqueId())) {
            messagePly(player, claimChunk.getMessages().adminOverrideEnable);
        } else {
            messagePly(player, claimChunk.getMessages().adminOverrideDisable);
        }
        return true;
    }
}
