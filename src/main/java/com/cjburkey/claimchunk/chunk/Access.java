package com.cjburkey.claimchunk.chunk;

import java.util.UUID;
import org.bukkit.entity.Player;

public class Access {
	
	private final UUID owner;
	private final UUID allowed;
	
	public Access(UUID owner, UUID allowed) {
		this.owner = owner;
		this.allowed = allowed;
	}
	
	public Access(Player owner, UUID allowed) {
		this(owner.getUniqueId(), allowed);
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public UUID getAcessee() {
		return allowed;
	}
	
	public String toString() {
		return owner.toString() + ';' + allowed.toString();
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allowed == null) ? 0 : allowed.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Access other = (Access) obj;
		if (allowed == null) {
			if (other.allowed != null)
				return false;
		} else if (!allowed.equals(other.allowed))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
}