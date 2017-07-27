package com.cjburkey.claimchunk.cmds;

import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.player.PlayerCustomNames;

public class CmdName implements ICommand {

	public String getCommand() {
		return "name";
	}

	public String getDescription() {
		return "Change the name that appears when someone enters your land.";
	}

	public Argument[] getPermittedArguments() {
		return new Argument[] { new Argument("newName", Argument.TabCompletion.NONE) };
	}

	public int getRequiredArguments() {
		return 0;
	}

	public boolean onCall(Player executor, String[] args) {
		PlayerCustomNames nh = ClaimChunk.getInstance().getCustomNames();
		if (args.length == 0) {
			if (nh.hasCustomName(executor.getUniqueId())) {
				nh.resetName(executor.getUniqueId());
				Utils.toPlayer(executor, Utils.getConfigColor("successColor"), Utils.getLang("NameClear"));
			} else {
				Utils.toPlayer(executor, Utils.getConfigColor("errorColor"), Utils.getLang("NoNameClear"));
			}
		} else {
			nh.setName(executor.getUniqueId(), args[0].trim());
			Utils.toPlayer(executor, Utils.getConfigColor("successColor"), Utils.getLang("NameSet").replaceAll("%%NAME%%", args[0].trim()));
		}
		return true;
	}
	
}