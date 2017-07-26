package com.cjburkey.claimchunk.cmds;

import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;

public class CmdAccess implements ICommand {

	public String getCommand() {
		return "access";
	}

	public String getDescription() {
		return "Toggle access for a player in your claimed territory.";
	}

	public String[] getPermittedArguments() {
		return new String[] { "player" };
	}

	public int getRequiredArguments() {
		return 1;
	}

	public void onCall(Player executor, String[] args) {
		MainHandler.accessChunk(executor, args);
	}
	
}