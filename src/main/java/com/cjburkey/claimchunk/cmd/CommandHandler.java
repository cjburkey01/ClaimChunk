package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final Queue<ICommand> cmds = new ConcurrentLinkedQueue<>();

    void registerCommand(Class<? extends ICommand> cls) {
        try {
            ICommand cmd = cls.newInstance();
            if (cmd != null && cmd.getCommand() != null && !cmd.getCommand().trim().isEmpty()
                    && !hasCommand(cmd.getCommand())) {
                cmds.add(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ICommand[] getCmds() {
        return cmds.toArray(new ICommand[0]);
    }

    private boolean hasCommand(String name) {
        return getCommand(name) != null;
    }

    public ICommand getCommand(String name) {
        for (ICommand c : cmds) {
            if (c.getCommand().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        runCommands(label.toLowerCase(), sender, args);
        return true;
    }

    private void runCommands(String cmdBase, CommandSender sender, String[] suppliedArguments) {
        if (!(sender instanceof Player)) {
            Utils.msg(sender, "Only in-game players may use ClaimChunk");
            return;
        }
        Player player = (Player) sender;
        if (!Utils.hasPerm(sender, true, "base")) {
            Utils.toPlayer(player, Config.errorColor(), Utils.getMsg("noPluginPerm"));
        }
        if (suppliedArguments.length < 1) {
            displayHelp(cmdBase, player);
            return;
        }
        String name = suppliedArguments[0];
        List<String> outArgs = new ArrayList<>(Arrays.asList(suppliedArguments).subList(1, suppliedArguments.length));
        ICommand cmd = getCommand(name);
        if (cmd == null) {
            displayHelp(cmdBase, player);
            return;
        }
        if (outArgs.size() < cmd.getRequiredArguments() || outArgs.size() > cmd.getPermittedArguments().length) {
            displayUsage(cmdBase, player, cmd);
            return;
        }
        boolean success = cmd.onCall(cmdBase, player, outArgs.toArray(new String[0]));
        if (!success) {
            displayUsage(cmdBase, player, cmd);
        }
    }

    private void displayHelp(String cmdUsed, Player ply) {
        Utils.msg(ply, String.format("%sInvalid command. See: %s/%s help",
                Config.errorColor(),
                Config.infoColor(),
                cmdUsed));
    }

    private void displayUsage(String cmdUsed, Player ply, ICommand cmd) {
        Utils.msg(ply, String.format("%sUsage: %s/%s %s %s",
                Config.errorColor(),
                cmdUsed,
                Config.infoColor(),
                cmd.getCommand(),
                getUsageArgs(cmd)));
    }

    public String getUsageArgs(ICommand cmd) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cmd.getPermittedArguments().length; i++) {
            boolean req = (i < cmd.getRequiredArguments());
            out.append(req ? '<' : '[');
            out.append(cmd.getPermittedArguments()[i].getArgument());
            out.append(req ? '>' : ']');
            out.append(' ');
        }
        return out.toString().trim();
    }

}
