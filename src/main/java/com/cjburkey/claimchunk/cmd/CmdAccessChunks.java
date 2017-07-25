package com.cjburkey.claimchunk.cmd;

import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;

public class CmdAccessChunks implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Utils.msg(sender, "&4Only ingame players may use /accesschunks");
			return true;
		}
		Player p = (Player) sender;
		if (args.length != 1) {
			Utils.msg(sender, Utils.getConfigColor("errorColor") + Utils.getLang("AccessArgUsage"));
			Utils.msg(sender, Utils.getConfigColor("errorColor") + "  " + Utils.getLang("AccessHelp"));
			return true;
		}
		Player other = ClaimChunk.getInstance().getServer().getPlayer(args[0]);
		if (other != null) {
			toggle(p, other.getUniqueId(), other.getName());
			return true;
		} else {
			UUID otherId = ClaimChunk.getInstance().getPlayers().getUuid(args[0]);
			if (otherId == null) {
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("PlayerNotFound"));
				return true;
			}
			toggle(p, otherId, args[0]);
		}
		return true;
	}
	
	private void toggle(Player owner, UUID other, String otherName) {
		if (owner.getUniqueId().equals(other)) {
			Utils.toPlayer(owner, Utils.getConfigColor("errorColor"), Utils.getLang("NotYourself"));
			return;
		}
		boolean hasAccess = ClaimChunk.getInstance().getAccess().toggleAccess(owner.getUniqueId(), other);
		if (hasAccess) {
			Utils.toPlayer(owner, Utils.getConfigColor("successColor"), Utils.getLang("HasAccess").replaceAll("%%PLAYER%%", otherName));
			return;
		}
		Utils.toPlayer(owner, Utils.getConfigColor("successColor"), Utils.getLang("NoLongerHasAccess").replaceAll("%%PLAYER%%", otherName));
	}
	
}