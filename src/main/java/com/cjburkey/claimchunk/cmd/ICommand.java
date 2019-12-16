package com.cjburkey.claimchunk.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ICommand {

    /**
     * Get the name of this subcommand.
     *
     * @return The name of this subcommand.
     */
    String getCommand();

    /**
     * Get the description for this subcommand.
     *
     * @return The description for this subcommand.
     */
    String getDescription();

    /**
     * Check whether the provided executor has permission to execute this
     * subcommand
     *
     * @param executor This subcommand's executor.
     * @return Whether this player has access to this subcommand.
     */
    boolean hasPermission(CommandSender executor);

    /**
     * Get the message to be displayed when users don't have permission to use
     * this subcommand.
     *
     * @return The lacking permissions message.
     */
    String getPermissionMessage();

    /**
     * Get whether this command should be displayed in the help subcommand list
     * for the provided executor.
     *
     * @param sender The help command's executor.
     * @return Whether this command will be dispalyed within the subcommand list.
     */
    default boolean getShouldDisplayInHelp(CommandSender sender) {
        return hasPermission(sender);
    }

    /**
     * Get a list of all arguments that could be provided to this subcommand.
     *
     * @return An array with all the possible arguments.
     */
    Argument[] getPermittedArguments();

    /**
     * Get the number of the possible arguments that are required arguments.
     *
     * @return The number of required arguments.
     */
    int getRequiredArguments();

    /**
     * Executes this command.
     *
     * @param cmdUsed  The version of the base command used (like `/chunk` or
     *                 `/claimchunk`).
     * @param executor The subcommand executor.
     * @param args     The raw string arguments passed by the executor.
     * @return Whether this subcommand's usage should be displayed.
     */
    boolean onCall(String cmdUsed, Player executor, String[] args);

}
