package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.sub.AccessCmd;
import com.cjburkey.claimchunk.smartcommand.sub.ClaimChunkCmd;
import com.cjburkey.claimchunk.smartcommand.sub.HelpCmd;

import de.goldmensch.commanddispatcher.ExecutorLevel;
import de.goldmensch.commanddispatcher.command.ArgValuedSubCommand;
import de.goldmensch.commanddispatcher.command.SmartCommand;
import de.goldmensch.commanddispatcher.exceptions.CommandNotValidException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Special thank you to Goldmensch for the new command API! Github link:
 * https://github.com/Goldmensch/SmartCommandDispatcher <br>
 * I have included the JavaDoc comments for methods that I wasn't 100% certain on and/or were
 * lacking documenting comments. These are here purely for my own benefit, I forget things very very
 * quickly :P
 *
 * @since 0.0.23
 */
public class ClaimChunkBaseCommand extends SmartCommand {

    private final ClaimChunk claimChunk;

    // A simple usage of Java's new records!
    // These are pretty cool :)
    private record CommandStr(CCSubCommand cmd, String... args) {}

    public ClaimChunkBaseCommand(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;

        registerCmds(
                // `/chunk access`
                new CommandStr(new AccessCmd(claimChunk), "access"),
                // `/chunk claim`
                new CommandStr(new ClaimChunkCmd(claimChunk), "claim"),
                // `/chunk help`
                new CommandStr(new HelpCmd(claimChunk, this), "help"));
    }

    private void registerCmds(CommandStr... commands) {
        for (CommandStr cmd : commands) {
            try {
                registerSubCommand(cmd.cmd, cmd.args);
            } catch (CommandNotValidException e) {
                // Hopefully won't occur, but compile-time safety isn't one of
                // Java's strong-suits
                Utils.err("Failed to initialize subcommand: /chunk %s", String.join(" ", cmd.args));
                e.printStackTrace();
            }
        }
    }

    /**
     * Called upon command execution where the arguments do not point to a known subcommand, or no
     * arguments were passed.
     *
     * @param args The arguments (possibly) passed.
     * @param sender The command sender.
     * @param command The command that was executed (should be an instance of this class).
     * @param label The label used to execute the command (which may have been through an alias).
     * @return Whether the command was handled or whether the usage from within the plugin.yml
     *     should be shown.
     */
    @Override
    public boolean noSubFound(String[] args, CommandSender sender, Command command, String label) {
        displayHelp(label, sender);

        return false;
    }

    /**
     * Called when a subcommand is executed by: A) the console, but only players may use the given
     * subcommand, B) players, but only consoles may execute the subcommand.
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
     * Called when the player executing a command lacks the permission to do so.
     *
     * @param cmdArgs The command executed, along with the arguments passed.
     * @param sender The command sender.
     */
    @Override
    public void noPermission(ArgValuedSubCommand cmdArgs, CommandSender sender) {
        Utils.msg(
                sender,
                "You do not have permission to execute /chunk " + cmdArgs.getCommand().getName());
    }

    private void displayHelp(String cmdUsed, CommandSender ply) {
        // Display help for ClaimChunk
        Utils.msg(ply, claimChunk.getMessages().invalidCommand.replace("%%CMD%%", cmdUsed));
    }

    /**
     * Get a non-null collection of this command's subcommands that implement CCSubCommands.
     *
     * @return A non-null list of commands.
     */
    public @NotNull Collection<CCSubCommand> getCmds() {
        return getSubCommandMap().values().stream()
                .filter(cmd -> cmd instanceof CCSubCommand)
                .map(cmd -> (CCSubCommand) cmd)
                .collect(Collectors.toList());
    }

    /**
     * Attempt to get the CCSubCommand subcommand that matches the given argument path.
     *
     * @param args Path to command
     * @return The CCSubCommand for this command, or null if not found or not an instance of
     *     CCSubCommand.
     */
    public @Nullable CCSubCommand getCmd(String... args) {
        if (args.length > 0 && getSubCommandMap().get(args) instanceof CCSubCommand ccSubCmd) {
            return ccSubCmd;
        }
        return null;
    }
}
