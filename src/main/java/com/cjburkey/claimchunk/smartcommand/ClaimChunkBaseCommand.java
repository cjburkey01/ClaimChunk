package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.smartcommand.sub.admin.AdminOverrideCmd;
import com.cjburkey.claimchunk.smartcommand.sub.admin.AdminReloadCmd;
import com.cjburkey.claimchunk.smartcommand.sub.admin.AdminUnclaimAllCmd;
import com.cjburkey.claimchunk.smartcommand.sub.admin.AdminUnclaimCmd;
import com.cjburkey.claimchunk.smartcommand.sub.ply.*;

import de.goldmensch.commanddispatcher.Executor;
import de.goldmensch.commanddispatcher.command.SmartCommand;
import de.goldmensch.commanddispatcher.exceptions.CommandNotValidException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Special thank you to Goldmensch for the new command API! GitHub link: <a
 * href="https://github.com/Goldmensch/SmartCommandDispatcher">github.com/Goldmensch/SmartCommandDispatcher</a>.
 * <br>
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

        // TODO: Update commands to be more modular and incorporate elements of the (soon to exist)
        // public API.

        // Player commands
        registerCmds(
                // `/chunk access`
                new CommandStr(new AccessCmd(claimChunk), "access"),
                // `/chunk checkaccess`
                new CommandStr(new CheckAccessCmd(claimChunk), "checkaccess"),
                // `/chunk revokeaccess`
                new CommandStr(new RevokeAccessCmd(claimChunk), "revokeaccess"),
                // `/chunk alert`
                new CommandStr(new AlertCmd(claimChunk), "alert"),
                // `/chunk auto`
                new CommandStr(new AutoCmd(claimChunk), "auto"),
                // `/chunk claim`
                new CommandStr(new ClaimCmd(claimChunk), "claim"),
                // `/chunk give`
                new CommandStr(new GiveCmd(claimChunk), "give"),
                // `/chunk help`
                new CommandStr(new HelpCmd(claimChunk, this), "help"),
                // `/chunk info`
                new CommandStr(new InfoCmd(claimChunk), "info"),
                // `/chunk list`
                new CommandStr(new ListCmd(claimChunk), "list"),
                // `/chunk name`
                new CommandStr(new NameCmd(claimChunk), "name"),
                // `/chunk show claimed`
                new CommandStr(new ShowClaimedCmd(claimChunk), "show", "claimed"),
                // `/chunk show`
                new CommandStr(new ShowCmd(claimChunk), "show"),
                // `/chunk unclaim all`
                new CommandStr(new UnclaimAllCmd(claimChunk), "unclaim", "all"),
                // `/chunk unclaim`
                new CommandStr(new UnclaimCmd(claimChunk), "unclaim"),
                // `/chunk scan`
                new CommandStr(new ScanCmd(claimChunk), "scan"));

        // Admin commands
        registerCmds(
                // `/chunk admin override`
                new CommandStr(new AdminOverrideCmd(claimChunk), "admin", "override"),
                // `/chunk admin unclaim all`
                new CommandStr(new AdminUnclaimAllCmd(claimChunk), "admin", "unclaim", "all"),
                // `/chunk admin unclaim`
                new CommandStr(new AdminUnclaimCmd(claimChunk), "admin", "unclaim"),
                // `/chunk admin reload`
                new CommandStr(new AdminReloadCmd(claimChunk), "admin", "reload"));
    }

    private void registerCmds(CommandStr... commands) {
        for (CommandStr cmd : commands) {
            try {
                registerSubCommand(cmd.cmd, cmd.args);
            } catch (CommandNotValidException e) {
                // Hopefully won't occur, but compile-time safety isn't one of
                // Java's strong-suits
                Utils.err("Failed to initialize subcommand: /chunk %s", String.join(" ", cmd.args));
                //noinspection CallToPrintStackTrace
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
    public boolean noSubFound(
            @NotNull String[] args,
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label) {
        displayHelp(label, sender);

        return true;
    }

    /**
     * Called when a subcommand is executed by: A) the console, but only players may use the given
     * subcommand, B) players, but only consoles may execute the subcommand.
     *
     * @param cmd The command executed, along with the arguments passed.
     * @param sender The command sender.
     */
    @SuppressWarnings("GrazieInspection")
    @Override
    public void wrongExecutor(
            @NotNull SubCommandEntity cmd,
            @NotNull CommandSender sender,
            @NotNull Executor executor) {
        if (executor == Executor.CONSOLE) {
            // This subcommand can only be used by the console
            Utils.msg(sender, claimChunk.getMessages().consoleOnly);
        } else if (executor == Executor.PLAYER) {
            // This subcommand can only be used by in-game players
            Utils.msg(sender, claimChunk.getMessages().ingameOnly);
        }
    }

    /**
     * Called when the player executing a command lacks the permission to do so.
     *
     * @param cmd The command executed, along with the arguments passed.
     * @param sender The command sender.
     */
    @Override
    public void noPermission(@NotNull SubCommandEntity cmd, @NotNull CommandSender sender) {
        Utils.msg(sender, claimChunk.getMessages().commandNoPermission);
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

    public @NotNull Optional<CCSubCommand> getSubCmd(@NotNull String[] args) {
        return super.searchSub(args)
                .flatMap(
                        cmd -> {
                            if (cmd.getCommand() instanceof CCSubCommand ccSubCommand) {
                                return Optional.of(ccSubCommand);
                            } else {
                                return Optional.empty();
                            }
                        });
    }
}
