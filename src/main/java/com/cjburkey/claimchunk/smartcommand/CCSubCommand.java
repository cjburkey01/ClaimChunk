package com.cjburkey.claimchunk.smartcommand;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;

import de.goldmensch.commanddispatcher.Executor;
import de.goldmensch.commanddispatcher.subcommand.SmartSubCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A wrapper around Goldmensch's smart subcommand to include more information and specific handling
 * procedures.
 *
 * @since 0.0.23
 */
public abstract class CCSubCommand extends SmartSubCommand implements TabCompleter {

    protected final ClaimChunk claimChunk;

    public CCSubCommand(ClaimChunk claimChunk, Executor executorLevel) {
        super(executorLevel, "");

        this.claimChunk = claimChunk;
    }

    /**
     * Get the description for this subcommand.
     *
     * @return The description for this subcommand.
     */
    public abstract @NotNull Optional<String> getDescription();

    /**
     * Check whether the provided executor has permission to execute this subcommand.
     *
     * @param executor This subcommand's executor.
     * @return Whether this player has access to this subcommand.
     */
    public abstract boolean hasPermission(@Nullable CommandSender executor);

    /**
     * Get the message to be displayed when users don't have permission to use this subcommand.
     *
     * @return The lacking permissions message.
     */
    public abstract @NotNull String getPermissionMessage();

    /**
     * Get whether this command should be displayed in the help subcommand list for the provided
     * executor.
     *
     * @return Whether this command will be displayed within the help list.
     */
    public boolean getShouldDisplayInHelp(@NotNull CommandSender sender) {
        return (getExecutor() == Executor.CONSOLE_PLAYER
                        || Executor.fromSender(sender) == getExecutor())
                && hasPermission(sender);
    }

    /**
     * Get a list of all arguments that could be provided to this subcommand.
     *
     * @return An array with all the possible arguments.
     */
    public abstract CCArg[] getPermittedArguments();

    /**
     * Get the maximum number of arguments (separated by spaces) that may be passed into this
     * command. Normally, this will just be the number of arguments given in {@link
     * CCSubCommand#getPermittedArguments()}.
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
     * @param cmdUsed The version of the base command used (like {@code /chunk} or {@code
     *     /claimchunk}).
     * @param executor The subcommand executor.
     * @param args The raw string arguments passed by the executor.
     * @return Whether this subcommand's usage should be displayed.
     */
    public abstract boolean onCall(
            @NotNull String cmdUsed, @NotNull CommandSender executor, @NotNull String[] args);

    /**
     * Sends a message to a CommandSender, which may be the console or a player.
     *
     * @param sender The message recipient.
     * @param msg The message with formatting placeholders.
     * @param arguments Placeholder expansion values.
     */
    protected final void messageChat(
            @NotNull CommandSender sender, @NotNull String msg, @NotNull Object... arguments) {
        Utils.msg(sender, msg.formatted(arguments));
    }

    /**
     * Sends a message to a CommandSender, which may be the console or a player.
     *
     * @param sender The message recipient.
     * @param msg The message with formatting placeholders.
     * @param arguments Placeholder expansion values.
     */
    protected final void messagePly(
            @NotNull Player sender, @NotNull String msg, @NotNull Object... arguments) {
        Utils.toPlayer(sender, msg.formatted(arguments));
    }

    // TODO: USE THIS IN PLACES WHERE IT COULD BE COOL?
    /**
     * Send a message in chat to the console OR to the player via titles if possible.
     *
     * @param sender The message recipient.
     * @param msg The message with formatting placeholders.
     * @param arguments Placeholder expansion values.
     */
    @SuppressWarnings("unused")
    protected final void msgTo(
            @NotNull CommandSender sender, @NotNull String msg, @NotNull Object... arguments) {
        if (sender instanceof Player player) {
            messagePly(player, msg, arguments);
        } else {
            messageChat(sender, msg, arguments);
        }
    }

    @Override
    public final boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        // TODO: REMOVE PERMISSION CHECK HERE
        // Make sure the executor has permission to use this command
        if (!hasPermission(sender)) {
            messageChat(sender, getPermissionMessage());
            return true;
        }

        // TODO: WAITING ON UPDATE TO COMMAND DISPATCHER
        // Make sure the player provided the correct number of arguments
        if (args.length < getRequiredArguments() || args.length > getMaxArguments()) {
            displayUsage(label, sender);
            return true;
        }

        // Check if the command executed successfully.
        // If the command didn't execute correctly, the usage for this command
        // should be displayed
        if (!onCall(label, sender, args)) {
            displayUsage(label, sender);
        }

        return true;
    }

    private void displayUsage(String cmdUsed, CommandSender sender) {
        // Display usage for a specific command
        messageChat(
                sender,
                claimChunk
                        .getMessages()
                        .errorDisplayUsage
                        .replace("%%CMD%%", cmdUsed)
                        .replace("%%SUB_CMD%%", getName())
                        .replace("%%ARGS%%", getUsageArgs()));
    }

    public String getUsageArgs() {
        // Create an empty StringBuilder to build the output string
        StringBuilder out = new StringBuilder();

        // Loop through all of the permitted arguments
        for (int i = 0; i < getPermittedArguments().length; i++) {
            var arg = getPermittedArguments()[i];

            // Check if this argument is required
            boolean req = (i < getRequiredArguments());

            // Add the command wrapper start
            out.append(req ? '<' : '[');

            // Add the argument name
            out.append(arg.arg);

            // Add little extra info for arguments
            if (arg.tab == CCAutoComplete.BOOLEAN) {
                out.append("{true/false}");
            } else if (arg.tab == CCAutoComplete.OFFLINE_PLAYER
                    || arg.tab == CCAutoComplete.ONLINE_PLAYER) {
                out.append("{Player}");
            }

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
    public final List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        int argNum = args.length;
        String partialArg = (args.length > 0) ? args[args.length - 1] : "";
        if (argNum < getPermittedArguments().length) {
            return switch (getPermittedArguments()[argNum].tab) {
                case ONLINE_PLAYER ->
                // Return all online players
                getOnlinePlayers(partialArg);
                case OFFLINE_PLAYER ->
                // Return all players
                getOfflinePlayers(partialArg);
                case BOOLEAN ->
                // Return a boolean value
                Arrays.asList("true", "false");
                default ->
                // Return an empty list because it's an invalid/none tab completion
                Collections.emptyList();
            };
        }

        return Collections.emptyList();
    }

    private List<String> getOnlinePlayers(String starts) {
        List<String> out = new ArrayList<>();
        // Loop through all players
        for (Player p : claimChunk.getServer().getOnlinePlayers()) {
            String add = p.getName();
            if (add.toLowerCase().startsWith(starts.toLowerCase())) {
                // Add player names that start with the same letters as the
                // letters typed in by the player
                out.add(p.getName());
            }
        }
        return out;
    }

    private List<String> getOfflinePlayers(String starts) {
        // Return a list of all players that have joined the server
        return claimChunk.getPlayerHandler().getJoinedPlayersFromName(starts);
    }

    /**
     * Replacement for the old argument class.
     *
     * @since 0.0.23
     */
    public record CCArg(String arg, CCAutoComplete tab) {}

    /**
     * The type of tab completion for a given argument.
     *
     * @since 0.0.23
     */
    public enum CCAutoComplete {

        /** No tab completion should occur. */
        NONE,

        /** Tab completion should include all online players. */
        ONLINE_PLAYER,

        /** Tab completion should include all players that have joined the server. */
        OFFLINE_PLAYER,

        /** Tab completion should be either `true` or `false`. */
        BOOLEAN,
    }
}
