package com.cjburkey.claimchunk.smartcommand.sub.ply;

import claimchunk.dependency.de.goldmensch.commanddispatcher.Executor;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.gui.screens.MainMenu;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.26
 */
public class GuiCmd extends CCSubCommand {

    public GuiCmd(ClaimChunk claimChunk) {
        super(claimChunk, Executor.PLAYER, true, "player");
    }

    @Override
    public @Nullable String getDescription() {
        return claimChunk.getMessages().cmdGui;
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
        claimChunk.getGuiHandler().openOrRefreshGui(new MainMenu(claimChunk, (Player) executor));
        return true;
    }
}
