package com.cjburkey.claimchunk.cmd;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Econ;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;

public class CmdUnclaimChunk implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Utils.msg(sender, "&4Only ingame players may use /unclaimchunk");
			return true;
		}
		Player p = (Player) sender;
		if (!Utils.hasPerm(p, "claimchunk.unclaim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NoPermToUnclaim"));
			return true;
		}
		ChunkHandler ch = ClaimChunk.getInstance().getChunks();
		Chunk loc = p.getLocation().getChunk();
		if(!ch.isClaimed(loc.getWorld(), loc.getX(), loc.getZ())) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("ChunkAlreadyNotClaimed"));
			return true;
		}
		if(!ch.isOwner(loc.getWorld(), loc.getX(), loc.getZ(), p)) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NotYourChunk"));
			return true;
		}
		if (ClaimChunk.getInstance().useEconomy()) {
			Econ e = ClaimChunk.getInstance().getEconomy();
			double reward = ClaimChunk.getInstance().getConfig().getDouble("unclaimReward");
			if (reward > 0) {
				e.addMoney(p.getUniqueId(), reward);
				Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("UnclaimReward").replaceAll("%%AMT%%", e.format(reward)));
			}
		}
		ch.unclaimChunk(loc.getWorld(), loc.getX(), loc.getZ());
		Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getLang("ChunkUnclaimed"));
		return true;
	}
	
}