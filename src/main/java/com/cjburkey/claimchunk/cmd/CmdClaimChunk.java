package com.cjburkey.claimchunk.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.title.TitleHandler;

public final class CmdClaimChunk implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 1) {
				try {
					TitleHandler.showTitle((Player) sender, args[0], ChatColor.GREEN, 10, 100, 20);
					TitleHandler.showSubTitle((Player) sender, args[0], ChatColor.AQUA, 10, 100, 20);
					TitleHandler.showActionbarTitle((Player) sender, args[0], ChatColor.GOLD, 10, 100, 20);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Utils.msg(sender, "&4Add an arg!");
			}
		} else {
			Utils.msg(sender, "&4Only ingame players may use /claimchunk");
		}
		return true;
	}
	
}