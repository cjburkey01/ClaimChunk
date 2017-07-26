package com.cjburkey.claimchunk.oldcmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OLDCmdAccessChunks implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//MainHandler.accessChunk(sender, args);
		return true;
	}
	
}