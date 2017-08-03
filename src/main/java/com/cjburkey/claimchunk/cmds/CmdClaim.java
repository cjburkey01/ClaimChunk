package com.cjburkey.claimchunk.cmds;

import java.io.IOException;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;
import com.cjburkey.claimchunk.cmd.MainHandler;

public class CmdClaim implements ICommand {

	public String getCommand() {
		return "claim";
	}

	public String getDescription() {
		return "Claim the chunk you're standing in.";
	}

	public Argument[] getPermittedArguments() {
		return new Argument[] {  };
	}

	public int getRequiredArguments() {
		return 0;
	}

	public boolean onCall(Player executor, String[] args) {
		try {
			MainHandler.claimChunk(executor);
		} catch (IOException e) {
			e.printStackTrace();
			Utils.msg(executor, Config.getColor("errorColor") + "There was an error while claiming that chunk.");
		}
		return true;
	}
	
}