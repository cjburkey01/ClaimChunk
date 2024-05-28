package com.cjburkey.claimchunk.smartcommand.sub.ply;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.gui.screens.MainMenu;
import com.cjburkey.claimchunk.smartcommand.CCSubCommand;

import de.goldmensch.commanddispatcher.Executor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 0.0.23
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
        claimChunk.getGuiHandler().openGui((Player) executor, new MainMenu(claimChunk));
        return true;
    }
}
