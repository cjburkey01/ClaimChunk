package com.cjburkey.claimchunk.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class DataPlayer {
	
	public UUID player;
	public String lastIgn;
	public List<UUID> permitted = new ArrayList<>();
	public String chunkName;
	
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
	
	public DataPlayer clone() {
		DataPlayer ret = new DataPlayer();
		ret.player = player;
		ret.lastIgn = lastIgn;
		ret.permitted = permitted;
		ret.chunkName = chunkName;
		return ret;
	}
	
}