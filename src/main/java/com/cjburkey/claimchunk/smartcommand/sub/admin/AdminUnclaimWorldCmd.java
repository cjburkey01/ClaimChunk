package com.cjburkey.claimchunk.smartcommand.sub.admin;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminUnclaimWorldCmd extends CCSubCommand {

    public AdminUnclaimWorldCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, false, "admin");
    }

    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdAdminUnclaimWorld;
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
        Player player = (Player) executor;
        int unclaimed =
                claimChunk.getChunkHandler().deleteAllWorldClaims(player.getWorld().getName());
        messagePly(
                player,
                claimChunk.getMessages().adminUnclaimAll.replace("%%CHUNKS%%", unclaimed + ""));

        return true;
    }
}
