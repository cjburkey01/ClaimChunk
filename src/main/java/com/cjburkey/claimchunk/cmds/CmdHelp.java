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
        return "Display ClaimChunk help (for [command], if supplied)";
    }

    @Override
    public boolean getShouldDisplayInHelp(CommandSender sender) {
        return Utils.hasPerm(sender, true, "base");
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
    public boolean onCall(Player executor, String[] args) {
        if (args.length == 0) {
            Utils.msg(executor, Config.getColor("infoColor") + "&l---[ ClaimChunk Help ] ---");
            for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
                if (cmd.getShouldDisplayInHelp(executor)) {
                    String out = (Config.getColor("infoColor") + "/chunk ")
                            + cmd.getCommand()
                            + ' '
                            + ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd);
                    Utils.msg(executor, out);
                    Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
                }
            }
        } else {
            ICommand cmd = ClaimChunk.getInstance().getCommandHandler().getCommand(args[0]);
            if (cmd != null) {
                Utils.msg(executor, Config.getColor("infoColor") + "&l---[ /chunk " + args[0] + " Help ] ---");
                String out = (Config.getColor("infoColor") + "/chunk ")
                        + cmd.getCommand()
                        + ' '
                        + ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd);
                Utils.msg(executor, out);
                Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
            } else {
                Utils.msg(executor, Config.getColor("errorColor") + "Command " + Config.getColor("infoColor") + "'' "
                        + Config.getColor("errorColor") + "not found.");
            }
        }
        return true;
    }

}
