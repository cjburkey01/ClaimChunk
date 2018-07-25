package com.cjburkey.claimchunk.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import com.cjburkey.claimchunk.Utils;

public class DataPlayer {
	
	public UUID player;
	public String lastIgn;
	public List<UUID> permitted = new ArrayList<>();
	public String chunkName;
	public long lastJoinTime;
	public boolean unclaimedAllChunks;
	
	private DataPlayer() {
	}
	
	public DataPlayer(Player player, UUID... permitted) {
		this.player = player.getUniqueId();
		lastIgn = player.getName();
		for (UUID id : permitted) {
			this.permitted.add(id);
		}
		chunkName = null;
	}
	
	public DataPlayer(UUID id, String name) {
		this.player = id;
		this.lastIgn = name;
		this.permitted.clear();
		chunkName = null;
	}
	
	public void onJoin() {
		Utils.log("Player joined: " + player);
		lastJoinTime = System.currentTimeMillis();
	}
	
	public DataPlayer clone() {
		DataPlayer ret = new DataPlayer();
		ret.player = player;
		ret.lastIgn = lastIgn;
		ret.permitted = permitted;
		ret.chunkName = chunkName;
		return ret;
	}
	
}