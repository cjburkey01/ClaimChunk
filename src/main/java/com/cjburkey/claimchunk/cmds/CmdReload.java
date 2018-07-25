package com.cjburkey.claimchunk.cmds;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Config;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.cmd.Argument;
import com.cjburkey.claimchunk.cmd.ICommand;

public class CmdReload implements ICommand {
	
	public String getCommand() {
		return "reload";
	}
	
	public String getDescription() {
		return "Reload the config for ClaimChunk";
	}
	
	public Argument[] getPermittedArguments() {
		return new Argument[0];
	}
	
	public int getRequiredArguments() {
		return 0;
	}
	
	public boolean onCall(Player executor, String[] args) {
		if (!executor.hasPermission("claimchunk.admin")) {
			Utils.toPlayer(executor, ChatColor.RED, Config.getString("messages", "reloadNoPerm"));
			return true;
		}
		PluginManager pluginManager = ClaimChunk.getInstance().getServer().getPluginManager();
		ClaimChunk.getInstance().reloadConfig();
		pluginManager.disablePlugin(ClaimChunk.getInstance());
		pluginManager.enablePlugin(ClaimChunk.getInstance());
		return true;
	}
	
}