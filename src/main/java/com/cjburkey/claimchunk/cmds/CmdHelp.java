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
		return "Display ClaimChunk help";
	}

	public Argument[] getPermittedArguments() {
		return new Argument[] {  };
	}

	public int getRequiredArguments() {
		return 0;
	}

	public boolean onCall(Player executor, String[] args) {
		Utils.msg(executor, Utils.getConfigColor("infoColor") + "&l---[ ClaimChunk Help ] ---");
		for (ICommand cmd : ClaimChunk.getInstance().getCommandHandler().getCmds()) {
			StringBuilder out = new StringBuilder();
			out.append(Utils.getConfigColor("infoColor") + "/chunk ");
			out.append(cmd.getCommand());
			out.append(ClaimChunk.getInstance().getCommandHandler().getUsageArgs(cmd));
			Utils.msg(executor, out.toString());
			Utils.msg(executor, "  " + ChatColor.RED + cmd.getDescription());
		}
		return true;
	}
	
}