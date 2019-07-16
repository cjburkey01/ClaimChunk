package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.ClaimChunk;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class AutoTabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, String[] args) {
        if (args.length < 1) return getCommands("");
        if (args.length == 1) return getCommands(args[0]);
        ICommand cmd = ClaimChunk.getInstance().getCommandHandler().getCommand(args[0]);
        if (cmd == null) return new ArrayList<>();
        int cmdArg = args.length - 2;
        if (cmdArg < cmd.getPermittedArguments().length) {
            Argument arg = cmd.getPermittedArguments()[cmdArg];
            switch (arg.getCompletion()) {
                case COMMAND:
                    return getCommands(args[args.length - 1]);
                case ONLINE_PLAYER:
                    return getOnlinePlayers(args[args.length - 1]);
                case OFFLINE_PLAYER:
                    return getOfflinePlayers(args[args.length - 1]);
                case BOOLEAN:
                    return Arrays.asList("true", "false");
                default:
                    return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    private List<String> getOnlinePlayers(String starts) {
        List<String> out = new ArrayList<>();
        for (Player p : ClaimChunk.getInstance().getServer().getOnlinePlayers()) {
            String add = p.getName();
            if (add.toLowerCase().startsWith(starts.toLowerCase())) {
                out.add(p.getName());
            }
        }
        return out;
    }

    private List<String> getCommands(String starts) {
        List<String> out = new ArrayList<>();
        for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
            String add = cmd.getCommand();
            if (add.toLowerCase().startsWith(starts.toLowerCase())) {
                out.add(cmd.getCommand());
            }
        }
        return out;
    }

    private List<String> getOfflinePlayers(String starts) {
        return ClaimChunk.getInstance().getPlayerHandler().getJoinedPlayers(starts);
    }

}
