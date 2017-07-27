package com.cjburkey.claimchunk.cmds;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

public class CmdHelp implements ICommand {

	public String getCommand() {
		return "help";
	}

	public String getDescription() {
		return "Display ClaimChunk help (for [command], if supplied)";
	}

	public Argument[] getPermittedArguments() {
		return new Argument[] { new Argument("command", Argument.TabCompletion.COMMAND) };
	}

	public int getRequiredArguments() {
		return 0;
	}

	public boolean onCall(Player executor, String[] args) {
		if (args.length == 0) {
			Utils.msg(executor, Utils.getConfigColor("infoColor") + "&l---[ ClaimChunk Help ] ---");
			for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
				StringBuilder out = new StringBuilder();
				out.append(Utils.getConfigColor("infoColor") + "/chunk ");
				out.append(cmd.getCommand());
				out.append(ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd));
				Utils.msg(executor, out.toString());
				Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
			}
		} else {
			ICommand cmd = ClaimChunk.getInstance().getCommandHandler().getCommand(args[0]);
			if (cmd != null) {
				Utils.msg(executor, Utils.getConfigColor("infoColor") + "&l---[ /chunk " + args[0] + " Help ] ---");
				StringBuilder out = new StringBuilder();
				out.append(Utils.getConfigColor("infoColor") + "/chunk ");
				out.append(cmd.getCommand());
				out.append(ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd));
				Utils.msg(executor, out.toString());
				Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
			} else {
				Utils.msg(executor, Utils.getConfigColor("errorColor") + "Command " + Utils.getConfigColor("infoColor") + "'' " + Utils.getConfigColor("errorColor") + "not found.");
			}
		}
		return true;
	}
	
}