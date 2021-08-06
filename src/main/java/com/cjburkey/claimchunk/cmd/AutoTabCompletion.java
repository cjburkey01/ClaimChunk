package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AutoTabCompletion implements TabCompleter {

    private final ClaimChunk claimChunk;

    public AutoTabCompletion(ClaimChunk claimChunk) {
        this.claimChunk = claimChunk;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      String[] args) {
        // There aren't any claim chunk commands present, return a list of them
        if (args.length < 1) return getCommands("");

        // The user is typing a command, return a list of commands
        if (args.length == 1) return getCommands(args[0]);

        // Get the command that the user has typed.
        ICommand cmd = claimChunk.getCommandHandler().getCommand(args[0]);

        // If the command isn't valid, there aren't any valid arguments.
        if (cmd == null) return new ArrayList<>();

        // Get the index of the command argument
        int cmdArg = args.length - 2;

        if (cmdArg < cmd.getPermittedArguments(claimChunk).length) {
            // If the user hasn't typed in all the arguments that they can for
            // this command, get the current argument they're typing
            Argument arg = cmd.getPermittedArguments(claimChunk)[cmdArg];

            switch (arg.getCompletion()) {
                case COMMAND:
                    // Return commands
                    return getCommands(args[args.length - 1]);
                case ONLINE_PLAYER:
                    // Return all online players
                    return getOnlinePlayers(args[args.length - 1]);
                case OFFLINE_PLAYER:
                    // Return all players
                    return getOfflinePlayers(args[args.length - 1]);
                case BOOLEAN:
                    // Return a boolean value
                    return Arrays.asList("true", "false");
                default:
                    // Return an empty list because it's an invalid/none tab completion
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
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

    private List<String> getCommands(String starts) {
        List<String> out = new ArrayList<>();
        // Loop through all commands
        for (ICommand cmd : claimChunk.getCommandHandler().getCmds()) {
            String add = cmd.getCommand(claimChunk);
            if (add.toLowerCase().startsWith(starts.toLowerCase())) {
                // Add commands that start with the same letters as the letters
                // typed in by the player.
                out.add(cmd.getCommand(claimChunk));
            }
        }
        return out;
    }

    private List<String> getOfflinePlayers(String starts) {
        // Return a list of all players that have joined the server
        return claimChunk.getPlayerHandler().getJoinedPlayersFromName(starts);
    }

}
