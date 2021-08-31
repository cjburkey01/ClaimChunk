package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public interface ICommand {

    /**
     * Get the name of this subcommand.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @return The name of this subcommand.
     */
    String getCommand(ClaimChunk claimChunk);

    /**
     * Get the description for this subcommand.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @return The description for this subcommand.
     */
    String getDescription(ClaimChunk claimChunk);

    /**
     * Check whether the provided executor has permission to execute this subcommand
     *
     * @param claimChunk The instance of ClaimChunk.
     * @param executor This subcommand's executor.
     * @return Whether this player has access to this subcommand.
     */
    boolean hasPermission(ClaimChunk claimChunk, CommandSender executor);

    /**
     * Get the message to be displayed when users don't have permission to use this subcommand.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @return The lacking permissions message.
     */
    String getPermissionMessage(ClaimChunk claimChunk);

    /**
     * Get whether this command should be displayed in the help subcommand list for the provided
     * executor.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @param sender The help command's executor.
     * @return Whether this command will be displayed within the subcommand list.
     */
    default boolean getShouldDisplayInHelp(ClaimChunk claimChunk, CommandSender sender) {
        return hasPermission(claimChunk, sender);
    }

    /**
     * Get a list of all arguments that could be provided to this subcommand.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @return An array with all the possible arguments.
     */
    Argument[] getPermittedArguments(ClaimChunk claimChunk);

    /**
     * Get the number of the possible arguments that are required arguments.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @return The number of required arguments.
     */
    int getRequiredArguments(ClaimChunk claimChunk);

    /**
     * Executes this command.
     *
     * @param claimChunk The instance of ClaimChunk.
     * @param cmdUsed The version of the base command used (like `/chunk` or `/claimchunk`).
     * @param executor The subcommand executor.
     * @param args The raw string arguments passed by the executor.
     * @return Whether this subcommand's usage should be displayed.
     */
    boolean onCall(ClaimChunk claimChunk, String cmdUsed, Player executor, String[] args);
}
