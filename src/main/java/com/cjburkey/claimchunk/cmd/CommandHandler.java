package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    // Concurrent list of all commands
    private final HashSet<ICommand> cmds = new HashSet<>();

    public void registerCommand(Class<? extends ICommand> cls) {
        try {
            // Create an instance of the provided command class
            ICommand cmd = cls.getDeclaredConstructor().newInstance();

            // If the command doesn't exist within the set yet, add it
            if (cmd.getCommand() != null && !cmd.getCommand().trim().isEmpty() && !hasCommand(cmd.getCommand())) {
                cmds.add(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all of the commands present in this command handler.
     *
     * @return An array with all of the commands within this command handler.
     */
    public ICommand[] getCmds() {
        synchronized (cmds) {
            return cmds.toArray(new ICommand[0]);
        }
    }

    /**
     * Get the object representation of this command by its name.
     *
     * @param name The name of the command to locate.
     * @return The object representation of the command by this name.
     */
    public ICommand getCommand(@Nonnull String name) {
        synchronized (cmds) {
            // Loop through all commands
            for (ICommand c : cmds) {
                // If the command shares a (case-insensitive) name, this is the command
                if (c.getCommand().equalsIgnoreCase(name)) {
                    return c;
                }
            }

            // Command not found
            return null;
        }
    }

    /**
     * Check if this command handler has a command by the provided name.
     *
     * @param name The name of the command.
     * @return Whether this handler has this command.
     */
    public boolean hasCommand(String name) {
        // If the command isn't found, it doesn't exist
        return getCommand(name) != null;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        runCommands(label.toLowerCase(), sender, args);
        return true;
    }

    private void runCommands(String cmdBase, CommandSender sender, String[] suppliedArguments) {
        // Currently, only in-game players are able to use any ClaimChunk commands.
        // TODO: CHANGE THIS TO A SYSTEM ON A PER-COMMAND BASIS!
        if (!(sender instanceof Player)) {
            Utils.msg(sender, ClaimChunk.getInstance().getMessages().ingameOnly);
            return;
        }

        Player player = (Player) sender;

        // Check if the player has the base permission
        if (!Utils.hasPerm(sender, true, "base")) {
            Utils.toPlayer(player, ClaimChunk.getInstance().getMessages().noPluginPerm);
            return;
        }

        // Make sure the player provided a subcommand
        if (suppliedArguments.length < 1) {
            displayHelp(cmdBase, player);
            return;
        }

        // Get the subcommand name
        String name = suppliedArguments[0];

        // Create a list of arguments that should be passed into the command
        List<String> outArgs = new ArrayList<>(Arrays.asList(suppliedArguments).subList(1, suppliedArguments.length));

        // Get the command by the provided name
        ICommand cmd = getCommand(name);

        // Make sure the command exists
        if (cmd == null) {
            displayHelp(cmdBase, player);
            return;
        }

        // Make sure the executor has permission to use this command
        if (!cmd.hasPermission(sender)) {
            Utils.msg(sender, cmd.getPermissionMessage());
            return;
        }

        // Make sure the player provided the correct number of arguments
        if (outArgs.size() < cmd.getRequiredArguments() || outArgs.size() > cmd.getPermittedArguments().length) {
            displayUsage(cmdBase, player, cmd);
            return;
        }

        // Check if the command executed successfully
        boolean success = cmd.onCall(cmdBase, player, outArgs.toArray(new String[0]));

        // If the command didn't execute correctly, the usage for this command
        // should be displayed
        if (!success) {
            displayUsage(cmdBase, player, cmd);
        }
    }

    private void displayHelp(String cmdUsed, Player ply) {
        // Display help for ClaimChunk
        Utils.msg(ply, ClaimChunk.getInstance().getMessages().invalidCommand.replace("%%CMD%%", cmdUsed));
    }

    private void displayUsage(String cmdUsed, Player ply, ICommand cmd) {
        // Display usage for a specific command
        Utils.msg(ply, ClaimChunk.getInstance().getMessages().errorDisplayUsage
                                .replace("%%CMD%%", cmdUsed)
                                .replace("%%SUB_CMD%%", cmd.getCommand())
                                .replace("%%ARGS%%", getUsageArgs(cmd)));
    }

    public String getUsageArgs(ICommand cmd) {
        // Create an empty StringBuilder to build the output string
        StringBuilder out = new StringBuilder();

        // Loop through all of the permitted arguments
        for (int i = 0; i < cmd.getPermittedArguments().length; i++) {
            // Check if this argument is required
            boolean req = (i < cmd.getRequiredArguments());

            // Add the command wrapper start
            out.append(req ? '<' : '[');

            // Add the argument name
            out.append(cmd.getPermittedArguments()[i].getArgument());

            // Add the command wrapper end
            out.append(req ? '>' : ']');

            // Add ending whitespace
            out.append(' ');
        }

        // Return the output
        return out.toString().trim();
    }

}
