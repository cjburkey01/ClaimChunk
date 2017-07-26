package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.cmds.CmdAccess;
import com.cjburkey.claimchunk.cmds.CmdClaim;
import com.cjburkey.claimchunk.cmds.CmdUnclaim;

public class Commands {
	
	public void register(CommandHandler cmd) {
		cmd.registerCommand(CmdClaim.class);
		cmd.registerCommand(CmdUnclaim.class);
		cmd.registerCommand(CmdAccess.class);
	}
	
}