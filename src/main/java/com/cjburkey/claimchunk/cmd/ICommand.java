package com.cjburkey.claimchunk.cmd;

import org.bukkit.entity.Player;

public interface ICommand {
	
	String getCommand();
	String getDescription();
	String[] getPermittedArguments();
	int getRequiredArguments();
	void onCall(Player executor, String[] args);
	
}