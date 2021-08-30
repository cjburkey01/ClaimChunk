package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import de.goldmensch.commanddispatcher.ExecutorLevel;
import de.goldmensch.commanddispatcher.subcommand.SmartSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper around Goldmensch's smart subcommand to include more information
 * and specific handling procedures.
 *
 * @since 0.0.23
 */
public abstract class CCSubCommand extends SmartSubCommand {

    protected final ClaimChunk claimChunk;

    public CCSubCommand(ClaimChunk claimChunk, ExecutorLevel executorLevel) {
        super(executorLevel, "");

        this.claimChunk = claimChunk;
    }

    /**
     * Get the description for this subcommand.
     *
     * @return The description for this subcommand.
     */
    public abstract String getDescription();

    /**
     * Check whether the provided executor has permission to execute this
     * subcommand
     *
     * @param executor This subcommand's executor.
     * @return Whether this player has access to this subcommand.
     */
    public abstract boolean hasPermission(CommandSender executor);

    /**
     * Get the message to be displayed when users don't have permission to use
     * this subcommand.
     *
     * @return The lacking permissions message.
     */
    public abstract String getPermissionMessage();

    /**
     * Get whether this command should be displayed in the help subcommand list
     * for the provided executor.
     *
     * @return Whether this command will be displayed within the help list.
     */
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return hasPermission(sender);
    }

    /**
     * Get a list of all arguments that could be provided to this subcommand.
     *
     * @return An array with all the possible arguments.
     */
    public abstract CCArg[] getPermittedArguments();

    /**
     * Get the maximum number of arguments (separated by spaces) that may be
     * passed into this command. Normally, this will just be the number of
     * arguments given in `getPermittedArguments()`.
     *
     * @return The maximum number of arguments for this command.
     */
    public int getMaxArguments() {
        return getPermittedArguments().length;
    }

    /**
     * Get the number of the possible arguments that are required arguments.
     *
     * @return The number of required arguments.
     */
    public abstract int getRequiredArguments();

    /**
     * Called upon execution of this command.
     *
     * @param cmdUsed  The version of the base command used (like `/chunk` or
     *                 `/claimchunk`).
     * @param executor The subcommand executor.
     * @param args     The raw string arguments passed by the executor.
     * @return Whether this subcommand's usage should be displayed.
     */
    public abstract boolean onCall(String cmdUsed, CommandSender executor, String[] args);

    @Override
    public final boolean onCommand(@NotNull CommandSender sender,
                                   @NotNull Command command,
                                   @NotNull String label,
                                   @NotNull String[] args) {
        Player player = (Player) sender;

        // Check if the player has the base permission
        if (!Utils.hasPerm(sender, true, "base")) {
            Utils.toPlayer(player, claimChunk.getMessages().noPluginPerm);
            return true;
        }

        // Create a list of arguments that should be passed into the command
        List<String> outArgs = Arrays.stream(args)
                .map(String::trim)
                .filter(arg -> !arg.isEmpty())
                .collect(Collectors.toList());

        // Make sure the executor has permission to use this command
        if (!hasPermission(sender)) {
            Utils.msg(sender, getPermissionMessage());
            return true;
        }

        // Make sure the player provided the correct number of arguments
        if (outArgs.size() < getRequiredArguments()
                || outArgs.size() > getMaxArguments()) {
            displayUsage(label, player);
            return true;
        }

        // Check if the command executed successfully
        boolean success = onCall(label, player, outArgs.toArray(new String[0]));

        // If the command didn't execute correctly, the usage for this command
        // should be displayed
        if (!success) {
            displayUsage(label, player);
        }

        return true;
    }

    private void displayUsage(String cmdUsed, Player ply) {
        // Display usage for a specific command
        Utils.msg(ply, claimChunk.getMessages().errorDisplayUsage
                .replace("%%CMD%%", cmdUsed)
                .replace("%%SUB_CMD%%", getName())
                .replace("%%ARGS%%", getUsageArgs()));
    }

    public String getUsageArgs() {
        // Create an empty StringBuilder to build the output string
        StringBuilder out = new StringBuilder();

        // Loop through all of the permitted arguments
        for (int i = 0; i < getPermittedArguments().length; i++) {
            // Check if this argument is required
            boolean req = (i < getRequiredArguments());

            // Add the command wrapper start
            out.append(req ? '<' : '[');

            // Add the argument name
            out.append(getPermittedArguments()[i].arg);

            // Add the command wrapper end
            out.append(req ? '>' : ']');

            // Add ending whitespace
            out.append(' ');
        }

        // Return the output
        return out.toString().trim();
    }

    @Nullable
    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender,
                                            @NotNull Command command,
                                            @NotNull String alias,
                                            @NotNull String[] args) {
        return null;
    }

    /**
     * Replacement for the old argument class.
     */
    public record CCArg(String arg, CCAutoComplete tab) {}

    public enum CCAutoComplete {

        /**
         * No tab completion should occur.
         */
        NONE,

        /**
         * Tab completion should include claim chunk commands.
         */
        COMMAND,

        /**
         * Tab completion should include all online players.
         */
        ONLINE_PLAYER,

        /**
         * Tab completion should include all players that have joined the server.
         */
        OFFLINE_PLAYER,

        /**
         * Tab completion should be either `true` or `false`.
         */
        BOOLEAN,

    }

}
