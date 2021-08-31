package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.ExecutorLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** @since 0.0.23 */
public class GiveCmd extends CCSubCommand {

    public GiveCmd(ClaimChunk claimChunk) {
        super(claimChunk, ExecutorLevel.PLAYER);
    }

    @Override
    public String getDescription() {
        return claimChunk.getMessages().cmdGive;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "claim");
    }

    @Override
    public @NotNull String getPermissionMessage() {
        return claimChunk.getMessages().giveNoPerm;
    }

    @Override
    public CCArg[] getPermittedArguments() {
        return new CCArg[] {
            new CCArg("player", CCAutoComplete.OFFLINE_PLAYER),
        };
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public boolean onCall(@NotNull String cmdUsed, @NotNull CommandSender executor, String[] args) {
        var player = (Player) executor;
        claimChunk.getMainHandler().giveChunk(player, player.getLocation().getChunk(), args[0]);
        return true;
    }
}
