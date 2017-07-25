package com.cjburkey.claimchunk.cmd;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.ClaimChunk;
import com.cjburkey.claimchunk.Utils;
import com.cjburkey.claimchunk.chunk.ChunkHandler;

public final class CmdClaimChunk implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Utils.msg(sender, "&4Only ingame players may use /claimchunk");
			return true;
		}
		Player p = (Player) sender;
		if (!Utils.hasPerm(p, "claimchunk.claim")) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("NoPermToClaim"));
			return true;
		}
		ChunkHandler ch = ClaimChunk.getInstance().getChunks();
		Chunk loc = p.getLocation().getChunk();
		if(ch.isClaimed(loc.getX(), loc.getZ())) {
			Utils.toPlayer(p, Utils.getConfigColor("errorColor"), Utils.getLang("ChunkAlreadyOwned"));
			return true;
		}
		ch.claimChunk(loc.getX(), loc.getZ(), p);
		Utils.toPlayer(p, Utils.getConfigColor("successColor"), Utils.getLang("ChunkClaimed"));
		return true;
	}
	
}