package com.cjburkey.claimchunk.cmd;

import org.bukkit.entity.Player;

public interface ICommand {
	
	String getCommand();
	String getDescription();
	Argument[] getPermittedArguments();
	int getRequiredArguments();
	void onCall(Player executor, String[] args);
	
}