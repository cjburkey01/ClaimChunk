package com.cjburkey.claimchunk.cmds;

import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;

public class CmdClaim implements ICommand {

	public String getCommand() {
		return "claim";
	}

	public String getDescription() {
		return "Claim the chunk you're standing in.";
	}

	public String[] getPermittedArguments() {
		return new String[] {  };
	}

	public int getRequiredArguments() {
		return 0;
	}

	public void onCall(Player executor, String[] args) {
		MainHandler.claimChunk(executor);
	}
	
}