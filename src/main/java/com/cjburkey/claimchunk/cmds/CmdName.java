package com.cjburkey.claimchunk.cmds;

import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.player.PlayerHandler;

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
		PlayerHandler nh = ClaimChunk.getInstance().getPlayerHandler();
		try {
			if (args.length == 0) {
				if (nh.hasChunkName(executor.getUniqueId())) {
					nh.clearChunkName(executor.getUniqueId());
					Utils.toPlayer(executor, Config.getColor("successColor"), Utils.getMsg("nameClear"));
				} else {
					Utils.toPlayer(executor, Config.getColor("errorColor"), Utils.getMsg("nameNotSet"));
				}
			} else {
				nh.setChunkName(executor.getUniqueId(), args[0].trim());
				Utils.toPlayer(executor, Config.getColor("successColor"), Utils.getMsg("nameSet").replaceAll("%%NAME%%", args[0].trim()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.msg(executor, "&4&lAn error occurred, please contact an admin.");
		}
		return true;
	}
	
}