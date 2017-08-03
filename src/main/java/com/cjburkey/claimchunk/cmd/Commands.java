package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.cmds.CmdAccess;
import com.cjburkey.claimchunk.cmds.CmdAuto;
import com.cjburkey.claimchunk.cmds.CmdClaim;
import com.cjburkey.claimchunk.cmds.CmdHelp;
import com.cjburkey.claimchunk.cmds.CmdName;
import com.cjburkey.claimchunk.cmds.CmdShow;
import com.cjburkey.claimchunk.cmds.CmdUnclaim;

public class Commands {
	
	public void register(CommandHandler cmd) {
		cmd.registerCommand(CmdHelp.class);
		cmd.registerCommand(CmdClaim.class);
		cmd.registerCommand(CmdAuto.class);
		cmd.registerCommand(CmdUnclaim.class);
		cmd.registerCommand(CmdAccess.class);
		cmd.registerCommand(CmdName.class);
		cmd.registerCommand(CmdShow.class);
	}
	
}