package com.cjburkey.claimchunk.chunk;

import java.util.UUID;
import org.bukkit.entity.Player;

public class Access {
	
	private final UUID owner;
	private final UUID allowed;
	
	public Access(Player owner, UUID allowed) {
		this.owner = owner.getUniqueId();
		this.allowed = allowed;
	}
	
}