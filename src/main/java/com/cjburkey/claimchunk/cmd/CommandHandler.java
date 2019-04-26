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
                Utils.debug(" Registered cmd: %s - %s", cmd.getCommand(), cmd.getDescription());
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
        runCommands(sender, args);
        return true;
    }

    private void runCommands(CommandSender sender, String[] suppliedArguments) {
        if (!(sender instanceof Player)) {
            Utils.msg(sender, "Only in-game players may use ClaimChunk");
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("claimchunk.base")) {
            Utils.toPlayer(player, false, Config.getColor("errorColor"), Utils.getMsg("noPluginPerm"));
        }
        if (suppliedArguments.length < 1) {
            displayHelp(player);
            return;
        }
        String name = suppliedArguments[0];
        List<String> outArgs = new ArrayList<>(Arrays.asList(suppliedArguments).subList(1, suppliedArguments.length));
        ICommand cmd = getCommand(name);
        if (cmd == null) {
            displayHelp(player);
            return;
        }
        if (outArgs.size() < cmd.getRequiredArguments() || outArgs.size() > cmd.getPermittedArguments().length) {
            displayUsage(player, cmd);
            return;
        }
        boolean success = cmd.onCall(player, outArgs.toArray(new String[0]));
        if (!success) {
            displayUsage(player, cmd);
        }
    }

    private void displayHelp(Player ply) {
        Utils.msg(ply, Config.getColor("errorColor") + "Invalid command. See: " + Config.getColor("infoColor")
                + "/chunk help");
    }

    private void displayUsage(Player ply, ICommand cmd) {
        final String out = String.format("%sUsage: %s/chunk %s %s",
                Config.getColor("errorColor"),
                Config.getColor("infoColor"),
                cmd.getCommand(),
                getUsageArgs(cmd));
        Utils.msg(ply, out);
    }

    public String getUsageArgs(ICommand cmd) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cmd.getPermittedArguments().length; i++) {
            boolean req = i < cmd.getRequiredArguments();
            out.append((req) ? '<' : '[');
            out.append(cmd.getPermittedArguments()[i].getArgument());
            out.append((req) ? '>' : ']');
            out.append(' ');
        }
        return out.toString().trim();
    }

}
