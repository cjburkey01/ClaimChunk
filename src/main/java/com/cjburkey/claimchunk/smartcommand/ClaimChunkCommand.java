package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.sub.VersionCmd;
import de.goldmensch.commanddispatcher.ExecutorLevel;
import de.goldmensch.commanddispatcher.command.ArgValuedSubCommand;
import de.goldmensch.commanddispatcher.command.SmartCommand;
import de.goldmensch.commanddispatcher.exceptions.CommandNotValidException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Special thank you to Goldmensch for the new command API!
 * Github link: https://github.com/Goldmensch/SmartCommandDispatcher
 *
 * <br />
 *
 * I have included the JavaDoc comments for methods that I wasn't 100% certain
 * on and/or were lacking documenting comments. These are here purely for my
 * own benefit, I forget things very very quickly :P
 *
 * @since 0.0.23
 */
public class ClaimChunkCommand extends SmartCommand {

    private final ClaimChunk claimChunk;

    public ClaimChunkCommand(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;

        // Register subcommands
        try {
            registerSubCommand(new VersionCmd(claimChunk), "version");
        } catch (CommandNotValidException e) {
            // Hopefully won't occur, but compile-time safety isn't one of
            // Java's strong-suits
            Utils.err("Failed to initialize subcommand:");
            e.printStackTrace();
        }
    }

    /**
     * Called upon command execution where the arguments do not point to a
     * known subcommand, or no arguments were passed.
     *
     * @param args The arguments (possibly) passed.
     * @param sender The command sender.
     * @param command The command that was executed (should be an instance of this class).
     * @param label The label used to execute the command (which may have been through an alias).
     *
     * @return Whether the command was handled or whether the usage from within the plugin.yml should be shown.
     */
    @Override
    public boolean noSubFound(String[] args, CommandSender sender, Command command, String label) {
        Utils.msg(sender, "No args provided");

        return false;
    }

    /**
     * Called when a subcommand is executed by: A) the console, but only
     * players may use the given subcommand, B) players, but only consoles
     * may execute the subcommand.
     *
     * @param cmdArgs The command executed, along with the arguments passed.
     * @param sender The command sender.
     */
    @Override
    public void wrongExecutorLevel(ArgValuedSubCommand cmdArgs, CommandSender sender) {
        ExecutorLevel cmdExecutorLevel = cmdArgs.getCommand().getExecutorLevel();

        if (cmdExecutorLevel == ExecutorLevel.CONSOLE) {
            // This subcommand can only be used by the console
            Utils.msg(sender, claimChunk.getMessages().ingameOnly);
        } else if (cmdExecutorLevel == ExecutorLevel.PLAYER) {
            // This subcommand can only be used by in-game players
            Utils.msg(sender, claimChunk.getMessages().consoleOnly);
        }
    }

    /**
     * Called when the player executing a command lacks the permission to do
     * so.
     *
     * @param cmdArgs The command executed, along with the arguments passed.
     * @param sender The command sender.
     */
    @Override
    public void noPermission(ArgValuedSubCommand cmdArgs, CommandSender sender) {
        Utils.msg(sender, "You do not have permission to execute /chunk "
                + cmdArgs.getCommand().getName());
    }

}
