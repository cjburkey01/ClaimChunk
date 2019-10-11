package com.cjburkey.claimchunk.cmds;

import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdHelp implements ICommand {

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return ClaimChunk.getInstance().getMessages().cmdHelp;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
    }

    public String getPermissionMessage() {
        return ClaimChunk.getInstance().getMessages().noPluginPerm;
    }

    @Override
    public Argument[] getPermittedArguments() {
        return new Argument[] {new Argument("command", Argument.TabCompletion.COMMAND)};
    }

    @Override
    public int getRequiredArguments() {
        return 0;
    }

    @Override
    public boolean onCall(String cmdUsed, Player executor, String[] args) {
        if (args.length == 0) {
            Utils.msg(executor, String.format("%s&l--- [ %s ] ---", Config.infoColor(), ClaimChunk.getInstance().getMessages().helpTitle));
            for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
                if (cmd.getShouldDisplayInHelp(executor)) {
                    String out = (String.format("%s/%s %s %s", Config.infoColor(), cmdUsed, cmd.getCommand(),
                            ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd)));
                    Utils.msg(executor, out);
                    Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
                }
            }
        } else {
            ICommand cmd = ClaimChunk.getInstance().getCommandHandler().getCommand(args[0]);
            if (cmd != null) {
                Utils.msg(executor, String.format("%s&l--- [ %s ] ---", Config.infoColor(), ClaimChunk.getInstance().getMessages().helpCommandTitle
                        .replace("%%CMD%%", String.format("/%s %s", cmdUsed, args[0]))));
                String out = (String.format("%s/%s %s %s", Config.infoColor(), cmdUsed, cmd.getCommand(),
                        ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd)));
                Utils.msg(executor, out);
                Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
            } else {
                Utils.msg(executor, Config.errorColor() + "Command " + Config.infoColor() + "'' " + Config.errorColor() + "not found.");
            }
        }
        return true;
    }

}
