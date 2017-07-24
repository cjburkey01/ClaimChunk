package com.cjburkey.claimchunk.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AccessHandler {
	
	private final Queue<Access> access = new ConcurrentLinkedQueue<>();
	
	public void giveAccess(UUID owner, UUID player) {
		if (!hasAccess(owner, player)) {
			access.add(new Access(owner, player));
		}
	}
	
	public void takeAccess(UUID owner, UUID player) {
		if (hasAccess(owner, player)) {
			access.remove(getAccess(owner, player));
		}
	}
	
	public boolean hasAccess(UUID owner, UUID player) {
		for (UUID uuid : getPermitted(owner)) {
			if (uuid.equals(player)) {
				return true;
			}
		}
		return false;
	}
	
	private Access getAccess(UUID owner, UUID player) {
		for(Access a : access) {
			if(a.getOwner().equals(owner) && a.getAcessee().equals(player)) {
				return a;
			}
		}
		return null;
	}
	
	private UUID[] getPermitted(UUID owner) {
		List<UUID> out = new ArrayList<>();
		for (Access permit : access) {
			if (permit.getOwner().equals(owner)) {
				out.add(permit.getAcessee());
			}
		}
		return out.toArray(new UUID[out.size()]);
	}
	
}